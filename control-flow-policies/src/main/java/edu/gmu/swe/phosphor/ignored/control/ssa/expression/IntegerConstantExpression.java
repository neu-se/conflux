package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;

public final class IntegerConstantExpression implements ConstantExpression {

    private final int constant;

    public IntegerConstantExpression(int constant) {
        this.constant = constant;
    }

    public int getConstant() {
        return constant;
    }

    IntegerConstantExpression increment(int amount) {
        return new IntegerConstantExpression(constant + amount);
    }

    IntegerConstantExpression negate(int size, AbstractInsnNode insnSource) {
        return new IntegerConstantExpression(-constant);
    }

    FloatConstantExpression castToFloat(int size, AbstractInsnNode insnSource) {
        return new FloatConstantExpression(constant);
    }

    DoubleConstantExpression castToDouble(int size, AbstractInsnNode insnSource) {
        return new DoubleConstantExpression(constant);
    }

    LongConstantExpression castToLong(int size, AbstractInsnNode insnSource) {
        return new LongConstantExpression(constant);
    }

    IntegerConstantExpression castToByte(int size, AbstractInsnNode insnSource) {
        return new IntegerConstantExpression((byte) constant);
    }

    IntegerConstantExpression castToShort(int size, AbstractInsnNode insnSource) {
        return new IntegerConstantExpression((short) constant);
    }

    IntegerConstantExpression castToChar(int size, AbstractInsnNode insnSource) {
        return new IntegerConstantExpression((char) constant);
    }

    IntegerConstantExpression add(IntegerConstantExpression other) {
        return new IntegerConstantExpression(constant + other.constant);
    }

    IntegerConstantExpression subtract(IntegerConstantExpression other) {
        return new IntegerConstantExpression(constant - other.constant);
    }

    IntegerConstantExpression divide(IntegerConstantExpression other) {
        return new IntegerConstantExpression(constant / other.constant);
    }

    IntegerConstantExpression multiply(IntegerConstantExpression other) {
        return new IntegerConstantExpression(constant * other.constant);
    }

    IntegerConstantExpression remainder(IntegerConstantExpression other) {
        return new IntegerConstantExpression(constant % other.constant);
    }

    IntegerConstantExpression shiftLeft(IntegerConstantExpression other) {
        return new IntegerConstantExpression(constant << other.constant);
    }

    IntegerConstantExpression shiftRight(IntegerConstantExpression other) {
        return new IntegerConstantExpression(constant >> other.constant);
    }

    IntegerConstantExpression shiftRightUnsigned(IntegerConstantExpression other) {
        return new IntegerConstantExpression(constant >>> other.constant);
    }

    IntegerConstantExpression bitwiseOr(IntegerConstantExpression other) {
        return new IntegerConstantExpression(constant | other.constant);
    }

    IntegerConstantExpression bitwiseAnd(IntegerConstantExpression other) {
        return new IntegerConstantExpression(constant & other.constant);
    }

    IntegerConstantExpression bitwiseXor(IntegerConstantExpression other) {
        return new IntegerConstantExpression(constant ^ other.constant);
    }

    @Override
    public boolean canMerge(ConstantExpression other) {
        if(other instanceof IntegerConstantExpression) {
            return constant == ((IntegerConstantExpression) other).constant;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof IntegerConstantExpression)) {
            return false;
        }
        IntegerConstantExpression that = (IntegerConstantExpression) o;
        return constant == that.constant;
    }

    @Override
    public int hashCode() {
        return constant;
    }

    @Override
    public String toString() {
        return String.valueOf(constant);
    }
}
