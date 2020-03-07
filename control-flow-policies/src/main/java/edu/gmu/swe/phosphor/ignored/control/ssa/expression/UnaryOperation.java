package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;
import static edu.gmu.swe.phosphor.ignored.control.ssa.expression.ArrayLengthOperation.ARRAY_LENGTH;
import static edu.gmu.swe.phosphor.ignored.control.ssa.expression.NegateOperation.NEGATE;

public interface UnaryOperation {

    String format(Expression expression);

    boolean canPerform(Expression operand);

    Expression perform(Expression operand);

    static UnaryOperation getInstance(AbstractInsnNode insn) {
        switch(insn.getOpcode()) {
            case INEG:
            case LNEG:
            case FNEG:
            case DNEG:
                return NEGATE;
            case ARRAYLENGTH:
                return ARRAY_LENGTH;
            case INSTANCEOF:
                return InstanceOfOperation.getInstance(insn);
            default:
                return CastOperation.getInstance(insn);
        }
    }
}
