package edu.neu.ccs.conflux.internal.policy.conflux;

import edu.columbia.cs.psl.phosphor.PhosphorInstructionInfo;
import edu.columbia.cs.psl.phosphor.control.AbstractControlFlowPropagationPolicy;
import edu.columbia.cs.psl.phosphor.control.ControlFlowPropagationPolicy;
import edu.columbia.cs.psl.phosphor.control.standard.BranchEnd;
import edu.columbia.cs.psl.phosphor.control.standard.ExceptionHandlerStart;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.MethodVisitor;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes;
import edu.neu.ccs.conflux.internal.policy.exception.MaybeThrownException;

import java.util.Iterator;

import static edu.columbia.cs.psl.phosphor.control.ControlFlowPropagationPolicy.push;
import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.COMBINE_TAGS;
import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;
import static edu.neu.ccs.conflux.internal.policy.exception.ExceptionTrackingMethodRecord.EXCEPTION_HANDLER_START;

public class ConfluxControlFlowPropagationPolicy extends AbstractControlFlowPropagationPolicy<ConfluxControlFlowAnalyzer> {

    private static final LoopLevel defaultLevel = LoopLevel.StableLoopLevel.STABLE_LOOP_LEVEL;

    /**
     * The number of unique IDs assigned to branches in the method
     */
    private int numberOfUniqueBranchIDs = 0;

    /**
     * The loop level information associated with the next instruction to be visited or null if no information
     * is associated with the instruction or there are no remaining instructions to be visited
     */
    private LoopLevel nextCopyTagInfo = defaultLevel;

    private FrameLoopStabilityInfo nextMethodFrameInfo = null;

    public ConfluxControlFlowPropagationPolicy(ConfluxControlFlowAnalyzer flowAnalyzer) {
        super(flowAnalyzer);
    }

    @Override
    public void onMethodExit(int opcode) {
        if(opcode != RETURN) {
            copyTag();
            COMBINE_TAGS.delegateVisit(delegate);
        }
    }

    @Override
    public void initializeLocalVariables(MethodVisitor mv) {
        numberOfUniqueBranchIDs = flowAnalyzer.getNumberOfUniqueBranchIDs();
    }

    @Override
    public void preparingFrame() {
        if(nextMethodFrameInfo != null) {
            // Start the frame and set the argument levels
            ControlFlowPropagationPolicy.push(delegate, nextMethodFrameInfo.getInvocationLevel());
            ControlFlowPropagationPolicy.push(delegate, nextMethodFrameInfo.getNumArguments());
            ConfluxMethodRecord.CONFLUX_CONTROL_STACK_START_FRAME.delegateVisit(delegate);
            Iterator<LoopLevel> argLevels = nextMethodFrameInfo.getLevelIterator();
            while(argLevels.hasNext()) {
                argLevels.next().setArgument(delegate);
            }
        }
        nextMethodFrameInfo = null;
    }

    @Override
    public void visitingIncrement(int var, int shadowVar) {
        delegate.visitVarInsn(ALOAD, shadowVar); // Current tag
        copyTag();
        COMBINE_TAGS.delegateVisit(delegate);
        delegate.visitVarInsn(ASTORE, shadowVar);
    }

    @Override
    public void visitingLocalVariableStore(int opcode, int var) {
        copyTag();
        COMBINE_TAGS.delegateVisit(delegate);
    }

    @Override
    public void visitingArrayStore(int opcode) {
        copyTag();
        COMBINE_TAGS.delegateVisit(delegate);
    }

    @Override
    public void visitingArrayLoad(int opcode) {
        delegate.visitInsn(DUP2);
        COMBINE_TAGS.delegateVisit(delegate);
        delegate.visitInsn(SWAP);
        delegate.visitInsn(POP);
    }

    @Override
    public void visitingFieldStore(int opcode, String owner, String name, String descriptor) {
        copyTag();
        COMBINE_TAGS.delegateVisit(delegate);
    }

