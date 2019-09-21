package edu.gmu.swe.phosphor.ignored.runtime;

import edu.columbia.cs.psl.phosphor.runtime.MultiTainter;
import edu.columbia.cs.psl.phosphor.runtime.Taint;

import java.util.*;

public abstract class FlowBenchResult {

    public abstract String getBenchmarkTypeDesc();

    public abstract void check(Set<?> expected, Set<?> predicted);

    @SuppressWarnings("unused")
    public void check$$PHOSPHORTAGGED(Collection<?> expected, Object actualData) {
        check(expected, actualData);
    }

    @SuppressWarnings("unused")
    public void checkEmpty$$PHOSPHORTAGGED(Object actualData) {
        checkEmpty(actualData);
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

    public void checkEmpty(Object actualData) {
        check(Collections.emptyList(), actualData);
    }
}