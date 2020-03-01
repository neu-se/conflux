package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public enum Condition {

    EQUAL("=="),
    NOT_EQUAL("!="),
    LESS_THAN("<"),
    GREATER_THAN(">"),
    LESS_THAN_OR_EQUAL("<="),
    GREATER_THAN_OR_EQUAL(">=");

    private final String symbol;

    Condition(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return getSymbol();
    }

    public static Condition getInstance(AbstractInsnNode insn) {
        switch(insn.getOpcode()) {
            case IF_ICMPEQ:
            case IF_ACMPEQ:
            case IFEQ:
            case IFNULL:
                return EQUAL;
            case IF_ICMPNE:
            case IF_ACMPNE:
            case IFNE:
            case IFNONNULL:
                return NOT_EQUAL;
            case IF_ICMPLT:
            case IFLT:
                return LESS_THAN;
            case IF_ICMPGT:
            case IFGT:
                return GREATER_THAN;
            case IF_ICMPGE:
            case IFGE:
                return GREATER_THAN_OR_EQUAL;
            case IF_ICMPLE:
            case IFLE:
                return LESS_THAN_OR_EQUAL;
            default:
                throw new IllegalArgumentException();
        }
    }
}
