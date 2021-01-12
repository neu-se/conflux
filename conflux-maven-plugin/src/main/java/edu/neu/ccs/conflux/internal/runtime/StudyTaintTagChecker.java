package edu.neu.ccs.conflux.internal.runtime;

import edu.columbia.cs.psl.phosphor.runtime.MultiTainter;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.LazyReferenceArrayObjTags;
import edu.columbia.cs.psl.phosphor.struct.TaintedPrimitiveWithObjTag;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Arrays;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import edu.neu.ccs.conflux.internal.StudyRunResult;


public class StudyTaintTagChecker {

    Set<Object> predicted = new HashSet<>();
    String input = null;

    @SuppressWarnings("unused")
    public <T> void check$$PHOSPHORTAGGED(Taint<T> thisTag, Object actualData, Taint<T> actualDataTag) {
        check(Taint.combineTags(actualDataTag, getTaint(actualData)));
    }

    @SuppressWarnings("unused")
    public <T> void recordInput$$PHOSPHORTAGGED(Taint<T> thisTag, String input, Taint<T> inputTag) {
        recordInput(input);
    }

    public void recordInput(String input) {
        if (input == null) {
            throw new NullPointerException();
        }
        this.input = input;
    }

    public void check(Object actualData) {
        Set<Object> predictedSet = new HashSet<>();
        Taint<?> taint = getTaint(actualData);
        if (taint != null && !taint.isEmpty()) {
            predictedSet.addAll(Arrays.asList(taint.getLabels()));
        }
        predicted.addAll(predictedSet);
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

    public StudyRunResult toRunResult() {
        return new StudyRunResult(input, predicted);
    }

    public boolean inputRecorded() {
        return input != null;
    }
}
