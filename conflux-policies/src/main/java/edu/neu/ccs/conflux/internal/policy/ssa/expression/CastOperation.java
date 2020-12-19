package edu.neu.ccs.conflux.internal.policy.ssa.expression;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.TypeInsnNode;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public final class CastOperation implements UnaryOperation {

    public static final CastOperation TO_INT = new CastOperation("int");
    public static final CastOperation TO_LONG = new CastOperation("long");
    public static final CastOperation TO_FLOAT = new CastOperation("float");
    public static final CastOperation TO_DOUBLE = new CastOperation("double");
    public static final CastOperation TO_BYTE = new CastOperation("byte");
    public static final CastOperation TO_CHAR = new CastOperation("char");
    public static final CastOperation TO_SHORT = new CastOperation("short");

    private final String desc;

    public CastOperation(String desc) {
        if(desc == null) {
            throw new NullPointerException();
        }
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return String.format("(%s)", desc);
    }

    @Override
    public String format(Expression expression) {
        return String.format("((%s) %s)", desc, expression);
    }

    @Override
    public boolean canPerform(Expression operand) {
        return true;
    }

    @Override
    public Expression perform(Expression operand) {
        if(operand instanceof IntegerConstantExpression) {
            switch(desc) {
                case "int":
                    return operand;
                case "long":
                    return ((IntegerConstantExpression) operand).castToLong();
                case "float":
                    return ((IntegerConstantExpression) operand).castToFloat();
                case "double":
                    return ((IntegerConstantExpression) operand).castToDouble();
                case "byte":
                    return ((IntegerConstantExpression) operand).castToByte();
                case "char":
                    return ((IntegerConstantExpression) operand).castToChar();
                case "short":
                    return ((IntegerConstantExpression) operand).castToShort();
                default:
                    throw new IllegalArgumentException();
            }
        } else if(operand instanceof FloatConstantExpression) {
            switch(desc) {
                case "int":
                    return ((FloatConstantExpression) operand).castToInt();
                case "long":
                    return ((FloatConstantExpression) operand).castToLong();
                case "float":
                    return operand;
                case "double":
                    return ((FloatConstantExpression) operand).castToDouble();
                default:
                    throw new IllegalArgumentException();
            }
        } else if(operand instanceof DoubleConstantExpression) {
            switch(desc) {
                case "int":
                    return ((DoubleConstantExpression) operand).castToInt();
                case "long":
                    return ((DoubleConstantExpression) operand).castToLong();
                case "float":
                    return ((DoubleConstantExpression) operand).castToFloat();
                case "double":
                    return operand;
                default:
                    throw new IllegalArgumentException();
            }
        } else if(operand instanceof LongConstantExpression) {
            switch(desc) {
                case "int":
                    return ((LongConstantExpression) operand).castToInt();
                case "long":
                    return operand;
                case "float":
                    return ((LongConstantExpression) operand).castToFloat();
                case "double":
                    return ((LongConstantExpression) operand).castToDouble();
                default:
                    throw new IllegalArgumentException();
            }
        } else {
            return operand;
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof CastOperation)) {
            return false;
        }
        CastOperation that = (CastOperation) o;
        return desc.equals(that.desc);
    }

    @Override
    public int hashCode() {
        return desc.hashCode();
    }

    public static CastOperation getInstance(AbstractInsnNode insn) {
        switch(insn.getOpcode()) {
            case CHECKCAST:
                return new CastOperation(((TypeInsnNode) insn).desc);
            case I2L:
            case D2L:
            case F2L:
                return TO_LONG;
            case I2F:
            case D2F:
            case L2F:
                return TO_FLOAT;
            case I2D:
            case F2D:
            case L2D:
                return TO_DOUBLE;
            case L2I:
            case D2I:
            case F2I:
                return TO_INT;
            case I2B:
                return TO_BYTE;
            case I2C:
                return TO_CHAR;
            case I2S:
                return TO_SHORT;
            default:
                throw new IllegalArgumentException();
        }
    }
}
