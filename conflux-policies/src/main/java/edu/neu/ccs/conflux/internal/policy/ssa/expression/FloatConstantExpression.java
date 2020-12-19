package edu.neu.ccs.conflux.internal.policy.ssa.expression;

public final class FloatConstantExpression implements ConstantExpression {

    private final float constant;

    public FloatConstantExpression(float constant) {
        this.constant = constant;
    }

    public float getConstant() {
        return constant;
    }

    FloatConstantExpression negate() {
        return new FloatConstantExpression(-constant);
    }

    DoubleConstantExpression castToDouble() {
        return new DoubleConstantExpression(constant);
    }

    IntegerConstantExpression castToInt() {
        return new IntegerConstantExpression((int) constant);
    }

    LongConstantExpression castToLong() {
        return new LongConstantExpression((long) constant);
    }

    FloatConstantExpression add(FloatConstantExpression other) {
        return new FloatConstantExpression(constant + other.constant);
    }

    FloatConstantExpression subtract(FloatConstantExpression other) {
        return new FloatConstantExpression(constant - other.constant);
    }

    FloatConstantExpression divide(FloatConstantExpression other) {
        return new FloatConstantExpression(constant / other.constant);
    }

    FloatConstantExpression multiply(FloatConstantExpression other) {
        return new FloatConstantExpression(constant * other.constant);
    }

    FloatConstantExpression remainder(FloatConstantExpression other) {
        return new FloatConstantExpression(constant % other.constant);
    }

    FloatConstantExpression compareG(FloatConstantExpression other) {
        float result;
        if(Float.isNaN(constant) || Float.isNaN(other.constant)) {
            result = 1;
        } else {
            result = Float.compare(constant, other.constant);
        }
        return new FloatConstantExpression(result);
    }

    FloatConstantExpression compareL(FloatConstantExpression other) {
        float result;
        if(Float.isNaN(constant) || Float.isNaN(other.constant)) {
            result = -1;
        } else {
            result = Float.compare(constant, other.constant);
        }
        return new FloatConstantExpression(result);
    }

    @Override
    public boolean canMerge(ConstantExpression other) {
        if(other instanceof IntegerConstantExpression) {
            return constant == ((IntegerConstantExpression) other).getConstant();
        } else if(other instanceof LongConstantExpression) {
            return constant == ((LongConstantExpression) other).getConstant();
        } else if(other instanceof FloatConstantExpression) {
            return constant == ((FloatConstantExpression) other).getConstant();
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
        } else if(!(o instanceof FloatConstantExpression)) {
            return false;
        }
        FloatConstantExpression that = (FloatConstantExpression) o;
        return Float.compare(that.constant, constant) == 0;
    }

    @Override
    public int hashCode() {
        return (constant != +0.0f ? Float.floatToIntBits(constant) : 0);
    }

    @Override
    public String toString() {
        return String.valueOf(constant);
    }
}
