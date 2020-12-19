package edu.neu.ccs.conflux.internal.policy.ssa.expression;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

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
                return NegateOperation.NEGATE;
            case ARRAYLENGTH:
                return ArrayLengthOperation.ARRAY_LENGTH;
            case INSTANCEOF:
                return InstanceOfOperation.getInstance(insn);
            default:
                return CastOperation.getInstance(insn);
        }
    }
}
