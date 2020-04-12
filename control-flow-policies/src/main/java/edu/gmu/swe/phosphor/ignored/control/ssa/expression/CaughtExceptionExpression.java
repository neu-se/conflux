package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public class CaughtExceptionExpression implements Expression {

    private final int id;

    public CaughtExceptionExpression(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
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
        return "caughtException" + id;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof CaughtExceptionExpression)) {
            return false;
        }
        CaughtExceptionExpression that = (CaughtExceptionExpression) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
