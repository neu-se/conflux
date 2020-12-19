package edu.neu.ccs.conflux.internal.policy.ssa.expression;

import static edu.neu.ccs.conflux.internal.policy.ssa.expression.ConstantExpression.fromBoolean;

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

    IntegerConstantExpression negate() {
        return new IntegerConstantExpression(-constant);
    }

    FloatConstantExpression castToFloat() {
        return new FloatConstantExpression(constant);
    }

    DoubleConstantExpression castToDouble() {
        return new DoubleConstantExpression(constant);
    }

    LongConstantExpression castToLong() {
        return new LongConstantExpression(constant);
    }

    IntegerConstantExpression castToByte() {
        return new IntegerConstantExpression((byte) constant);
    }

    IntegerConstantExpression castToShort() {
        return new IntegerConstantExpression((short) constant);
    }

    IntegerConstantExpression castToChar() {
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

    public IntegerConstantExpression greaterThan(IntegerConstantExpression other) {
        return fromBoolean(constant > other.constant);
    }

    public IntegerConstantExpression greaterThanOrEqual(IntegerConstantExpression other) {
        return fromBoolean(constant >= other.constant);
    }

    public IntegerConstantExpression lessThan(IntegerConstantExpression other) {
        return fromBoolean(constant < other.constant);
    }

    public IntegerConstantExpression lessThanOrEqual(IntegerConstantExpression other) {
        return fromBoolean(constant <= other.constant);
    }

    @Override
    public boolean canMerge(ConstantExpression other) {
        if(other instanceof IntegerConstantExpression) {
            return constant == ((IntegerConstantExpression) other).constant;
        }
        return false;
    }

    @Override
    public <V> V accept(ExpressionVisitor<V> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <V, S> V accept(StatefulExpressionVisitor<V, ? super S> visitor, S state) {
        return visitor.visit(this, state);
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
