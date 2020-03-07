package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public enum BinaryOperation {

    ADD("+") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return false;
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            return null;
        }
    },
    SUBTRACT("-") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return false;
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            return null;
        }
    },
    MULTIPLY("*") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return false;
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            return null;
        }
    },
    DIVIDE("/") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return false;
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            return null;
        }
    },
    REMAINDER("%") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return false;
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            return null;
        }
    },
    SHIFT_LEFT("<<") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return false;
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            return null;
        }
    },
    SHIFT_RIGHT(">>") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return false;
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            return null;
        }
    },
    SHIFT_RIGHT_UNSIGNED(">>>") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return false;
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            return null;
        }
    },
    BITWISE_OR("|") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return false;
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            return null;
        }
    },
    BITWISE_AND("&") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return false;
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            return null;
        }
    },
    BITWISE_XOR("^") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return false;
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            return null;
        }
    },
    COMPARE("compare") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return false;
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            return null;
        }
    },
    COMPARE_G("compareG") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return false;
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            return null;
        }
    },
    COMPARE_L("compareL") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return false;
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            return null;
        }
    };

    private final String symbol;

    BinaryOperation(String symbol) {
        this.symbol = symbol;
    }

    public String format(Expression operand1, Expression operand2) {
        return String.format("%s %s %s", operand1, symbol, operand2);
    }

    public static BinaryOperation getInstance(AbstractInsnNode insn) {
        switch(insn.getOpcode()) {
            case IADD:
            case LADD:
            case FADD:
            case DADD:
                return ADD;
            case ISUB:
            case LSUB:
            case FSUB:
            case DSUB:
                return SUBTRACT;
            case IMUL:
            case LMUL:
            case FMUL:
            case DMUL:
                return MULTIPLY;
            case IDIV:
            case LDIV:
            case FDIV:
            case DDIV:
                return DIVIDE;
            case IREM:
            case LREM:
            case FREM:
            case DREM:
                return REMAINDER;
            case ISHL:
            case LSHL:
                return SHIFT_LEFT;
            case ISHR:
            case LSHR:
                return SHIFT_RIGHT;
            case IUSHR:
            case LUSHR:
                return SHIFT_RIGHT_UNSIGNED;
            case IAND:
            case LAND:
                return BITWISE_AND;
            case IOR:
            case LOR:
                return BITWISE_OR;
            case IXOR:
            case LXOR:
                return BITWISE_XOR;
            case LCMP:
                return COMPARE;
            case FCMPL:
            case DCMPL:
                return COMPARE_L;
            case FCMPG:
            case DCMPG:
                return COMPARE_G;
            default:
                throw new IllegalArgumentException();
        }
    }

    public abstract boolean canPerform(Expression operand1, Expression operand2);

    public abstract Expression perform(Expression operand1, Expression operand2);
}
