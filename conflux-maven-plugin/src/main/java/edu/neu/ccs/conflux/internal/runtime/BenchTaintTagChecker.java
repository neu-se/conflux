package edu.neu.ccs.conflux.internal.runtime;

import edu.columbia.cs.psl.phosphor.runtime.MultiTainter;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.LazyReferenceArrayObjTags;
import edu.columbia.cs.psl.phosphor.struct.TaintedPrimitiveWithObjTag;
import edu.neu.ccs.conflux.internal.BenchRunResult;

import java.util.*;

public class BenchTaintTagChecker {

    private int truePositives = 0;
    private int falsePositives = 0;
    private int falseNegatives = 0;

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

    public void check$$PHOSPHORTAGGED(Taint<?> thisTag, Collection<?> expected, Taint<?> expectedTag, boolean actualData,
                                      Taint<?> actualDataTag) {
        check(expected, actualDataTag);
    }

    public void check$$PHOSPHORTAGGED(Taint<?> thisTag, Collection<?> expected, Taint<?> expectedTag, byte actualData,
                                      Taint<?> actualDataTag) {
        check(expected, actualDataTag);
    }

    public void check$$PHOSPHORTAGGED(Taint<?> thisTag, Collection<?> expected, Taint<?> expectedTag, char actualData,
                                      Taint<?> actualDataTag) {
        check(expected, actualDataTag);
    }

    public void check$$PHOSPHORTAGGED(Taint<?> thisTag, Collection<?> expected, Taint<?> expectedTag, double actualData,
                                      Taint<?> actualDataTag) {
        check(expected, actualDataTag);
    }

    public void check$$PHOSPHORTAGGED(Taint<?> thisTag, Collection<?> expected, Taint<?> expectedTag, float actualData,
                                      Taint<?> actualDataTag) {
        check(expected, actualDataTag);
    }

    public void check$$PHOSPHORTAGGED(Taint<?> thisTag, Collection<?> expected, Taint<?> expectedTag, int actualData,
                                      Taint<?> actualDataTag) {
        check(expected, actualDataTag);
    }

    public void check$$PHOSPHORTAGGED(Taint<?> thisTag, Collection<?> expected, Taint<?> expectedTag, long actualData,
                                      Taint<?> actualDataTag) {
        check(expected, actualDataTag);
    }

    public void check$$PHOSPHORTAGGED(Taint<?> thisTag, Collection<?> expected, Taint<?> expectedTag, short actualData,
                                      Taint<?> actualDataTag) {
        check(expected, actualDataTag);
    }

    public <T> void check$$PHOSPHORTAGGED(Taint<T> thisTag, Collection<?> expected, Taint<T> expectedTag,
                                          Object actualData, Taint<T> actualDataTag) {
        check(expected, Taint.combineTags(actualDataTag, getTaint(actualData)));
    }

    public <T> void checkEmpty$$PHOSPHORTAGGED(Taint<T> thisTag, Object actualData, Taint<T> actualDataTag) {
        checkEmpty(Taint.combineTags(actualDataTag, getTaint(actualData)));
    }

    public void check(Collection<?> expected, Object actualData) {
        java.util.Set<?> expectedSet = new HashSet<>(expected);
        java.util.Set<Object> predictedSet = new HashSet<>();
        Taint<?> taint = getTaint(actualData);
        if (taint != null && !taint.isEmpty()) {
            predictedSet.addAll(Arrays.asList(taint.getLabels()));
        }
        check(expectedSet, predictedSet);
    }

    public void checkEmpty(Object actualData) {
        check(Collections.emptyList(), actualData);
    }

    @SuppressWarnings("unchecked")
    private <T> Taint<T> getTaint(Object actualData) {
        if (actualData == null) {
            return null;
        } else if (actualData instanceof Taint) {
            return (Taint<T>) actualData;
        } else if (actualData instanceof TaintedPrimitiveWithObjTag) {
            return ((TaintedPrimitiveWithObjTag) actualData).taint;
        }
        if (actualData instanceof String) {
            return Taint.combineTaintArray(MultiTainter.getStringCharTaints((String) actualData));
        } else if (actualData instanceof LazyReferenceArrayObjTags) {
            return Taint.combineTags(MultiTainter.getMergedTaint(actualData),
                    Taint.combineTaintArray(((LazyReferenceArrayObjTags) actualData).taints));
        } else {
            return MultiTainter.getMergedTaint(actualData);
        }
    }

    public void check(Set<?> expected, Set<?> predicted) {
        for (Object label : expected) {
            if (predicted.contains(label)) {
                truePositives++;
            } else {
                falseNegatives++;
            }
        }
        for (Object label : predicted) {
            if (!expected.contains(label)) {
                falsePositives++;
            }
        }
    }

    public BenchRunResult toRunResult() {
        return new BenchRunResult(truePositives, falsePositives, falseNegatives);
    }
}
