package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;

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
        } else if(!(o instanceof ObjectConstantExpression)) {
            return false;
        }
        ObjectConstantExpression that = (ObjectConstantExpression) o;
        return constant != null ? constant.equals(that.constant) : that.constant == null;
    }

    @Override
    public int hashCode() {
        return constant != null ? constant.hashCode() : 0;
    }

    @Override
    public String toString() {
        if(constant == null) {
            return "null";
        } else if(constant instanceof String) {
            return String.format("\"%s\"", constant);
        } else {
            return constant.toString();
        }
    }

    @Override
    public ObjectConstantExpression process(Map<VersionedExpression, VersionStack> versionStacks) {
        return this;
    }
}
