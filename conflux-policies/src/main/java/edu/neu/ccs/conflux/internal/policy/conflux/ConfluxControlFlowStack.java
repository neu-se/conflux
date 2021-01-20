package edu.neu.ccs.conflux.internal.policy.conflux;

import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.neu.ccs.conflux.internal.policy.exception.ExceptionalControlFlowStack;

public final class ConfluxControlFlowStack<E> extends ExceptionalControlFlowStack<E> {

    @SuppressWarnings("rawtypes")
    private static final ConfluxControlFlowStack disabledInstance = new ConfluxControlFlowStack(true);
    private ControlFrame<E> stackTop;
    private final ControlFrameBuilder<E> frameBuilder;
    private Taint<E> nextBranchTag;

    public ConfluxControlFlowStack() {
        this(false);
    }

    public ConfluxControlFlowStack(boolean disabled) {
        super(disabled);
        stackTop = new ControlFrame<>(0, null, null);
        frameBuilder = new ControlFrameBuilder<>();
        nextBranchTag = Taint.emptyTaint();
    }

    private ConfluxControlFlowStack(ConfluxControlFlowStack<E> stack) {
        super(stack);
        stackTop = new ControlFrame<>(stack.stackTop);
        frameBuilder = stack.frameBuilder.copy();
        nextBranchTag = Taint.emptyTaint();
    }

    @Override
    public ConfluxControlFlowStack<E> copyTop() {
        return new ConfluxControlFlowStack<>(this);
    }

    @Override
    public void enteringUninstrumentedWrapper() {
        frameBuilder.reset();
    }

    public ConfluxControlFlowStack<E> startFrame(int invocationLevel, int numArguments) {
        frameBuilder.start(invocationLevel, numArguments);
        return this;
    }

    public ConfluxControlFlowStack<E> setNextFrameArgStable() {
        frameBuilder.setNextArgLevel(0);
        return this;
    }

    public ConfluxControlFlowStack<E> setNextFrameArgDependent(int[] dependencies) {
        frameBuilder.setNextArgLevel(getLevel(dependencies));
        return this;
    }

    public ConfluxControlFlowStack<E> setNextFrameArgUnstable(int levelOffset) {
        frameBuilder.setNextArgLevel(getLevel(levelOffset));
        return this;
    }

    @Override
    public void pushFrame() {
        stackTop = frameBuilder.build(stackTop);
    }

    public int getLevel(int levelOffset) {
        return stackTop.getLevel(levelOffset);
    }

    public int getLevel(int[] dependencies) {
        return stackTop.getLevel(dependencies);
    }

    @Override
    public void popFrame() {
        stackTop = stackTop.getNext();
    }

    public Taint<E> copyTagStable() {
        return isDisabled() ? Taint.emptyTaint() : stackTop.copyTag(0);
    }

    public Taint<E> copyTagDependent(int[] dependencies) {
        return isDisabled() ? Taint.emptyTaint() : stackTop.copyTag(getLevel(dependencies));
    }

    public Taint<E> copyTagUnstable(int levelOffset) {
        return isDisabled() ? Taint.emptyTaint() : stackTop.copyTag(getLevel(levelOffset));
    }

    public void pushStable(int branchID, int branchesSize) {
        if(!isDisabled()) {
            stackTop.push(nextBranchTag, branchID, branchesSize, 0);
        }
    }

    public void pushDependent(int branchID, int branchesSize, int[] dependencies) {
        if(!isDisabled()) {
            stackTop.push(nextBranchTag, branchID, branchesSize, getLevel(dependencies));
        }
    }

    public void pushUnstable(int branchID, int branchesSize, int levelOffset) {
        if(!isDisabled()) {
            stackTop.push(nextBranchTag, branchID, branchesSize, getLevel(levelOffset));
        }
    }

    public void pop(int branchID) {
        stackTop.pop(branchID);
    }

    @Override
    public void reset() {
        super.reset();
        stackTop.reset();
        nextBranchTag = Taint.emptyTaint();
    }

    public void exitLoopLevel(int levelOffset) {
        stackTop.exitLoopLevel(levelOffset);
    }

    public void setNextBranchTag(Taint<E> nextBranchTag) {
        this.nextBranchTag = nextBranchTag;
    }

    @Override
    public Taint<E> copyTag() {
        return copyTagStable();
    }

    @SuppressWarnings("unchecked")
    public static <E> ConfluxControlFlowStack<E> factory(boolean disabled) {
        if (disabled) {
            return disabledInstance;
        } else {
            return new ConfluxControlFlowStack<>(false);
        }
    }
}
