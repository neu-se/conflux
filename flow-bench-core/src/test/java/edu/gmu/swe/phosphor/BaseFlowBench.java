package edu.gmu.swe.phosphor;

import edu.columbia.cs.psl.phosphor.runtime.MultiTainter;
import edu.columbia.cs.psl.phosphor.runtime.Taint;

import java.util.Arrays;
import java.util.HashSet;

public abstract class BaseFlowBench {

    public static String taintWithIndices(String input) {
        char[] c = input.toCharArray();
        for(int i = 0; i < c.length; i++) {
            c[i] = MultiTainter.taintedChar(c[i], i);
        }
        return new String(c);
    }

    public static <T> T[] taintWithIndices(T[] input) {
        for(int i = 0; i < input.length; i++) {
            MultiTainter.taintedObject(input[i], new Taint<>(i));
        }
        return input;
    }

    public static boolean[] taintWithIndices(boolean[] input) {
        for(int i = 0; i < input.length; i++) {
            input[i] = MultiTainter.taintedBoolean(input[i], i);
        }
        return input;
    }

    public static byte[] taintWithIndices(byte[] input) {
        for(int i = 0; i < input.length; i++) {
            input[i] = MultiTainter.taintedByte(input[i], i);
        }
        return input;
    }

    public static char[] taintWithIndices(char[] input) {
        for(int i = 0; i < input.length; i++) {
            input[i] = MultiTainter.taintedChar(input[i], i);
        }
        return input;
    }

    public static double[] taintWithIndices(double[] input) {
        for(int i = 0; i < input.length; i++) {
            input[i] = MultiTainter.taintedDouble(input[i], i);
        }
        return input;
    }

    public static float[] taintWithIndices(float[] input) {
        for(int i = 0; i < input.length; i++) {
            input[i] = MultiTainter.taintedFloat(input[i], i);
        }
        return input;
    }

    public static int[] taintWithIndices(int[] input) {
        for(int i = 0; i < input.length; i++) {
            input[i] = MultiTainter.taintedInt(input[i], i);
        }
        return input;
    }

    public static long[] taintWithIndices(long[] input) {
        for(int i = 0; i < input.length; i++) {
            input[i] = MultiTainter.taintedLong(input[i], i);
        }
        return input;
    }

    public static short[] taintWithIndices(short[] input) {
        for(int i = 0; i < input.length; i++) {
            input[i] = MultiTainter.taintedShort(input[i], i);
        }
        return input;
    }

    public static Taint[] getTaints(String s) {
        Taint[] result = new Taint[s.length()];
        for(int i = 0; i < s.length(); i++) {
            result[i] = MultiTainter.getTaint(s.charAt(i));
        }
        return result;
    }

    public static BinaryFlowBenchResult calculateBinaryResult(Taint[] expected, Taint[] actual) {
        if(expected == null) {
            throw new IllegalArgumentException("Expected array must be non-null");
        }
        if(actual == null) {
            actual = new Taint[expected.length];
        }
        if(expected.length != actual.length) {
            throw new IllegalArgumentException("Expected and actual arrays must be same length");
        }
        int truePositives = 0;
        int trueNegatives = 0;
        int falsePositives = 0;
        int falseNegatives = 0;
        for(int i = 0; i < expected.length; i++) {
            Taint e = expected[i];
            Taint a = actual[i];
            if(e == null || e.isEmpty()) {
                if(a == null || a.isEmpty()) {
                    trueNegatives++;
                } else {
                    falsePositives++;
                }
            } else {
                if(a == null || a.isEmpty()) {
                    falseNegatives++;
                } else {
                    truePositives++;
                }
            }
        }
        return new BinaryFlowBenchResult(truePositives, trueNegatives, falsePositives, falseNegatives);
    }

    public static MultiLabelFlowBenchResult calculateMultiLabelResult(Taint[] expected, Taint[] actual) {
        if(expected == null) {
            throw new IllegalArgumentException("Expected array must be non-null");
        }
        if(actual == null) {
            actual = new Taint[expected.length];
        }
        if(expected.length != actual.length) {
            throw new IllegalArgumentException("Expected and actual arrays must be same length");
        }
        int[] correctLabels = new int[expected.length];
        int[] predictedLabels = new int[expected.length];
        int[] expectedLabels = new int[expected.length];
        for(int i = 0; i < expected.length; i++) {
            Object[] e = expected[i] == null ? new Object[0] : expected[i].getLabels();
            Object[] a = actual[i] == null ? new Object[0] : actual[i].getLabels();
            expectedLabels[i] = e.length;
            predictedLabels[i] = a.length;
            HashSet<Object> expectedSet = new HashSet<>(Arrays.asList(a));
            for(Object o : a) {
                if(expectedSet.contains(o)) {
                    correctLabels[i]++;
                }
            }
        }
        return new MultiLabelFlowBenchResult(correctLabels, predictedLabels, expectedLabels);
    }
}
