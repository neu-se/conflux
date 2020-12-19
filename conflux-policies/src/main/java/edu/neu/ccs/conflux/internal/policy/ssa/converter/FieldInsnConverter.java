package edu.neu.ccs.conflux.internal.policy.ssa.converter;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.FieldInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Value;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.FieldAccess;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.StackElement;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.AssignmentStatement;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.Statement;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public class FieldInsnConverter extends InsnConverter {

    FieldInsnConverter(InsnConverter next) {
        super(next);
    }

    @Override
    protected boolean canProcess(AbstractInsnNode insn) {
        switch(insn.getOpcode()) {
            case GETSTATIC:
            case PUTSTATIC:
            case GETFIELD:
            case PUTFIELD:
                return true;
            default:
                return false;
        }
    }

    @Override
    protected Statement[] process(AbstractInsnNode insn, Frame<? extends Value> frame) {
        String owner = ((FieldInsnNode) insn).owner;
        String name = ((FieldInsnNode) insn).name;
        String desc = ((FieldInsnNode) insn).desc;
        FieldAccess expr;
        StackElement zero = new StackElement(frame.getStackSize());
        StackElement first = new StackElement(frame.getStackSize() - 1);
        StackElement second = new StackElement(frame.getStackSize() - 2);
        switch(insn.getOpcode()) {
            case GETSTATIC:
                expr = new FieldAccess(owner, name, desc, null);
                return new Statement[]{new AssignmentStatement(zero, expr)};
            case PUTSTATIC:
                expr = new FieldAccess(owner, name, desc, null);
                return new Statement[]{new AssignmentStatement(expr, first)};
            case GETFIELD:
                expr = new FieldAccess(owner, name, desc, first);
                return new Statement[]{new AssignmentStatement(first, expr)};
            case PUTFIELD:
                expr = new FieldAccess(owner, name, desc, second);
                return new Statement[]{new AssignmentStatement(expr, first)};
            default:
                throw new IllegalArgumentException();
        }
    }
}
