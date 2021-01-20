package edu.neu.ccs.conflux.policy.conflux;

import edu.columbia.cs.psl.phosphor.runtime.MultiTainter;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.neu.ccs.conflux.policy.BasePolicyTest;
import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

public class GeneralConfluxITCase extends BasePolicyTest {

    @Test
    public void testBasicSwitch() {
        int x = MultiTainter.taintedInt(88, "testBasicSwitch");
        int y;
        switch (x) {
            case 0:
            case 1:
                y = 5;
                break;
            case 88:
                y = 4;
                break;
            case 40:
            default:
                y = 7;
        }
        assertTaintHasOnlyLabel(MultiTainter.getTaint(y), "testBasicSwitch");
    }

    @Test
    public void testBranchBeforeSuper() {
        Parent p = new Child(MultiTainter.taintedBoolean(true, "testBranchBeforeSuper"));
        assertTaintHasOnlyLabel(MultiTainter.getTaint(p.i), "testBranchBeforeSuper");
    }

    @Test
    public void testSimpleIfEqualTaken() {
        int a = MultiTainter.taintedInt(5, "a");
        int b = MultiTainter.taintedInt(5, "b");
        int c = 7;
        if(a == b) {
            c = 22;
        }
        assertTaintHasOnlyLabels(MultiTainter.getTaint(c), "a", "b");
    }

    @Test
    public void testSimpleIfNotEqualNotTaken() {
        int a = MultiTainter.taintedInt(5, "a");
        int b = MultiTainter.taintedInt(5, "b");
        int c = 7;
        int d = 0;
        if(a != b) {
            d = 88;
        } else {
            c = 22;
        }
        assertTaintHasOnlyLabels(MultiTainter.getTaint(c), "a", "b");
    }

    @Test
    public void testSimpleIfNotEqualTaken() {
        int a = MultiTainter.taintedInt(5, "a");
        int b = MultiTainter.taintedInt(7, "b");
        int c = 7;
        int d = 0;
        if(a != b) {
            d = 88;
        } else {
            c = 22;
        }
        assertNullOrEmpty(MultiTainter.getTaint(d));
    }

    @Test
    public void testForLoopMultipleReturns() {
        char[] c = createDigitArray();
        char[] copy = copyDigits(c, false);
        checkDigitArray(copy);
    }

    @Test
    public void testSimpleAnd() {
        int a = MultiTainter.taintedInt(5, "a");
        int b = MultiTainter.taintedInt(22, "b");
        int c = 7;
        if(a == 5 && b == 22) {
            c = 43;
        }
        assertTaintHasOnlyLabels(MultiTainter.getTaint(c), "a", "b");
    }

    @Test
    public void testArrayEqualsSingleExitFalse() {
        int[] a = new int[]{0, 1, 2, 3, 4};
        taintWithIndices(a);
        int[] b = new int[]{0, 1, 2, 3, 5};
        boolean result = arrayEqualsSingleExit(a, b);
        assertNullOrEmpty(MultiTainter.getTaint(result));
    }


    @Test
    public void testArrayEqualsMultipleExitFalse() {
        int[] a = new int[]{0, 1, 2, 3, 4};
        taintWithIndices(a);
        int[] b = new int[]{0, 1, 2, 3, 5};
        boolean result = arrayEqualsMultipleExit(a, b);
        assertNullOrEmpty(MultiTainter.getTaint(result));
    }

    @Test
    public void testRecursiveArrayEqualsTrue() {
        int[] a = new int[]{0, 1, 2, 3, 4};
        taintWithIndices(a);
        int[] b = new int[]{0, 1, 2, 3, 4};
        boolean result = recursiveEquals(0, a, b);
        assertTaintHasOnlyLabels(MultiTainter.getTaint(result), 0, 1, 2, 3, 4);
    }

    @Test
    public void testGetCharsTaintedReference() {
        char[] c = new char[4];
        String s = MultiTainter.taintedReference("012", 0);
        s.getChars(0, 3, c, 0);
        for(int i = 0; i < c.length; i++) {
            if(i > 2) {
                assertNullOrEmpty(MultiTainter.getTaint(c[i]));
            } else {
                assertTaintHasOnlyLabel(MultiTainter.getTaint(c[i]), 0);
            }
        }
    }

    @Test
    public void testBitSetGetFalse() {
        BitSet b = new BitSet(10);
        int i = MultiTainter.taintedInt(0, 0);
        boolean z = b.get(i);
        assertNullOrEmpty(MultiTainter.getTaint(z));
    }

    public static boolean arrayEqualsSingleExit(int[] a, int[] b) {
        boolean result = true;
        if(a.length != b.length) {
            result = false;
        } else {
            for(int i = 0; i < a.length; i++) {
                if(a[i] != b[i]) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    public static boolean arrayEqualsMultipleExit(int[] a, int[] b) {
        if(a.length != b.length) {
            return false;
        }
        for(int i = 0; i < a.length; i++) {
            if(a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean recursiveEquals(int i, int[] a, int[] b) {
        if(i >= a.length) {
            return true;
        } else if(a[i] != b[i]) {
            return false;
        } else {
            return recursiveEquals(++i, a, b);
        }
    }

    private static char[] copyDigits(char[] c, boolean noZeros) {
        char[] copy = new char[c.length];
        for(int i = 0; i < c.length; i++) {
            if(c[i] == '0') {
                if(noZeros) {
                    throw new IllegalArgumentException();
                }
                copy[i] = '%';
            } else {
                copy[i] = c[i];
            }
        }
        return copy;
    }

    public static char[] createDigitArray() {
        char[] c = "0123456789".toCharArray();
        for(int i = 0; i < c.length; i++) {
            c[i] = MultiTainter.taintedChar(c[i], i);
        }
        return c;
    }

    public static void checkDigitArray(char[] c) {
        for(int i = 0; i < c.length; i++) {
            Taint t = MultiTainter.getTaint(c[i]);
            assertNotNull(t);
            Object[] labels = t.getLabels();
            assertArrayEquals(new Object[]{i}, labels);
        }
    }

    private static class Parent {
        int i;

        Parent(int i) {
            this.i = i;
        }
    }

    private static class Child extends Parent {
        Child(boolean b) {
            super(b ? 55 : 429);
        }
    }
}
