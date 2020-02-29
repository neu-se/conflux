package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public final class DoubleConstantExpression implements ConstantExpression {

    private final double constant;

    public DoubleConstantExpression(double constant) {
        this.constant = constant;
    }

    public double getConstant() {
        return constant;
    }

    DoubleConstantExpression negate() {
        return new DoubleConstantExpression(-constant);
    }

    FloatConstantExpression castToFloat() {
        return new FloatConstantExpression((float) constant);
    }

    IntegerConstantExpression castToInt() {
        return new IntegerConstantExpression((int) constant);
    }

    LongConstantExpression castToLong() {
        return new LongConstantExpression((long) constant);
    }

    DoubleConstantExpression add(DoubleConstantExpression other) {
        return new DoubleConstantExpression(constant + other.constant);
    }

    DoubleConstantExpression subtract(DoubleConstantExpression other) {
        return new DoubleConstantExpression(constant - other.constant);
    }

    DoubleConstantExpression divide(DoubleConstantExpression other) {
        return new DoubleConstantExpression(constant / other.constant);
    }

    DoubleConstantExpression multiply(DoubleConstantExpression other) {
        return new DoubleConstantExpression(constant * other.constant);
    }

    DoubleConstantExpression remainder(DoubleConstantExpression other) {
        return new DoubleConstantExpression(constant % other.constant);
    }

    DoubleConstantExpression compareG(DoubleConstantExpression other) {
        double result;
        if(Double.isNaN(constant) || Double.isNaN(other.constant)) {
            result = 1;
        } else {
            result = Double.compare(constant, other.constant);
        }
        return new DoubleConstantExpression(result);
    }

    DoubleConstantExpression compareL(DoubleConstantExpression other) {
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
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof DoubleConstantExpression) || !super.equals(o)) {
            return false;
        }
        DoubleConstantExpression that = (DoubleConstantExpression) o;
        return Double.compare(that.constant, constant) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(constant);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return String.valueOf(constant);
    }
}
