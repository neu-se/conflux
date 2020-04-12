package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public class ParameterExpression implements Expression {

    private final int parameterNumber;

    public ParameterExpression(int parameterNumber) {
        this.parameterNumber = parameterNumber;
    }

    public int getParameterNumber() {
        return parameterNumber;
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
    public String toString() {
        return "param" + parameterNumber;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof ParameterExpression)) {
            return false;
        }
        ParameterExpression that = (ParameterExpression) o;
        return parameterNumber == that.parameterNumber;
    }

    @Override
    public int hashCode() {
        return parameterNumber;
    }
}
