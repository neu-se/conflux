package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public enum Condition {

    EQUAL("==") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return operand1 instanceof ConstantExpression && operand2 instanceof ConstantExpression;
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            if(canPerform(operand1, operand2)) {
                return ((ConstantExpression) operand1).constantEqual((ConstantExpression) operand2);
            } else {
                throw new IllegalArgumentException();
            }
        }
    },
    NOT_EQUAL("!=") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return operand1 instanceof ConstantExpression && operand2 instanceof ConstantExpression;
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            if(canPerform(operand1, operand2)) {
                return ((ConstantExpression) operand1).notEqual((ConstantExpression) operand2);
            } else {
                throw new IllegalArgumentException();
            }
        }
    },
    LESS_THAN("<") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression;
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            if(canPerform(operand1, operand2)) {
                return ((IntegerConstantExpression) operand1).lessThan((IntegerConstantExpression) operand2);
            } else {
                throw new IllegalArgumentException();
            }
        }
    },
    GREATER_THAN(">") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression;
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            if(canPerform(operand1, operand2)) {
                return ((IntegerConstantExpression) operand1).greaterThan((IntegerConstantExpression) operand2);
            } else {
                throw new IllegalArgumentException();
            }
        }
    },
    LESS_THAN_OR_EQUAL("<=") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression;
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            if(canPerform(operand1, operand2)) {
                return ((IntegerConstantExpression) operand1).greaterThanOrEqual((IntegerConstantExpression) operand2);
            } else {
                throw new IllegalArgumentException();
            }
        }
    },
    GREATER_THAN_OR_EQUAL(">=") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression;
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            if(canPerform(operand1, operand2)) {
                return ((IntegerConstantExpression) operand1).lessThanOrEqual((IntegerConstantExpression) operand2);
            } else {
                throw new IllegalArgumentException();
            }
        }
    };

    private final String symbol;

    Condition(String symbol) {
        this.symbol = symbol;
    }

    public String format(Expression operand1, Expression operand2) {
        return String.format("%s %s %s", operand1, symbol, operand2);
    }

    public abstract boolean canPerform(Expression operand1, Expression operand2);

    public abstract Expression perform(Expression operand1, Expression operand2);

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
