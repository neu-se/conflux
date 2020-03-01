package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import java.util.Objects;

public final class ObjectConstantExpression implements ConstantExpression {

    private final Object constant;

    public ObjectConstantExpression(Object constant) {
        this.constant = constant;
    }

    @Override
    public boolean canMerge(ConstantExpression other) {
        return other instanceof ObjectConstantExpression
                && Objects.equals(constant, ((ObjectConstantExpression) other).constant);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof ObjectConstantExpression) || !super.equals(o)) {
            return false;
        }
        ObjectConstantExpression that = (ObjectConstantExpression) o;
        return constant != null ? constant.equals(that.constant) : that.constant == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (constant != null ? constant.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return constant == null ? "null" : constant.toString();
    }
}
