package edu.gmu.swe.phosphor.ignored.runtime;

import edu.columbia.cs.psl.phosphor.runtime.MultiTainter;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.TaintedPrimitiveWithObjTag;

import java.util.*;

@SuppressWarnings("unused")
public abstract class FlowBenchResult {

    FlowBenchResult() {

    }

    public abstract void startingRun(int numberOfEntities);

    public abstract void check(Set<?> expected, Set<?> predicted);

    public <T> void check$$PHOSPHORTAGGED(Taint<T> thisTag, Collection<?> expected, Taint<T> expectedTag, Object actualData, Taint<T> actualDataTag) {
        check(expected, Taint.combineTags(actualDataTag, getTaint(actualData)));
    }

    public <T> void checkEmpty$$PHOSPHORTAGGED(Taint<T> thisTag, Object actualData, Taint<T> actualDataTag) {
        checkEmpty(Taint.combineTags(actualDataTag, getTaint(actualData)));
    }

    public void check(Collection<?> expected, boolean actualData) {
        throw new IllegalStateException("Calling un-instrumented Phosphor stubs");
    }

    public void check(Collection<?> expected, byte actualData) {
        throw new IllegalStateException("Calling un-instrumented Phosphor stubs");
    }

    public void check(Collection<?> expected, char actualData) {
        throw new IllegalStateException("Calling un-instrumented Phosphor stubs");
    }

    public void check(Collection<?> expected, double actualData) {
        throw new IllegalStateException("Calling un-instrumented Phosphor stubs");
    }

    public void check(Collection<?> expected, float actualData) {
        throw new IllegalStateException("Calling un-instrumented Phosphor stubs");
    }

    public void check(Collection<?> expected, int actualData) {
        throw new IllegalStateException("Calling un-instrumented Phosphor stubs");
    }

    public void check(Collection<?> expected, long actualData) {
        throw new IllegalStateException("Calling un-instrumented Phosphor stubs");
    }

    public void check(Collection<?> expected, short actualData) {
        throw new IllegalStateException("Calling un-instrumented Phosphor stubs");
    }

    public void check$$PHOSPHORTAGGED(Taint<?> thisTag, Collection<?> expected, Taint<?> expectedTag, boolean actualData, Taint<?> actualDataTag) {
        check(expected, actualDataTag);
    }

    public void check$$PHOSPHORTAGGED(Taint<?> thisTag, Collection<?> expected, Taint<?> expectedTag, byte actualData, Taint<?> actualDataTag) {
        check(expected, actualDataTag);
    }

    public void check$$PHOSPHORTAGGED(Taint<?> thisTag, Collection<?> expected, Taint<?> expectedTag, char actualData, Taint<?> actualDataTag) {
        check(expected, actualDataTag);
    }

    public void check$$PHOSPHORTAGGED(Taint<?> thisTag, Collection<?> expected, Taint<?> expectedTag, double actualData, Taint<?> actualDataTag) {
        check(expected, actualDataTag);
    }

    public void check$$PHOSPHORTAGGED(Taint<?> thisTag, Collection<?> expected, Taint<?> expectedTag, float actualData, Taint<?> actualDataTag) {
        check(expected, actualDataTag);
    }

    public void check$$PHOSPHORTAGGED(Taint<?> thisTag, Collection<?> expected, Taint<?> expectedTag, int actualData, Taint<?> actualDataTag) {
        check(expected, actualDataTag);
    }

    public void check$$PHOSPHORTAGGED(Taint<?> thisTag, Collection<?> expected, Taint<?> expectedTag, long actualData, Taint<?> actualDataTag) {
        check(expected, actualDataTag);
    }

    public void check$$PHOSPHORTAGGED(Taint<?> thisTag, Collection<?> expected, Taint<?> expectedTag, short actualData, Taint<?> actualDataTag) {
        check(expected, actualDataTag);
    }

    public void check(Collection<?> expected, Object actualData) {
        Set<?> expectedSet = new HashSet<>(expected);
        Set<Object> predictedSet = new HashSet<>();
        Taint<?> taint = getTaint(actualData);
        if(taint != null && !taint.isEmpty()) {
            predictedSet.addAll(Arrays.asList(taint.getLabels()));
        }
        check(expectedSet, predictedSet);
    }

    public void checkEmpty(Object actualData) {
        check(Collections.emptyList(), actualData);
    }

    @SuppressWarnings("unchecked")
    <T> Taint<T> getTaint(Object actualData) {
        if(actualData == null) {
            return null;
        } else if(actualData instanceof Taint) {
            return (Taint<T>) actualData;
        } else if(actualData instanceof TaintedPrimitiveWithObjTag) {
            return ((TaintedPrimitiveWithObjTag) actualData).taint;
        }
        if(actualData instanceof String) {
            return Taint.combineTaintArray(MultiTainter.getStringCharTaints((String) actualData));
        } else {
            return MultiTainter.getMergedTaint(actualData);
        }
    }
}