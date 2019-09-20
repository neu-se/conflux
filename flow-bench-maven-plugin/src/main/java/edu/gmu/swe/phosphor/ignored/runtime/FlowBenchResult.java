package edu.gmu.swe.phosphor.ignored.runtime;

import edu.columbia.cs.psl.phosphor.runtime.MultiTainter;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class FlowBenchResult {

    public abstract String getBenchmarkTypeDesc();

    public abstract void check(Set<?> expected, Set<?> predicted);

    @SuppressWarnings("unused")
    public void check(Collection<?> expected, boolean[] actualData) {
        throw new IllegalStateException("Calling un-instrumented Phosphor stub.");
    }

    @SuppressWarnings("unused")
    public void check(Collection<?> expected, byte[] actualData) {
        throw new IllegalStateException("Calling un-instrumented Phosphor stub.");
    }

    @SuppressWarnings("unused")
    public void check(Collection<?> expected, char[] actualData) {
        throw new IllegalStateException("Calling un-instrumented Phosphor stub.");
    }

    @SuppressWarnings("unused")
    public void check(Collection<?> expected, double[] actualData) {
        throw new IllegalStateException("Calling un-instrumented Phosphor stub.");
    }

    @SuppressWarnings("unused")
    public void check(Collection<?> expected, float[] actualData) {
        throw new IllegalStateException("Calling un-instrumented Phosphor stub.");
    }

    @SuppressWarnings("unused")
    public void check(Collection<?> expected, int[] actualData) {
        throw new IllegalStateException("Calling un-instrumented Phosphor stub.");
    }

    @SuppressWarnings("unused")
    public void check(Collection<?> expected, long[] actualData) {
        throw new IllegalStateException("Calling un-instrumented Phosphor stub.");
    }

    @SuppressWarnings("unused")
    public void check(Collection<?> expected, short[] actualData) {
        throw new IllegalStateException("Calling un-instrumented Phosphor stub.");
    }

    @SuppressWarnings("unused")
    public void check$$PHOSPHORTAGGED(Collection<?> expected, LazyBooleanArrayObjTags tags, boolean[] actualData, ControlTaintTagStack ctrl) {
        check(expected, tags);
    }

    @SuppressWarnings("unused")
    public void check$$PHOSPHORTAGGED(Collection<?> expected, LazyByteArrayObjTags tags, byte[] actualData, ControlTaintTagStack ctrl) {
        check(expected, tags);
    }

    @SuppressWarnings("unused")
    public void check$$PHOSPHORTAGGED(Collection<?> expected, LazyCharArrayObjTags tags, char[] actualData, ControlTaintTagStack ctrl) {
        check(expected, tags);
    }

    @SuppressWarnings("unused")
    public void check$$PHOSPHORTAGGED(Collection<?> expected, LazyDoubleArrayObjTags tags, double[] actualData, ControlTaintTagStack ctrl) {
        check(expected, tags);
    }

    @SuppressWarnings("unused")
    public void check$$PHOSPHORTAGGED(Collection<?> expected, LazyFloatArrayObjTags tags, float[] actualData, ControlTaintTagStack ctrl) {
        check(expected, tags);
    }

    @SuppressWarnings("unused")
    public void check$$PHOSPHORTAGGED(Collection<?> expected, LazyIntArrayObjTags tags, int[] actualData, ControlTaintTagStack ctrl) {
        check(expected, tags);
    }

    @SuppressWarnings("unused")
    public void check$$PHOSPHORTAGGED(Collection<?> expected, LazyLongArrayObjTags tags, long[] actualData, ControlTaintTagStack ctrl) {
        check(expected, tags);
    }

    @SuppressWarnings("unused")
    public void check$$PHOSPHORTAGGED(Collection<?> expected, LazyShortArrayObjTags tags, short[] actualData, ControlTaintTagStack ctrl) {
        check(expected, tags);
    }

    @SuppressWarnings("unused")
    public void check$$PHOSPHORTAGGED(Collection<?> expected, LazyBooleanArrayObjTags tags, boolean[] actualData) {
        check(expected, tags);
    }

    @SuppressWarnings("unused")
    public void check$$PHOSPHORTAGGED(Collection<?> expected, LazyByteArrayObjTags tags, byte[] actualData) {
        check(expected, tags);
    }

    @SuppressWarnings("unused")
    public void check$$PHOSPHORTAGGED(Collection<?> expected, LazyCharArrayObjTags tags, char[] actualData) {
        check(expected, tags);
    }

    @SuppressWarnings("unused")
    public void check$$PHOSPHORTAGGED(Collection<?> expected, LazyDoubleArrayObjTags tags, double[] actualData) {
        check(expected, tags);
    }

    @SuppressWarnings("unused")
    public void check$$PHOSPHORTAGGED(Collection<?> expected, LazyFloatArrayObjTags tags, float[] actualData) {
        check(expected, tags);
    }

    @SuppressWarnings("unused")
    public void check$$PHOSPHORTAGGED(Collection<?> expected, LazyIntArrayObjTags tags, int[] actualData) {
        check(expected, tags);
    }

    @SuppressWarnings("unused")
    public void check$$PHOSPHORTAGGED(Collection<?> expected, LazyLongArrayObjTags tags, long[] actualData) {
        check(expected, tags);
    }

    @SuppressWarnings("unused")
    public void check$$PHOSPHORTAGGED(Collection<?> expected, LazyShortArrayObjTags tags, short[] actualData) {
        check(expected, tags);
    }

    @SuppressWarnings("unused")
    public void check$$PHOSPHORTAGGED(Collection<?> expected, Object actualData, ControlTaintTagStack ctrl) {
        check(expected, actualData);
    }

    @SuppressWarnings("unused")
    public void check$$PHOSPHORTAGGED(Collection<?> expected, Object actualData) {
        check(expected, actualData);
    }

    public void check(Collection<?> expected, Object actualData) {
        Set<?> expectedSet = new HashSet<>(expected);
        Set<Object> predictedSet = new HashSet<>();
        if(actualData != null) {
            Taint taint = actualData instanceof Taint ? (Taint) actualData : MultiTainter.getMergedTaint(actualData);
            if(taint != null && !taint.isEmpty()) {
                predictedSet.addAll(Arrays.asList(taint.getLabels()));
            }
        }
        check(expectedSet, predictedSet);
    }
}