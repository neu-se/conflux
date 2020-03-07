package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public enum BinaryOperation {

    ADD("+") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return (operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression)
                    || (operand1 instanceof FloatConstantExpression && operand2 instanceof FloatConstantExpression)
                    || (operand1 instanceof DoubleConstantExpression && operand2 instanceof DoubleConstantExpression)
                    || (operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression);
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            if(operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression) {
                return ((IntegerConstantExpression) operand1).add((IntegerConstantExpression) operand2);
            } else if(operand1 instanceof FloatConstantExpression && operand2 instanceof FloatConstantExpression) {
                return ((FloatConstantExpression) operand1).add((FloatConstantExpression) operand2);
            } else if(operand1 instanceof DoubleConstantExpression && operand2 instanceof DoubleConstantExpression) {
                return ((DoubleConstantExpression) operand1).add((DoubleConstantExpression) operand2);
            } else if(operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression) {
                return ((LongConstantExpression) operand1).add((LongConstantExpression) operand2);
            } else {
                throw new IllegalArgumentException();
            }
        }
    },
    SUBTRACT("-") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return (operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression)
                    || (operand1 instanceof FloatConstantExpression && operand2 instanceof FloatConstantExpression)
                    || (operand1 instanceof DoubleConstantExpression && operand2 instanceof DoubleConstantExpression)
                    || (operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression);
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            if(operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression) {
                return ((IntegerConstantExpression) operand1).subtract((IntegerConstantExpression) operand2);
            } else if(operand1 instanceof FloatConstantExpression && operand2 instanceof FloatConstantExpression) {
                return ((FloatConstantExpression) operand1).subtract((FloatConstantExpression) operand2);
            } else if(operand1 instanceof DoubleConstantExpression && operand2 instanceof DoubleConstantExpression) {
                return ((DoubleConstantExpression) operand1).subtract((DoubleConstantExpression) operand2);
            } else if(operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression) {
                return ((LongConstantExpression) operand1).subtract((LongConstantExpression) operand2);
            } else {
                throw new IllegalArgumentException();
            }
        }
    },
    MULTIPLY("*") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return (operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression)
                    || (operand1 instanceof FloatConstantExpression && operand2 instanceof FloatConstantExpression)
                    || (operand1 instanceof DoubleConstantExpression && operand2 instanceof DoubleConstantExpression)
                    || (operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression);
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            if(operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression) {
                return ((IntegerConstantExpression) operand1).multiply((IntegerConstantExpression) operand2);
            } else if(operand1 instanceof FloatConstantExpression && operand2 instanceof FloatConstantExpression) {
                return ((FloatConstantExpression) operand1).multiply((FloatConstantExpression) operand2);
            } else if(operand1 instanceof DoubleConstantExpression && operand2 instanceof DoubleConstantExpression) {
                return ((DoubleConstantExpression) operand1).multiply((DoubleConstantExpression) operand2);
            } else if(operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression) {
                return ((LongConstantExpression) operand1).multiply((LongConstantExpression) operand2);
            } else {
                throw new IllegalArgumentException();
            }
        }
    },
    DIVIDE("/") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return (operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression)
                    || (operand1 instanceof FloatConstantExpression && operand2 instanceof FloatConstantExpression)
                    || (operand1 instanceof DoubleConstantExpression && operand2 instanceof DoubleConstantExpression)
                    || (operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression);
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            if(operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression) {
                return ((IntegerConstantExpression) operand1).divide((IntegerConstantExpression) operand2);
            } else if(operand1 instanceof FloatConstantExpression && operand2 instanceof FloatConstantExpression) {
                return ((FloatConstantExpression) operand1).divide((FloatConstantExpression) operand2);
            } else if(operand1 instanceof DoubleConstantExpression && operand2 instanceof DoubleConstantExpression) {
                return ((DoubleConstantExpression) operand1).divide((DoubleConstantExpression) operand2);
            } else if(operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression) {
                return ((LongConstantExpression) operand1).divide((LongConstantExpression) operand2);
            } else {
                throw new IllegalArgumentException();
            }
        }
    },
    REMAINDER("%") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return (operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression)
                    || (operand1 instanceof FloatConstantExpression && operand2 instanceof FloatConstantExpression)
                    || (operand1 instanceof DoubleConstantExpression && operand2 instanceof DoubleConstantExpression)
                    || (operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression);
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            if(operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression) {
                return ((IntegerConstantExpression) operand1).remainder((IntegerConstantExpression) operand2);
            } else if(operand1 instanceof FloatConstantExpression && operand2 instanceof FloatConstantExpression) {
                return ((FloatConstantExpression) operand1).remainder((FloatConstantExpression) operand2);
            } else if(operand1 instanceof DoubleConstantExpression && operand2 instanceof DoubleConstantExpression) {
                return ((DoubleConstantExpression) operand1).remainder((DoubleConstantExpression) operand2);
            } else if(operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression) {
                return ((LongConstantExpression) operand1).remainder((LongConstantExpression) operand2);
            } else {
                throw new IllegalArgumentException();
            }
        }
    },
    SHIFT_LEFT("<<") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return (operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression)
                    || (operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression);
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            if(operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression) {
                return ((IntegerConstantExpression) operand1).shiftLeft((IntegerConstantExpression) operand2);
            } else if(operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression) {
                return ((LongConstantExpression) operand1).shiftLeft((LongConstantExpression) operand2);
            } else {
                throw new IllegalArgumentException();
            }
        }
    },
    SHIFT_RIGHT(">>") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return (operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression)
                    || (operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression);
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            if(operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression) {
                return ((IntegerConstantExpression) operand1).shiftRight((IntegerConstantExpression) operand2);
            } else if(operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression) {
                return ((LongConstantExpression) operand1).shiftRight((LongConstantExpression) operand2);
            } else {
                throw new IllegalArgumentException();
            }
        }
    },
    SHIFT_RIGHT_UNSIGNED(">>>") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return (operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression)
                    || (operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression);
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            if(operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression) {
                return ((IntegerConstantExpression) operand1).shiftRightUnsigned((IntegerConstantExpression) operand2);
            } else if(operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression) {
                return ((LongConstantExpression) operand1).shiftRightUnsigned((LongConstantExpression) operand2);
            } else {
                throw new IllegalArgumentException();
            }
        }
    },
    BITWISE_OR("|") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return (operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression)
                    || (operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression);
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            if(operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression) {
                return ((IntegerConstantExpression) operand1).bitwiseOr((IntegerConstantExpression) operand2);
            } else if(operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression) {
                return ((LongConstantExpression) operand1).bitwiseOr((LongConstantExpression) operand2);
            } else {
                throw new IllegalArgumentException();
            }
        }
    },
    BITWISE_AND("&") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return (operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression)
                    || (operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression);
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            if(operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression) {
                return ((IntegerConstantExpression) operand1).bitwiseAnd((IntegerConstantExpression) operand2);
            } else if(operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression) {
                return ((LongConstantExpression) operand1).bitwiseAnd((LongConstantExpression) operand2);
            } else {
                throw new IllegalArgumentException();
            }
        }
    },
    BITWISE_XOR("^") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return (operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression)
                    || (operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression);
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            if(operand1 instanceof IntegerConstantExpression && operand2 instanceof IntegerConstantExpression) {
                return ((IntegerConstantExpression) operand1).bitwiseXor((IntegerConstantExpression) operand2);
            } else if(operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression) {
                return ((LongConstantExpression) operand1).bitwiseXor((LongConstantExpression) operand2);
            } else {
                throw new IllegalArgumentException();
            }
        }
    },
    COMPARE("compare") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return (operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression);
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            if(operand1 instanceof LongConstantExpression && operand2 instanceof LongConstantExpression) {
                return ((LongConstantExpression) operand1).compare((LongConstantExpression) operand2);
            } else {
                throw new IllegalArgumentException();
            }
        }
    },
    COMPARE_G("compareG") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return (operand1 instanceof FloatConstantExpression && operand2 instanceof FloatConstantExpression)
                    || (operand1 instanceof DoubleConstantExpression && operand2 instanceof DoubleConstantExpression);
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            if(operand1 instanceof FloatConstantExpression && operand2 instanceof FloatConstantExpression) {
                return ((FloatConstantExpression) operand1).compareG((FloatConstantExpression) operand2);
            } else if(operand1 instanceof DoubleConstantExpression && operand2 instanceof DoubleConstantExpression) {
                return ((DoubleConstantExpression) operand1).compareG((DoubleConstantExpression) operand2);
            } else {
                throw new IllegalArgumentException();
            }
        }
    },
    COMPARE_L("compareL") {
        @Override
        public boolean canPerform(Expression operand1, Expression operand2) {
            return (operand1 instanceof FloatConstantExpression && operand2 instanceof FloatConstantExpression)
                    || (operand1 instanceof DoubleConstantExpression && operand2 instanceof DoubleConstantExpression);
        }

        @Override
        public Expression perform(Expression operand1, Expression operand2) {
            if(operand1 instanceof FloatConstantExpression && operand2 instanceof FloatConstantExpression) {
                return ((FloatConstantExpression) operand1).compareL((FloatConstantExpression) operand2);
            } else if(operand1 instanceof DoubleConstantExpression && operand2 instanceof DoubleConstantExpression) {
                return ((DoubleConstantExpression) operand1).compareL((DoubleConstantExpression) operand2);
            } else {
                throw new IllegalArgumentException();
            }
        }
    };

    private final String symbol;

    BinaryOperation(String symbol) {
        this.symbol = symbol;
    }

    public String format(Expression operand1, Expression operand2) {
        return String.format("%s %s %s", operand1, symbol, operand2);
    }

    public abstract boolean canPerform(Expression operand1, Expression operand2);

    public abstract Expression perform(Expression operand1, Expression operand2);

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
}
