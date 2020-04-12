package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public final class DoubleConstantExpression implements ConstantExpression {

    private final double constant;

    public DoubleConstantExpression(double constant) {
        this.constant = constant;
    }

    public double getConstant() {
        return constant;
    }

    public DoubleConstantExpression negate() {
        return new DoubleConstantExpression(-constant);
    }

    public FloatConstantExpression castToFloat() {
        return new FloatConstantExpression((float) constant);
    }

    public IntegerConstantExpression castToInt() {
        return new IntegerConstantExpression((int) constant);
    }

    public LongConstantExpression castToLong() {
        return new LongConstantExpression((long) constant);
    }

    public DoubleConstantExpression add(DoubleConstantExpression other) {
        return new DoubleConstantExpression(constant + other.constant);
    }

    public DoubleConstantExpression subtract(DoubleConstantExpression other) {
        return new DoubleConstantExpression(constant - other.constant);
    }

    public DoubleConstantExpression divide(DoubleConstantExpression other) {
        return new DoubleConstantExpression(constant / other.constant);
    }

    public DoubleConstantExpression multiply(DoubleConstantExpression other) {
        return new DoubleConstantExpression(constant * other.constant);
    }

    public DoubleConstantExpression remainder(DoubleConstantExpression other) {
        return new DoubleConstantExpression(constant % other.constant);
    }

    public DoubleConstantExpression compareG(DoubleConstantExpression other) {
        double result;
        if(Double.isNaN(constant) || Double.isNaN(other.constant)) {
            result = 1;
        } else {
            result = Double.compare(constant, other.constant);
        }
        return new DoubleConstantExpression(result);
    }

    public DoubleConstantExpression compareL(DoubleConstantExpression other) {
        double result;
        if(Double.isNaN(constant) || Double.isNaN(other.constant)) {
            result = -1;
        } else {
            result = Double.compare(constant, other.constant);
        }
        return new DoubleConstantExpression(result);
    }

    @Override
    public boolean canMerge(ConstantExpression other) {
        if(other instanceof IntegerConstantExpression) {
            return constant == ((IntegerConstantExpression) other).getConstant();
        } else if(other instanceof LongConstantExpression) {
            return constant == ((LongConstantExpression) other).getConstant();
        } else if(other instanceof FloatConstantExpression) {
            return constant == ((FloatConstantExpression) other).getConstant();
        } else if(other instanceof DoubleConstantExpression) {
            return constant == ((DoubleConstantExpression) other).getConstant();
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
        } else if(!(o instanceof DoubleConstantExpression)) {
            return false;
        }
        DoubleConstantExpression that = (DoubleConstantExpression) o;
        return Double.compare(that.constant, constant) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(constant);
        return (int) (temp ^ (temp >>> 32));
    }

    @Override
    public String toString() {
        return String.valueOf(constant);
    }
}
