package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static edu.gmu.swe.phosphor.FlowBenchUtil.taintWithIndices;

public class ControlFlowBenchUtil {

    public static final String RESERVED_CHARS_FOR_PERCENT_ENCODING = ":@&?%";


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

    /**
     * Checks taint propagation when encoding a string of spaces characters as '+'s as is done in the
     * 'www-form-urlencoded' encoding scheme.
     */
    public static void checkSpacesUrlEncode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy,
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
    public static void checkSpacesUrlDecode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy,
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
    public static void checkReservedPercentEncode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy,
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
    public static void checkReservedPercentDecode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy,
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
    public static void checkUnicodePercentEncode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy,
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
    public static void checkUnicodePercentDecode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy,
                                                 UnaryOperator<String> decoder) {
        String input = "%CE%A9%E3%8E%99"; // 2 encoded characters consisting of 2/3 percent-encoded octets
        input = taintWithIndices(input + input, policy);
        String output = decoder.apply(input);
        List<Integer> inputCharsPerOutput = Arrays.asList(6, 9, 6, 9);
        checkManyInputsToOneOutput(benchResult, policy, output, inputCharsPerOutput);
    }

    /**
     * Checks taint propagation when escaping HTML reserved characters by replacing them with named character entities.
     */
    public static void checkHtmlEscape(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy,
                                       UnaryOperator<String> escaper) {
        String input = "&<>";
        input = taintWithIndices(input + input, policy);
        String output = escaper.apply(input);
        List<Integer> outputCharsPerInput = Arrays.asList(5, 4, 4, 5, 4, 4);
        checkOneInputToManyOutputs(benchResult, policy, output, outputCharsPerInput);
    }

    /**
     * Checks taint propagation when unescaping named character entities to HTML reserved characters;
     */
    public static void checkHtmlUnescape(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy,
                                         UnaryOperator<String> unescaper) {
        String input = "&amp;&lt;&gt;";
        input = taintWithIndices(input + input, policy);
        String output = unescaper.apply(input);
        List<Integer> inputCharsPerOutput = Arrays.asList(5, 4, 4, 5, 4, 4);
        checkManyInputsToOneOutput(benchResult, policy, output, inputCharsPerOutput);
    }

    /**
     * Checks taint propagation when escaping EcmaScript/JavaScript special characters.
     */
    public static void checkJavaScriptEscape(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy,
                                             UnaryOperator<String> escaper) {
        String input = "\"'\\/\t\n\f\b\u2028\u2029";
        input = taintWithIndices(input + input, policy);
        String output = escaper.apply(input);
        List<Integer> outputCharsPerInput = Arrays.asList(2, 2, 2, 2, 2, 2, 2, 2, 6, 6, 2, 2, 2, 2, 2, 2, 2, 2, 6, 6);
        checkOneInputToManyOutputs(benchResult, policy, output, outputCharsPerInput);
    }

    /**
     * Checks taint propagation when unescaping EcmaScript/JavaScript special characters.
     */
    public static void checkJavaScriptUnescape(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy,
                                               UnaryOperator<String> unescaper) {
        String input = "\\\"\\'\\\\\\/\\t\\n\\f\\b\\u2028\\u2029\\\"\\'\\\\\\/\\t\\n\\f\\b\\u000B\\u2029";
        input = taintWithIndices(input + input, policy);
        String output = unescaper.apply(input);
        List<Integer> inputCharsPerOutput = Arrays.asList(2, 2, 2, 2, 2, 2, 2, 2, 6, 6, 2, 2, 2, 2, 2, 2, 2, 2, 6, 6);
        checkManyInputsToOneOutput(benchResult, policy, output, inputCharsPerOutput);
    }
}
