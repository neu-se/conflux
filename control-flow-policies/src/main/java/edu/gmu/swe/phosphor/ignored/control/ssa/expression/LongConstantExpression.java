package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;

public final class LongConstantExpression implements ConstantExpression {

    private final long constant;

    public LongConstantExpression(long constant) {
        this.constant = constant;
    }

    public long getConstant() {
        return constant;
    }

    LongConstantExpression negate(int size, AbstractInsnNode insnSource) {
        return new LongConstantExpression(-constant);
    }

    FloatConstantExpression castToFloat(int size, AbstractInsnNode insnSource) {
        return new FloatConstantExpression(constant);
    }

    DoubleConstantExpression castToDouble(int size, AbstractInsnNode insnSource) {
        return new DoubleConstantExpression(constant);
    }

    IntegerConstantExpression castToInt(int size, AbstractInsnNode insnSource) {
        return new IntegerConstantExpression((int) constant);
    }

    LongConstantExpression add(LongConstantExpression other) {
        return new LongConstantExpression(constant + other.constant);
    }

    LongConstantExpression subtract(LongConstantExpression other) {
        return new LongConstantExpression(constant - other.constant);
    }

    LongConstantExpression divide(LongConstantExpression other) {
        return new LongConstantExpression(constant / other.constant);
    }

    LongConstantExpression multiply(LongConstantExpression other) {
        return new LongConstantExpression(constant * other.constant);
    }

    LongConstantExpression remainder(LongConstantExpression other) {
        return new LongConstantExpression(constant % other.constant);
    }

    LongConstantExpression shiftLeft(LongConstantExpression other) {
        return new LongConstantExpression(constant << other.constant);
    }

    LongConstantExpression shiftRight(LongConstantExpression other) {
        return new LongConstantExpression(constant >> other.constant);
    }

    LongConstantExpression shiftRightUnsigned(LongConstantExpression other) {
        return new LongConstantExpression(constant >>> other.constant);
    }

    LongConstantExpression bitwiseOr(LongConstantExpression other) {
        return new LongConstantExpression(constant | other.constant);
    }

    LongConstantExpression bitwiseAnd(LongConstantExpression other) {
        return new LongConstantExpression(constant & other.constant);
    }

    LongConstantExpression bitwiseXor(LongConstantExpression other) {
        return new LongConstantExpression(constant ^ other.constant);
    }

    LongConstantExpression compare(LongConstantExpression other) {
        return new LongConstantExpression(Long.compare(constant, other.constant));
    }

    @Override
    public boolean canMerge(ConstantExpression other) {
        if(other instanceof IntegerConstantExpression) {
            return constant == ((IntegerConstantExpression) other).getConstant();
        } else if(other instanceof LongConstantExpression) {
            return constant == ((LongConstantExpression) other).getConstant();
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof LongConstantExpression) || !super.equals(o)) {
            return false;
        }
        LongConstantExpression that = (LongConstantExpression) o;
        return constant == that.constant;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (constant ^ (constant >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return String.valueOf(constant);
    }
}
