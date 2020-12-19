package edu.neu.ccs.conflux.internal.policy.strict;

import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.neu.ccs.conflux.internal.policy.basic.BasicControlFlowStack;

public class StrictControlFlowStack<E> extends BasicControlFlowStack<E> {

    @SuppressWarnings("rawtypes")
    private static final StrictControlFlowStack disabledInstance = new StrictControlFlowStack(true);
    private Taint<E> nextBranchTag = Taint.emptyTaint();

    public StrictControlFlowStack(boolean disabled) {
        super(disabled);
    }

    public StrictControlFlowStack(StrictControlFlowStack<E> other) {
        super(other);
    }

    @Override
    public StrictControlFlowStack<E> copyTop() {
        return new StrictControlFlowStack<>(this);
    }

    @Override
    public void reset() {
        super.reset();
        nextBranchTag = Taint.emptyTaint();
    }

    public void setNextBranchTag(Taint<E> nextBranchTag) {
        this.nextBranchTag = nextBranchTag;
    }

    public boolean[] push(boolean[] pushedBranches, int branchID, int maxSize) {
        if(!isDisabled()) {
            return super.push(nextBranchTag, pushedBranches, branchID, maxSize);
        } else {
            return pushedBranches;
        }
    }

    @SuppressWarnings("unchecked")
    public static <E> StrictControlFlowStack<E> factory(boolean disabled) {
        if(disabled) {
            return disabledInstance;
        } else {
            return new StrictControlFlowStack<>(false);
        }
    }
}
