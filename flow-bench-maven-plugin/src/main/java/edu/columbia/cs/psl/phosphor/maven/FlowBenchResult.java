package edu.columbia.cs.psl.phosphor.maven;

import edu.columbia.cs.psl.phosphor.runtime.MultiTainter;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.ControlTaintTagStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class FlowBenchResult {


    public abstract void check(Set<?> expected, Set<?> predicted);

    @SuppressWarnings("unused")
    public void check$$PHOSPHORTAGGED(Collection<?> expected, Object actualData, ControlTaintTagStack ctrl) {
        check(expected, actualData);
    }

    public void check(Collection<?> expected, Object actualData) {
        Set<?> expectedSet = new HashSet<>(expected);
        Set<Object> predictedSet = new HashSet<>();
        if(actualData != null) {
            Taint taint = MultiTainter.getTaint(actualData);
            if(taint != null && !taint.isEmpty()) {
                predictedSet.addAll(Arrays.asList(taint.getLabels()));
            }
        }
        check(expectedSet, predictedSet);
    }
}