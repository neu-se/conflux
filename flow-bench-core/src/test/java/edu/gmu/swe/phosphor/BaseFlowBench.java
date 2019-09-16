package edu.gmu.swe.phosphor;

import edu.columbia.cs.psl.phosphor.runtime.MultiTainter;
import edu.columbia.cs.psl.phosphor.runtime.Taint;

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
}
