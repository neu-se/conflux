package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;

public final class MonitorStatement implements Statement {

    private final MonitorOperation operation;
    private final Expression expression;

    public MonitorStatement(MonitorOperation operation, Expression expression) {
        if(operation == null || expression == null) {
            throw new NullPointerException();
        }
        this.operation = operation;
        this.expression = expression;
    }

    public MonitorOperation getOperation() {
        return operation;
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
        return operation.format(expression);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof MonitorStatement)) {
            return false;
        }
        MonitorStatement that = (MonitorStatement) o;
        if(operation != that.operation) {
            return false;
        }
        return expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        int result = operation.hashCode();
        result = 31 * result + expression.hashCode();
        return result;
    }
}
