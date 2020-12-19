package edu.neu.ccs.conflux.internal.policy.ssa.expression;

public final class NewExpression implements Expression {

    private final String desc;

    public NewExpression(String desc) {
        if(desc == null) {
            throw new NullPointerException();
        }
        this.desc = desc;
    }

    @Override
    public <V> V accept(ExpressionVisitor<V> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <V, S> V accept(StatefulExpressionVisitor<V, ? super S> visitor, S state) {
        return visitor.visit(this, state);
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return String.format("(new %s)", desc);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof NewExpression)) {
            return false;
        }
        NewExpression that = (NewExpression) o;
        return desc.equals(that.desc);
    }

    @Override
    public int hashCode() {
        return desc.hashCode();
    }
}
