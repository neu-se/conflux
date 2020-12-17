package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;

public final class ReturnStatement implements Statement {

    private final Expression expression;


    public ReturnStatement(Expression expression) {
        this.expression = expression;
    }

    public boolean isVoid() {
        return expression == null;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public <V> V accept(StatementVisitor<V> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <V, S> V accept(StatefulStatementVisitor<V, S> visitor, S state) {
        return visitor.visit(this, state);
    }

    @Override
    public String toString() {
        if(expression == null) {
            return "return";
        }
        return String.format("return %s", expression);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof ReturnStatement)) {
            return false;
        }
        ReturnStatement that = (ReturnStatement) o;
        return expression != null ? expression.equals(that.expression) : that.expression == null;
    }

    @Override
    public int hashCode() {
        return expression != null ? expression.hashCode() : 0;
    }
}
