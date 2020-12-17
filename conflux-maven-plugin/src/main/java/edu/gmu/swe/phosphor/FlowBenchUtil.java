package edu.gmu.swe.phosphor;

import edu.columbia.cs.psl.phosphor.runtime.MultiTainter;
import edu.columbia.cs.psl.phosphor.runtime.Taint;

public class FlowBenchUtil {

    public static String taintWithIndices(String input) {
        return taintWithIndices(input, 0, input.length());
    }

    public static <T> T[] taintWithIndices(T[] input) {
        return taintWithIndices(input, 0, input.length);
    }

    public static boolean[] taintWithIndices(boolean[] input) {
        return taintWithIndices(input, 0, input.length);
    }

    public static byte[] taintWithIndices(byte[] input) {
        return taintWithIndices(input, 0, input.length);
    }

    public static char[] taintWithIndices(char[] input) {
        return taintWithIndices(input, 0, input.length);
    }

    public static double[] taintWithIndices(double[] input) {
        return taintWithIndices(input, 0, input.length);
    }

    public static float[] taintWithIndices(float[] input) {
        return taintWithIndices(input, 0, input.length);
    }

    public static int[] taintWithIndices(int[] input) {
        return taintWithIndices(input, 0, input.length);
    }

    public static long[] taintWithIndices(long[] input) {
        return taintWithIndices(input, 0, input.length);
    }

    public static short[] taintWithIndices(short[] input) {
        return taintWithIndices(input, 0, input.length);
    }

    public static String taintWithIndices(String input, int start, int len) {
        char[] c = input.toCharArray();
        for(int i = start; i < len; i++) {
            c[i] = MultiTainter.taintedChar(c[i], i);
        }
        return new String(c);
    }

    public static <T> T[] taintWithIndices(T[] input, int start, int len) {
        for(int i = start; i < len; i++) {
            MultiTainter.taintedReference(input[i], Taint.withLabel(i));
        }
        return input;
    }

    public static boolean[] taintWithIndices(boolean[] input, int start, int len) {
        for(int i = start; i < len; i++) {
            input[i] = MultiTainter.taintedBoolean(input[i], i);
        }
        return input;
    }

    public static byte[] taintWithIndices(byte[] input, int start, int len) {
        for(int i = start; i < len; i++) {
            input[i] = MultiTainter.taintedByte(input[i], i);
        }
        return input;
    }

    public static char[] taintWithIndices(char[] input, int start, int len) {
        for(int i = start; i < len; i++) {
            input[i] = MultiTainter.taintedChar(input[i], i);
        }
        return input;
    }

    public static double[] taintWithIndices(double[] input, int start, int len) {
        for(int i = start; i < len; i++) {
            input[i] = MultiTainter.taintedDouble(input[i], i);
        }
        return input;
    }

    public static float[] taintWithIndices(float[] input, int start, int len) {
        for(int i = start; i < len; i++) {
            input[i] = MultiTainter.taintedFloat(input[i], i);
        }
        return input;
    }

    public static int[] taintWithIndices(int[] input, int start, int len) {
        for(int i = start; i < len; i++) {
            input[i] = MultiTainter.taintedInt(input[i], i);
        }
        return input;
    }

    public static long[] taintWithIndices(long[] input, int start, int len) {
        for(int i = start; i < len; i++) {
            input[i] = MultiTainter.taintedLong(input[i], i);
        }
        return input;
    }

    public static short[] taintWithIndices(short[] input, int start, int len) {
        for(int i = start; i < len; i++) {
            input[i] = MultiTainter.taintedShort(input[i], i);
        }
        return input;
    }
}
