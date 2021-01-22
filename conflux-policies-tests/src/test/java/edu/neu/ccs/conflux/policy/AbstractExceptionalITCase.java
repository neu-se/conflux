package edu.neu.ccs.conflux.policy;

import edu.columbia.cs.psl.phosphor.runtime.MultiTainter;
import org.junit.Test;

import static org.junit.Assert.fail;

@SuppressWarnings({"ResultOfMethodCallIgnored", "unused", "Convert2MethodRef"})
public class AbstractExceptionalITCase extends BasePolicyTest {

    @Test
    public void testArrayLoadNPE() {
        // NullPointerException -> {arrayref}
        int[] arr = MultiTainter.taintedReference(null, "arrayref");
        int index = 0;
        testPropagation(() -> {
            int x = arr[index];
        }, NullPointerException.class, "arrayref");
    }

    @Test
    public void testArrayLoadAIOOBE() {
        // ArrayIndexOutOfBoundsException -> {index, arrayref}
        int[] arr = MultiTainter.taintedReference(new int[0], "arrayref");
        int index = MultiTainter.taintedInt(0, "index");
        testPropagation(() -> {
            int x = arr[index];
        }, ArrayIndexOutOfBoundsException.class, "index", "arrayref");
    }

    @Test
    public void testReferenceArrayStoreNPE() {
        // NullPointerException -> {arrayref}
        Object[] arr = MultiTainter.taintedReference(null, "arrayref");
        int index = 0;
        Object value = "hello";
        testPropagation(() -> arr[index] = value, NullPointerException.class, "arrayref");
    }

    @Test
    public void testReferenceArrayStoreAIOOBE() {
        // ArrayIndexOutOfBoundsException -> {index, arrayref}
        Object[] arr = MultiTainter.taintedReference(new String[0], "arrayref");
        int index = MultiTainter.taintedInt(0, "index");
        Object value = "hello";
        testPropagation(() -> arr[index] = value, ArrayIndexOutOfBoundsException.class, "index", "arrayref");
    }

    @Test
    public void testReferenceArrayStoreASE() {
        // ArrayStoreException -> {value, arrayref}
        Object[] arr = MultiTainter.taintedReference(new String[1], "arrayref");
        int index = 0;
        Object value = MultiTainter.taintedReference((Object) 1, "value");
        testPropagation(() -> arr[index] = value, ArrayStoreException.class, "value", "arrayref");
    }

    @Test
    public void testPrimitiveArrayStoreNPE() {
        // NullPointerException -> {arrayref}
        int[] arr = MultiTainter.taintedReference(null, "arrayref");
        int index = 0;
        int value = 7;
        testPropagation(() -> arr[index] = value, NullPointerException.class, "arrayref");
    }

    @Test
    public void testPrimitiveArrayStoreAIOOBE() {
        // ArrayIndexOutOfBoundsException -> {index, arrayref}
        int[] arr = MultiTainter.taintedReference(new int[0], "arrayref");
        int index = MultiTainter.taintedInt(0, "index");
        int value = 7;
        testPropagation(() -> arr[index] = value, ArrayIndexOutOfBoundsException.class, "index", "arrayref");
    }

    @Test
    public void testRemAE() {
        // ArithmeticException -> {value2}
        long value2 = MultiTainter.taintedLong(0, "value2");
        testPropagation(() -> {
            long x = 7 % value2;
        }, ArithmeticException.class, "value2");
    }

    @Test
    public void testDivAE() {
        // ArithmeticException -> {value2}
        long value2 = MultiTainter.taintedLong(0, "value2");
        testPropagation(() -> {
            long x = 7 / value2;
        }, ArithmeticException.class, "value2");
    }

    @Test
    public void testGetFieldNPE() {
        // NullPointerException -> {objectref}
        Record obj = MultiTainter.taintedReference(null, "objectref");
        testPropagation(() -> {
            int x = obj.i;
        }, NullPointerException.class, "objectref");
    }

    @Test
    public void testPutFieldNPE() {
        // NullPointerException -> {objectref}
        Record obj = MultiTainter.taintedReference(null, "objectref");
        testPropagation(() -> obj.i = 7, NullPointerException.class, "objectref");
    }

    @Test
    public void testInvokeVirtualNPE() {
        // NullPointerException -> {objectref}
        Object obj = MultiTainter.taintedReference(null, "objectref");
        testPropagation(() -> obj.toString(), NullPointerException.class, "objectref");
    }

    @Test
    public void testInvokeInterfaceNPE() {
        // NullPointerException -> {objectref}
        Example obj = MultiTainter.taintedReference(null, "objectref");
        testPropagation(() -> obj.getValue(), NullPointerException.class, "objectref");
    }

    @Test
    public void testNewArrayNASE() {
        // NegativeArraySizeException -> {count}
        testPropagation(() -> {
                    int[] i = new int[MultiTainter.taintedInt(-1, "count")];
                },
                NegativeArraySizeException.class, "count");
    }

    @Test
    public void testArrayLengthNPE() {
        //  NullPointerException -> {arrayref}
        Object[] arr = MultiTainter.taintedReference(null, "arrayref");
        testPropagation(() -> {
            int x = arr.length;
        }, NullPointerException.class, "arrayref");
    }

    @Test
    public void testThrowNPE() {
        // NullPointerException -> {objectref}
        RuntimeException e = MultiTainter.taintedReference(null, "objectref");
        testPropagation(() -> {
            throw e;
        }, NullPointerException.class, "objectref");
    }

    @Test
    public void testThrowExplicit() {
        // Throwable -> {objectref}
        RuntimeException e = MultiTainter.taintedReference(new RuntimeException(), "objectref");
        testPropagation(() -> {
            throw e;
        }, RuntimeException.class, "objectref");
    }

    @Test
    public void testCheckCastCCE() {
        // ClassCastException -> {objectref}
        testPropagation(() -> {
                    String s = (String) MultiTainter.taintedReference(new Object(), "objectref");
                },
                ClassCastException.class, "objectref");
    }

    @Test
    public void testMonitorEnterNPE() {
        // NullPointerException -> {objectref}
        testPropagation(() -> {
                    synchronized (MultiTainter.taintedReference(null, "objectref")) {
                    }
                },
                NullPointerException.class, "objectref");
    }

    @Test
    public void testNewMultiArrayNASE() {
        // NegativeArraySizeException  -> {count1, counts...}
        testPropagation(() -> {
                    int[][] i = new int[MultiTainter.taintedInt(-1, "count1")][5];
                },
                NegativeArraySizeException.class, "count1");
    }

    private static void testPropagation(Runnable operation, Class<? extends Throwable> expectedException,
                                        Object... labels) {
        try {
            operation.run();
        } catch (Throwable t) {
            if (expectedException.equals(t.getClass())) {
                assertTaintHasOnlyLabels(MultiTainter.getTaint(t), labels);
                return;
            }
        }
        fail("Expected " + expectedException.getSimpleName() + " to be thrown");
    }

    private interface Example {
        @SuppressWarnings("UnusedReturnValue")
        int getValue();
    }

    private static final class Record {
        int i;
    }
}
