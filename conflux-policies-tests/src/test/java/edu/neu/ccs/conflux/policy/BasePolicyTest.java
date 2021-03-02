package edu.neu.ccs.conflux.policy;

import edu.columbia.cs.psl.phosphor.PreMain;
import edu.columbia.cs.psl.phosphor.runtime.MultiTainter;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class BasePolicyTest {
    @Before
    public void clearErrorFlag() {
        PreMain.INSTRUMENTATION_EXCEPTION_OCCURRED = false;
    }

    @After
    public void checkForError() {
        if (PreMain.INSTRUMENTATION_EXCEPTION_OCCURRED) {
            Assert.fail("Instrumentation error occurred");
        }
    }

    public static void assertNullOrEmpty(Taint<?> taint) {
        if (taint != null && !taint.isEmpty()) {
            fail("Expected null taint. Got: " + taint);
        }
    }

    public static void assertNoTaint(String obj) {
        Taint<?> taint = MultiTainter.getTaint(obj.toCharArray()[0]);
        if (taint != null && !taint.isEmpty()) {
            fail("Expected null taint. Got: " + taint);
        }
    }

    public static void assertNonNullTaint(Object obj) {
        Taint<?> t = MultiTainter.getTaint(obj);
        assertNotNull(obj);
        if (t == null || t.isEmpty()) {
            fail("Expected non-null taint - got: " + t);
        }
    }

    public static void assertNonNullTaint(Taint<?> obj) {
        assertNotNull(obj);
        if (obj.isEmpty()) {
            fail("Expected non-null taint - got: " + obj);
        }
    }

    public static void assertTaintHasLabel(Taint<?> obj, Object lbl) {
        assertNotNull(obj);
        if (!obj.containsLabel(lbl)) {
            fail("Expected taint contained " + lbl + ", has " + obj);
        }
    }

    public static void assertTaintHasOnlyLabel(Taint<?> obj, Object lbl) {
        assertNotNull(obj);
        if (!obj.containsOnlyLabels(new Object[]{lbl})) {
            fail("Expected taint contained ONLY " + lbl + ", found " + obj);
        }

    }

    public static void assertTaintHasOnlyLabels(Taint<?> obj, Object... lbl) {
        assertNotNull(obj);
        if (!obj.containsOnlyLabels(lbl)) {
            fail("Expected taint contained ONLY " + Arrays.toString(lbl) + ", found " + obj);
        }
    }

    public static void taintWithIndices(int[] a) {
        for (int i = 0; i < a.length; i++) {
            a[i] = MultiTainter.taintedInt(a[i], i);
        }
    }

    public static String taintWithIndices(String input) {
        return taintWithIndices(input, 0, input.length());
    }

    public static String taintWithIndices(String input, int start, int len) {
        char[] c = input.toCharArray();
        for (int i = start; i < len; i++) {
            c[i] = MultiTainter.taintedChar(c[i], i);
        }
        return new String(c);
    }
}
