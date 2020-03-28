package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static edu.gmu.swe.phosphor.FlowBenchUtil.taintWithIndices;

public class ControlFlowBenchUtil {

    public static void checkBaseNEncode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy, int n,
                                        Function<byte[], byte[]> encoder) {
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
                                        Function<byte[], byte[]> encoder, Function<byte[], byte[]> decoder) {
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
}
