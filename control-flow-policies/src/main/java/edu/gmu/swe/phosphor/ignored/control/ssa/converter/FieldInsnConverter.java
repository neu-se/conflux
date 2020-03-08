package edu.gmu.swe.phosphor.ignored.control.ssa.converter;

import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.FieldInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.FieldAccess;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.StackElement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.AssignmentStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

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
    protected Statement[] process(AbstractInsnNode insn, Frame<TypeValue> frame) {
        String owner = ((FieldInsnNode) insn).owner;
        String name = ((FieldInsnNode) insn).name;
        FieldAccess expr;
        StackElement zero = new StackElement(frame.getStackSize());
        StackElement first = new StackElement(frame.getStackSize() - 1);
        StackElement second = new StackElement(frame.getStackSize() - 2);
        switch(insn.getOpcode()) {
            case GETSTATIC:
                expr = new FieldAccess(owner, name, null);
                return new Statement[]{new AssignmentStatement(zero, expr)};
            case PUTSTATIC:
                expr = new FieldAccess(owner, name, null);
                return new Statement[]{new AssignmentStatement(expr, first)};
            case GETFIELD:
                expr = new FieldAccess(owner, name, first);
                return new Statement[]{new AssignmentStatement(first, expr)};
            case PUTFIELD:
                expr = new FieldAccess(owner, name, second);
                return new Statement[]{new AssignmentStatement(expr, first)};
            default:
                throw new IllegalArgumentException();
        }
    }
}
