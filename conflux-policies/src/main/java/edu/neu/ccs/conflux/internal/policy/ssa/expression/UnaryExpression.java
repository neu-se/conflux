package edu.neu.ccs.conflux.internal.policy.ssa.expression;

public final class UnaryExpression implements Expression {

    private final UnaryOperation operation;
    private final Expression operand;

    public UnaryExpression(UnaryOperation operation, Expression operand) {
        if(operation == null || operand == null) {
            throw new NullPointerException();
        }
        this.operation = operation;
        this.operand = operand;
    }

    public UnaryOperation getOperation() {
        return operation;
    }

    public Expression getOperand() {
        return operand;
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
        return operation.format(operand);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof UnaryExpression)) {
            return false;
        }
        UnaryExpression that = (UnaryExpression) o;
        if(!operation.equals(that.operation)) {
            return false;
        }
        return operand.equals(that.operand);
    }

    @Override
    public int hashCode() {
        int result = operation.hashCode();
        result = 31 * result + operand.hashCode();
        return result;
    }
}
