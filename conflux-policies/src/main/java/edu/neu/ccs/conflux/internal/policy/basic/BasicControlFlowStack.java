package edu.neu.ccs.conflux.internal.policy.basic;

import edu.columbia.cs.psl.phosphor.control.ControlFlowStack;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.SinglyLinkedList;

public class BasicControlFlowStack<E> extends ControlFlowStack {

    @SuppressWarnings("rawtypes")
    private static final BasicControlFlowStack disabledInstance = new BasicControlFlowStack(true);
    private final SinglyLinkedList<Taint<E>> taintHistory = new SinglyLinkedList<>();

    public BasicControlFlowStack(boolean disabled) {
        super(disabled);
        taintHistory.push(Taint.emptyTaint()); // Starting taint is null/empty
    }

    protected BasicControlFlowStack(BasicControlFlowStack<E> other) {
        super(other.isDisabled());
        taintHistory.push(other.taintHistory.peek());
    }

    @Override
    public BasicControlFlowStack<E> copyTop() {
        return new BasicControlFlowStack<>(this);
    }

    @Override
    public void reset() {
        int size = taintHistory.size();
        taintHistory.clear();
        for(int i = 0; i < size; i++) {
            taintHistory.push(Taint.emptyTaint());
        }
    }

    @Override
    public void pushFrame() {

    }

    @Override
    public void popFrame() {

    }

    public final boolean[] push(Taint<E> tag, boolean[] pushedBranches, int branchID, int maxSize) {
        if(isDisabled() || tag == null || tag.isEmpty()) {
            return pushedBranches;
        }
        if(pushedBranches == null) {
            pushedBranches = new boolean[maxSize];
        }
        if(!pushedBranches[branchID]) {
            // Adding a label for this branch for the first time
            pushedBranches[branchID] = true;
            taintHistory.push(tag.union(taintHistory.peek()));

        } else {
            Taint<E> r = taintHistory.peek();
            if(r != tag && !r.isSuperset(tag)) {
                taintHistory.push(taintHistory.pop().union(tag));
            }
        }
        return pushedBranches;
    }

    public final void pop(boolean[] pushedBranches, int branchID) {
        if(pushedBranches != null) {
            if(pushedBranches[branchID]) {
                taintHistory.pop();
            }
            pushedBranches[branchID] = false;
        }
    }

    public final void pop(boolean[] pushedBranches) {
        if(pushedBranches != null) {
            for(int i = 0; i < pushedBranches.length; i++) {
                if(pushedBranches[i]) {
                    taintHistory.pop();
                    pushedBranches[i] = false;
                }
            }
        }
    }

    @Override
    public Taint<E> copyTag() {
        return isDisabled() ? Taint.emptyTaint() : taintHistory.peek();
    }

    @SuppressWarnings("unchecked")
    public static <E> BasicControlFlowStack<E> factory(boolean disabled) {
        if(disabled) {
            return disabledInstance;
        } else {
            return new BasicControlFlowStack<>(false);
        }
    }
}
