package edu.neu.ccs.conflux.internal.policy.exception;


import edu.columbia.cs.psl.phosphor.control.ControlFlowStack;
import edu.columbia.cs.psl.phosphor.runtime.Taint;

public abstract class ExceptionalControlFlowStack<E> extends ControlFlowStack {

    private Taint<E> instructionExceptionTag = Taint.emptyTaint();

    public ExceptionalControlFlowStack(boolean disabled) {
        super(disabled);
    }

    public ExceptionalControlFlowStack(ExceptionalControlFlowStack<E> stack) {
        super(stack.isDisabled());
        this.instructionExceptionTag = stack.instructionExceptionTag;
    }

    public void setInstructionExceptionTag(Taint<E> instructionExceptionTag) {
        this.instructionExceptionTag = Taint.combineTags(instructionExceptionTag, copyTag());
    }

    /**
     * Called at the start of an exception handler.
     * Returns the taint tag for the handled exception.
     */
    public Taint<E> exceptionHandlerStart(Taint<E> handledExceptionTag) {
        return Taint.combineTags(instructionExceptionTag, handledExceptionTag);
    }

    @Override
    public void reset() {
        instructionExceptionTag = Taint.emptyTaint();
    }

    @Override
    public abstract Taint<E> copyTag();
}
