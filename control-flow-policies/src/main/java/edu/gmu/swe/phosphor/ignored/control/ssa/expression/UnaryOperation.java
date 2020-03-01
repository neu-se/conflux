package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;
import static edu.gmu.swe.phosphor.ignored.control.ssa.expression.NegateOperation.NEGATE;

public interface UnaryOperation {

    static UnaryOperation getInstance(AbstractInsnNode insn) {
        switch(insn.getOpcode()) {
            case INEG:
            case LNEG:
            case FNEG:
            case DNEG:
                return NEGATE;
            default:
                return CastOperation.getInstance(insn);
        }
    }
}
