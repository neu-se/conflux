package edu.neu.ccs.conflux.internal.policy.ssa.expression;

public final class BinaryExpression implements Expression {

    private final BinaryOperation operation;
    private final Expression operand1;
    private final Expression operand2;

    public BinaryExpression(BinaryOperation operation, Expression operand1, Expression operand2) {
        if(operation == null || operand1 == null || operand2 == null) {
            throw new NullPointerException();
        }
        this.operation = operation;
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    public BinaryOperation getOperation() {
        return operation;
    }

    public Expression getOperand1() {
        return operand1;
    }

    public Expression getOperand2() {
        return operand2;
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
        return operation.format(operand1, operand2);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof BinaryExpression)) {
            return false;
        }
        BinaryExpression that = (BinaryExpression) o;
        if(operation != that.operation) {
            return false;
        }
        if(!operand1.equals(that.operand1)) {
            return false;
        }
        return operand2.equals(that.operand2);
    }

    @Override
    public int hashCode() {
        int result = operation.hashCode();
        result = 31 * result + operand1.hashCode();
        result = 31 * result + operand2.hashCode();
        return result;
    }
}
