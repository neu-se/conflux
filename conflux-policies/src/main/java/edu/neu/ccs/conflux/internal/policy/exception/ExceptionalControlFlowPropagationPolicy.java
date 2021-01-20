package edu.neu.ccs.conflux.internal.policy.exception;

import edu.columbia.cs.psl.phosphor.PhosphorInstructionInfo;
import edu.columbia.cs.psl.phosphor.control.AbstractControlFlowPropagationPolicy;
import edu.columbia.cs.psl.phosphor.control.standard.ExceptionHandlerStart;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;

import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.NEW_EMPTY_TAINT;
import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public class ExceptionalControlFlowPropagationPolicy extends AbstractControlFlowPropagationPolicy<ExceptionMarkingAnalyzer> {

    public ExceptionalControlFlowPropagationPolicy(ExceptionMarkingAnalyzer marker) {
        super(marker);
    }

    @Override
    public void generateEmptyTaint() {
        NEW_EMPTY_TAINT.delegateVisit(delegate);
    }

    @Override
    public void visitingJump(int opcode, Label label) {
        switch (opcode) {
            case IFNULL:
            case IFNONNULL:
            case IFEQ:
            case IFNE:
            case IFGE:
            case IFGT:
            case IFLE:
            case IFLT:
                delegate.visitInsn(POP);
                break;
            case IF_ICMPEQ:
            case IF_ICMPLE:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGT:
            case IF_ICMPGE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
                delegate.visitInsn(POP);
                delegate.visitInsn(SWAP);
                delegate.visitInsn(POP);
                break;
        }
    }

    @Override
    public void visitTableSwitch(int min, int max, Label defaultLabel, Label[] labels) {
        delegate.visitInsn(POP); // Remove the taint tag
    }

    @Override
    public void visitLookupSwitch(Label defaultLabel, int[] keys, Label[] labels) {
        delegate.visitInsn(POP); // Remove the taint tag
    }

    @Override
    public void visitingPhosphorInstructionInfo(PhosphorInstructionInfo info) {
        if (info instanceof ExceptionHandlerStart) {
            delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
            delegate.visitInsn(SWAP);
            ExceptionalMethodRecord.EXCEPTION_HANDLER_START.delegateVisit(delegate);
        } else if (info instanceof MaybeThrownException) {
            ((MaybeThrownException) info).visit(delegate, localVariableManager, analyzer);
        }
    }
}
