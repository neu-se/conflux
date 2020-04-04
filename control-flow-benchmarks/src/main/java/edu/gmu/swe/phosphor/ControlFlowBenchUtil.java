package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static edu.gmu.swe.phosphor.FlowBenchUtil.taintWithIndices;

public class ControlFlowBenchUtil {

    public static final String RESERVED_CHARS_FOR_PERCENT_ENCODING = ":@&?%";

    public static void checkBaseNEncode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy, int n,
                                        UnaryOperator<byte[]> encoder) {
        int bitsPerChar = 8;
        int bitsPerPack = 31 - Integer.numberOfLeadingZeros(n); // floor(log_2(x)), if x > 0
        int gcd = gcd(bitsPerChar, bitsPerPack);
        int charsPerGroup = bitsPerPack / gcd;
        String value = repeat("Lorem ipsum", charsPerGroup); // ensure that the value cleanly divides into groups
        String input = taintWithIndices(value + value, policy);
        byte[] output = encoder.apply(input.getBytes());
        // Build the expected sets
        BitSet[] expected = new BitSet[output.length];
        for(int i = 0; i < expected.length; i++) {
            expected[i] = new BitSet();
        }
        for(int inputIndex = 0; inputIndex < input.length(); inputIndex++) {
            if(policy.inTaintedRange(inputIndex, input.length())) {
                for(int i = 0; i < bitsPerChar; i += bitsPerPack) {
                    int bitIndex = bitsPerChar * inputIndex + i;
                    int packIndex = bitIndex / bitsPerPack;
                    expected[packIndex].set(inputIndex);
                }
            }
        }
        for(int outputIndex = 0; outputIndex < output.length; outputIndex++) {
            Set<Integer> expectedSet = expected[outputIndex].stream()
                    .boxed()
                    .collect(Collectors.toSet());
            benchResult.check(expectedSet, output[outputIndex]);
        }
    }

    public static void checkBaseNDecode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy, int n,
                                        UnaryOperator<byte[]> encoder, Function<byte[], byte[]> decoder) {
        int bitsPerChar = 8;
        int bitsPerPack = 31 - Integer.numberOfLeadingZeros(n); // floor(log_2(x)), if x > 0
        int gcd = gcd(bitsPerChar, bitsPerPack);
        int charsPerGroup = bitsPerPack / gcd;
        String value = repeat("Lorem ipsum", charsPerGroup); // ensure that the value cleanly divides into groups
        value += value;
        byte[] input = encoder.apply(value.getBytes());
        taintWithIndices(input, policy);
        byte[] output = decoder.apply(input);
        // Build the expected sets
        BitSet[] expected = new BitSet[output.length];
        for(int i = 0; i < expected.length; i++) {
            expected[i] = new BitSet();
        }
        for(int inputIndex = 0; inputIndex < input.length; inputIndex++) {
            if(policy.inTaintedRange(inputIndex, input.length)) {
                for(int i = 0; i < bitsPerPack; i += bitsPerChar) {
                    int bitIndex = bitsPerPack * inputIndex + i;
                    int charIndex = bitIndex / bitsPerChar;
                    expected[charIndex].set(inputIndex);
                }
            }
        }
        for(int outputIndex = 0; outputIndex < output.length; outputIndex++) {
            Set<Integer> expectedSet = expected[outputIndex].stream()
                    .boxed()
                    .collect(Collectors.toSet());
            benchResult.check(expectedSet, output[outputIndex]);
        }
    }

    /**
     * Converts an array of bytes into a string of hexadecimal digits
     */
    public static void checkHexEncode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy,
                                      Function<byte[], String> encoder) {
        byte[] input = taintWithIndices(new byte[]{126, 74, -79, 32, 126, 74, -79, 32}, policy);
        String output = encoder.apply(input);
        for(int i = 0; i < input.length; i++) {
            if(policy.inTaintedRange(i, input.length)) {
                benchResult.check(Collections.singletonList(i), output.charAt(i * 2));
                benchResult.check(Collections.singletonList(i), output.charAt(i * 2 + 1));
            } else {
                benchResult.checkEmpty(output.charAt(i * 2));
                benchResult.checkEmpty(output.charAt(i * 2 + 1));
            }
        }
    }

    /**
     * Converts a string of hexadecimal digits to an array of bytes
     */
    public static void checkHexDecode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy,
                                      Function<String, byte[]> decoder) {
        String input = taintWithIndices("7e4ab1207e4ab120", policy);
        byte[] output = decoder.apply(input);
        for(int i = 0; i < input.length(); i += 2) {
            if(policy.inTaintedRange(i, input.length())) {
                benchResult.check(Arrays.asList(i, i + 1), output[i / 2]);
            } else {
                benchResult.checkEmpty(output[i / 2]);
            }
        }
    }

    public static String repeat(String s, int n) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < n; i++) {
            builder.append(s);
        }
        return builder.toString();
    }

    public static int gcd(int a, int b) {
        // Euclid's algorithm for greatest common divisor
        return b == 0 ? a : gcd(b, a % b);
    }

    /**
     * Checks taint propagation when encoding a string of spaces characters as '+'s as is done in the
     * 'www-form-urlencoded' encoding scheme.
     */
    public static void checkURLEncodeSpaces(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy,
                                            UnaryOperator<String> encoder) {
        String input = "     ";
        input = taintWithIndices(input + input, policy);
        String output = encoder.apply(input);
        List<Integer> outputCharsPerInput = new LinkedList<>(Collections.nCopies(input.length(), 1));
        checkOneInputToManyOutputs(benchResult, policy, output, outputCharsPerInput);
    }

    /**
     * Checks taint propagation when decoding a string of '+' characters as spaces as is done when decoding the
     * 'www-form-urlencoded' encoding scheme.
     */
    public static void checkURLDecodeSpaces(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy,
                                            UnaryOperator<String> decoder) {
        String input = "+++++";
        input = taintWithIndices(input + input, policy);
        String output = decoder.apply(input);
        List<Integer> inputCharsPerOutput = new LinkedList<>(Collections.nCopies(output.length(), 1));
        checkManyInputsToOneOutput(benchResult, policy, output, inputCharsPerOutput);
    }

    /**
     * Checks taint propagation when encoding reserved ascii characters to percent-encoded octets.
     */
    public static void checkPercentEncodeReserved(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy,
                                                  UnaryOperator<String> encoder) {
        String input = RESERVED_CHARS_FOR_PERCENT_ENCODING + RESERVED_CHARS_FOR_PERCENT_ENCODING;
        input = taintWithIndices(input + input, policy);
        String output = encoder.apply(input);
        List<Integer> outputCharsPerInput = new LinkedList<>(Collections.nCopies(input.length(), 3));
        checkOneInputToManyOutputs(benchResult, policy, output, outputCharsPerInput);
    }

    /**
     * Checks taint propagation when decoding reserved ascii characters from percent-encoded octets.
     */
    public static void checkPercentDecodeReserved(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy,
                                                  UnaryOperator<String> decoder) {
        String input = "%3a%40%26%3f%25"; // 5 characters each 1 percent-encoded octet long
        input = taintWithIndices(input + input, policy);
        String output = decoder.apply(input);
        List<Integer> inputCharsPerOutput = new LinkedList<>(Collections.nCopies(output.length(), 3));
        checkManyInputsToOneOutput(benchResult, policy, output, inputCharsPerOutput);
    }

    /**
     * Checks taint propagation when encoding unicode characters to percent-encoded octets.
     */
    public static void checkPercentEncodeUnicode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy,
                                                 UnaryOperator<String> encoder) {
        String input = "\u03A9\u3399"; // encoded as 2/3 percent-encoded octets
        input = taintWithIndices(input + input, policy);
        String output = encoder.apply(input);
        List<Integer> outputCharsPerInput = Arrays.asList(6, 9, 6, 9);
        checkOneInputToManyOutputs(benchResult, policy, output, outputCharsPerInput);
    }

    /**
     * Checks taint propagation when decoding unicode characters from percent-encoded octets.
     */
    public static void checkPercentDecodeUnicode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy,
                                                 UnaryOperator<String> decoder) {
        String input = "%CE%A9%E3%8E%99"; // 2 encoded characters consisting of 2/3 percent-encoded octets
        input = taintWithIndices(input + input, policy);
        String output = decoder.apply(input);
        List<Integer> inputCharsPerOutput = Arrays.asList(6, 9, 6, 9);
        checkManyInputsToOneOutput(benchResult, policy, output, inputCharsPerOutput);
    }

    public static void checkOneInputToManyOutputs(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy,
                                                  String output, List<Integer> outputCharsPerInput) {
        int inputIndex = 0;
        int outputIndex = 0;
        for(int outputChars : outputCharsPerInput) {
            List<Integer> expected = Collections.emptyList();
            if(policy.inTaintedRange(inputIndex, outputCharsPerInput.size())) {
                expected = Collections.singletonList(inputIndex);
            }
            for(int i = 0; i < outputChars; i++) {
                benchResult.check(expected, output.charAt(outputIndex++));
            }
            inputIndex++;
        }
    }

    public static void checkManyInputsToOneOutput(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy,
                                                  String output, List<Integer> inputCharsPerOutput) {
        int numInputs = 0;
        for(int inputChars : inputCharsPerOutput) {
            numInputs += inputChars;
        }
        int inputIndex = 0;
        int outputIndex = 0;
        for(int inputChars : inputCharsPerOutput) {
            Set<Integer> expected = new HashSet<>();
            for(int i = 0; i < inputChars; i++) {
                if(policy.inTaintedRange(inputIndex, numInputs)) {
                    expected.add(inputIndex);
                }
                inputIndex++;
            }
            benchResult.check(expected, output.charAt(outputIndex++));
        }
    }
}
