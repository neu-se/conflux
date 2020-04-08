package edu.gmu.swe.phosphor.ignored.control.strict;

import edu.columbia.cs.psl.phosphor.PhosphorInstructionInfo;
import edu.columbia.cs.psl.phosphor.control.AbstractControlFlowPropagationPolicy;
import edu.columbia.cs.psl.phosphor.control.LocalVariable;
import edu.columbia.cs.psl.phosphor.control.standard.BranchEnd;
import edu.columbia.cs.psl.phosphor.control.standard.BranchStart;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.MethodVisitor;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;

import static edu.columbia.cs.psl.phosphor.control.ControlFlowPropagationPolicy.push;
import static edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord.COMBINE_TAGS;
import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;
import static edu.gmu.swe.phosphor.ignored.control.basic.BasicMethodRecord.*;
import static edu.gmu.swe.phosphor.ignored.control.strict.StrictMethodRecord.STRICT_CONTROL_STACK_PUSH;
import static edu.gmu.swe.phosphor.ignored.control.strict.StrictMethodRecord.STRICT_CONTROL_STACK_SET_NEXT_BRANCH_TAG;

public class StrictControlFlowPropagationPolicy extends AbstractControlFlowPropagationPolicy<StrictControlFlowAnalyzer> {

    /**
     * The number of unique IDs assigned to branches in the method
     */
    private int numberOfUniqueBranchIDs = 0;

    /**
     * The local variable index of the boolean[] instance for storing pushed branches or -1 if not created
     */
    private int pushedBranchesIndex = -1;

    /**
     * Array containing the the local variables created by this policy
     */
    private LocalVariable[] createdLocalVariables = new LocalVariable[0];

    public StrictControlFlowPropagationPolicy(StrictControlFlowAnalyzer flowAnalyzer) {
        super(flowAnalyzer);
    }

    @Override
    public void onMethodExit(int opcode) {
        switch(opcode) {
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
                copyTag();
                COMBINE_TAGS.delegateVisit(delegate);
        }
    }

    @Override
    public void poppingFrame(MethodVisitor mv) {
        if(pushedBranchesIndex != -1) {
            mv.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
            mv.visitVarInsn(ALOAD, pushedBranchesIndex);
            BASIC_CONTROL_STACK_POP_ALL.delegateVisit(mv);
        }
    }

    @Override
    public LocalVariable[] createdLocalVariables() {
        return createdLocalVariables;
    }

    @Override
    public void initializeLocalVariables(MethodVisitor mv) {
        numberOfUniqueBranchIDs = flowAnalyzer.getNumberOfUniqueBranchIDs();
        if(numberOfUniqueBranchIDs > 0) {
            // Create a local variable for the array used to track tags pushed for each "branch" location
            mv.visitInsn(Opcodes.ACONST_NULL);
            pushedBranchesIndex = localVariableManager.createPermanentLocalVariable(boolean[].class, "pushedBranches");
            mv.visitVarInsn(Opcodes.ASTORE, pushedBranchesIndex);
            LocalVariable lv = new LocalVariable(pushedBranchesIndex, Type.getInternalName(boolean[].class));
            createdLocalVariables = new LocalVariable[]{lv};
        } else {
            createdLocalVariables = new LocalVariable[0];
        }
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
        switch(opcode) {
            case ISTORE:
            case FSTORE:
            case DSTORE:
            case LSTORE:
            case ASTORE:
                copyTag();
                COMBINE_TAGS.delegateVisit(delegate);
        }
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

    @Override
    public void visitingPhosphorInstructionInfo(PhosphorInstructionInfo info) {
        if(info instanceof BranchStart) {
            delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
            delegate.visitVarInsn(ALOAD, pushedBranchesIndex);
            push(delegate, ((BranchStart) info).getBranchID());
            push(delegate, numberOfUniqueBranchIDs);
            // control-stack [Z I I
            STRICT_CONTROL_STACK_PUSH.delegateVisit(delegate);
            delegate.visitVarInsn(ASTORE, pushedBranchesIndex);
        } else if(info instanceof BranchEnd) {
            if(pushedBranchesIndex != -1) {
                delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
                delegate.visitVarInsn(ALOAD, pushedBranchesIndex);
                push(delegate, ((BranchEnd) info).getBranchID());
                // control-stack [Z I
                BASIC_CONTROL_STACK_POP.delegateVisit(delegate);
            }
        }
    }

    private void setNextBranchTag() {
        // taint
        delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
        delegate.visitInsn(SWAP);
        // control-stack taint
        STRICT_CONTROL_STACK_SET_NEXT_BRANCH_TAG.delegateVisit(delegate);
    }

    private void copyTag() {
        delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
        CONTROL_STACK_COPY_TAG.delegateVisit(delegate);
    }
}
