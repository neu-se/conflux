package edu.neu.ccs.conflux.internal.policy.data;

import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.neu.ccs.conflux.internal.policy.exception.ExceptionalControlFlowStack;

/**
 * ControlFlowStack that can be used for propagating from an instruction's operands to any exception directly
 * thrown by the instruction.
 *
 * @param <E> the type of the labels used in the stack's taint tags
 */
public class ExceptionalControlFlowStackImpl<E> extends ExceptionalControlFlowStack<E> {

    @SuppressWarnings("rawtypes")
    private static final ExceptionalControlFlowStackImpl disabledInstance = new ExceptionalControlFlowStackImpl<>(true);

    public ExceptionalControlFlowStackImpl(boolean disabled) {
        super(disabled);
    }

    protected ExceptionalControlFlowStackImpl(ExceptionalControlFlowStackImpl<E> other) {
        super(other);
    }

    @Override
    public ExceptionalControlFlowStackImpl<E> copyTop() {
        return new ExceptionalControlFlowStackImpl<>(this);
    }

    @Override
    public void pushFrame() {

    }

    @Override
    public void popFrame() {

    }

    @Override
    public Taint<E> copyTag() {
        return Taint.emptyTaint();
    }

    @SuppressWarnings("unchecked")
    public static <E> ExceptionalControlFlowStackImpl<E> factory(boolean disabled) {
        if (disabled) {
            return disabledInstance;
        } else {
            return new ExceptionalControlFlowStackImpl<>(false);
        }
    }
}
