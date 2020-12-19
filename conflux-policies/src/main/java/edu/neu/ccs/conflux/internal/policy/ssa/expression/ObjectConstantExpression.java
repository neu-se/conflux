package edu.neu.ccs.conflux.internal.policy.ssa.expression;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;

public final class ObjectConstantExpression implements ConstantExpression {

    private final Object constant;

    public ObjectConstantExpression(Object constant) {
        this.constant = constant;
    }

    public Object getConstant() {
        return constant;
    }

    public boolean instanceOf(String desc) {
        if(constant == null) {
            return false;
        } else {
            return Type.getInternalName(constant.getClass()).equals(desc);
        }
    }

    @Override
    public boolean canMerge(ConstantExpression other) {
        return other instanceof ObjectConstantExpression
                && ((ObjectConstantExpression) other).constant == constant;
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
}