    @Override
    public void visitingInstanceFieldLoad(String owner, String name, String descriptor) {
        delegate.visitInsn(DUP2);
        COMBINE_TAGS.delegateVisit(delegate);
        delegate.visitInsn(SWAP);
        delegate.visitInsn(POP);
    }

    @Override
    public void generateEmptyTaint() {
        copyTag();
    }

    private void copyTag() {
        delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
        nextCopyTagInfo.copyTag(delegate);
        nextCopyTagInfo = defaultLevel;
    }

    @Override
    public void visitingJump(int opcode, Label label) {
        switch(opcode) {
            case Opcodes.IF_ICMPEQ:
            case Opcodes.IF_ICMPNE:
            case Opcodes.IF_ICMPLT:
            case Opcodes.IF_ICMPGE:
            case Opcodes.IF_ICMPGT:
            case Opcodes.IF_ICMPLE:
            case Opcodes.IF_ACMPNE:
            case Opcodes.IF_ACMPEQ:
                // v1 t1 v2 t2
                delegate.visitInsn(DUP2_X1);
                // v1 v2 t2 t1 v2 t2
                delegate.visitInsn(POP2);
                // v1 v2 t2 t1
                COMBINE_TAGS.delegateVisit(delegate);
                // v1 v2 t
            case Opcodes.IFEQ:
            case Opcodes.IFNE:
            case Opcodes.IFLT:
            case Opcodes.IFGE:
            case Opcodes.IFGT:
            case Opcodes.IFLE:
            case Opcodes.IFNULL:
            case Opcodes.IFNONNULL:
                // t
                setNextBranchTag();
        }
    }

    @Override
    public void visitTableSwitch(int min, int max, Label defaultLabel, Label[] labels) {
        setNextBranchTag();
    }

    @Override
    public void visitLookupSwitch(Label defaultLabel, int[] keys, Label[] labels) {
        setNextBranchTag();
    }

    /**
     * stack_pre = [taint]
     * stack_post = []
     */
    private void setNextBranchTag() {
        delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
        delegate.visitInsn(SWAP);
        ConfluxMethodRecord.CONFLUX_CONTROL_STACK_SET_NEXT_BRANCH_TAG.delegateVisit(delegate);
    }

    @Override
    public void visitingPhosphorInstructionInfo(PhosphorInstructionInfo info) {
        if (info instanceof ExitLoopLevelInfo) {
            delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
            push(delegate, ((ExitLoopLevelInfo) info).getLevelOffset());
            ConfluxMethodRecord.CONFLUX_CONTROL_STACK_EXIT_LOOP_LEVEL.delegateVisit(delegate);
        } else if (info instanceof BranchEnd) {
            delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
            push(delegate, ((BranchEnd) info).getBranchID());
            ConfluxMethodRecord.CONFLUX_CONTROL_STACK_POP.delegateVisit(delegate);
        } else if (info instanceof ConfluxBranchStart) {
            delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
            push(delegate, ((ConfluxBranchStart) info).getBranchID());
            push(delegate, numberOfUniqueBranchIDs);
            ((ConfluxBranchStart) info).getLevel().pushTag(delegate);
        } else if (info instanceof CopyTagInfo) {
            nextCopyTagInfo = ((CopyTagInfo) info).getLevel();
        } else if (info instanceof FrameLoopStabilityInfo) {
            nextMethodFrameInfo = (FrameLoopStabilityInfo) info;
        } else if (info instanceof ExceptionHandlerStart) {
            delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
            delegate.visitInsn(SWAP);
            EXCEPTION_HANDLER_START.delegateVisit(delegate);
        } else if (info instanceof MaybeThrownException) {
            ((MaybeThrownException) info).visit(delegate, localVariableManager, analyzer);
        }
    }

    @Override
    public void visitingInstanceOf() {
        delegate.visitInsn(POP);
        copyTag();
    }
}
