package edu.neu.ccs.conflux.policy.conflux;

import edu.columbia.cs.psl.phosphor.runtime.MultiTainter;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.neu.ccs.conflux.policy.BasePolicyTest;
import org.junit.Test;

public class InterMethodLoopConfluxITCase extends BasePolicyTest {

    @Test
    public void testReceiverInvariantParamInvariant() {
        int[] values = new int[]{1, 1, 0, 1};
        BasePolicyTest.taintWithIndices(values);
        Example e = new Example();
        for(int i = 0; i < values.length; i++) {
            if(values[i] == 0) {
                // e refers to the same object on each loop iteration
                e.setX(5);
            }
        }
        assertNullOrEmpty(MultiTainter.getTaint(e.getX()));
    }

    @Test
    public void testReceiverVariantParamInvariant() {
        int[] values = new int[]{0, 1, 0, 1};
        BasePolicyTest.taintWithIndices(values);
        Example[] es = new Example[values.length];
        for(int i = 0; i < values.length; i++) {
            es[i] = new Example();
            if(values[i] == 0) {
                // es[i] refers to a different object on each loop iteration
                es[i].setX(5);
            }
        }
        for(int i = 0; i < values.length; i++) {
            Taint<?> tag = MultiTainter.getTaint(es[i].getX());
            if(i % 2 == 0) {
                assertTaintHasOnlyLabel(tag, i);
            } else {
                assertNullOrEmpty(tag);
            }
        }
    }

    @Test
    public void testReceiverInvariantParamVariant() {
        int[] values = new int[]{1, 1, 0, 1};
        BasePolicyTest.taintWithIndices(values);
        Example e = new Example();
        for(int i = 0; i < values.length; i++) {
            if(values[i] == 0) {
                // e refers to the same object on each loop iteration
                // i is a different value on each loop iteration
                e.setX(i);
            }
        }
        Taint<?> tag = MultiTainter.getTaint(e.getX());
        assertTaintHasOnlyLabel(tag, 2);
    }

    @Test
    public void testReceiverVariantParamVariant() {
        int[] values = new int[]{0, 1, 0, 1};
        BasePolicyTest.taintWithIndices(values);
        Example[] es = new Example[values.length];
        for(int i = 0; i < values.length; i++) {
            es[i] = new Example();
            if(values[i] == 0) {
                // es[i] refers to a different object on each loop iteration
                // i is a different value on each loop iteration
                es[i].setX(i);
            }
        }
        for(int i = 0; i < values.length; i++) {
            Taint<?> tag = MultiTainter.getTaint(es[i].getX());
            if(i % 2 == 0) {
                assertTaintHasOnlyLabel(tag, i);
            } else {
                assertNullOrEmpty(tag);
            }
        }
    }

    private static class Example {
        private int x = 0;

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }
    }

    @Test
    public void testReplace() {
        char[] values = new char[]{taint('h', 0), taint('i', 1)};
        replace(values, 'h', 'j');
        assertTaintHasOnlyLabel(MultiTainter.getTaint(values[0]), 0);
        assertTaintHasOnlyLabel(MultiTainter.getTaint(values[1]), 1);
    }

    private static char taint(char c, Object label) {
        return MultiTainter.taintedChar(c, label);
    }

    @Test
    public void testContains() {
        char[] values = new char[]{taint('h', 0), taint('i', 1)};
        char[] result = contains(values, 'h');
        assertNullOrEmpty(MultiTainter.getTaint(result[0]));
    }

    static void replace(char[] a, char t, char r) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == t) {
                set(a, i, r);
            }
        }
    }

    static char[] contains(char[] a, char t) {
        char[] res= new char[]{'n'};
        for (int i = 0; i < a.length; i++) {
            if (a[i] == t) {
                set(res, 0, 'y');
            }
        }
        return res;
    }

    static void set(char[] a, int i, char x) {
        a[i] = x;
    }
}
