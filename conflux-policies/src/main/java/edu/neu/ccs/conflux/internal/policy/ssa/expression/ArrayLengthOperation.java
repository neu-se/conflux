package edu.neu.ccs.conflux.internal.policy.ssa.expression;

public enum ArrayLengthOperation implements UnaryOperation {

    ARRAY_LENGTH;

    @Override
    public String format(Expression expression) {
        return String.format("%s.length", expression);
    }

    @Override
    public boolean canPerform(Expression operand) {
        return operand instanceof NewArrayExpression;
    }

    @Override
    public Expression perform(Expression operand) {
        if(operand instanceof NewArrayExpression) {
            return ((NewArrayExpression) operand).getDims()[0];
        } else {
            throw new IllegalArgumentException();
        }
    }
}

