package edu.neu.ccs.conflux.internal.policy.ssa.expression;

public enum NegateOperation implements UnaryOperation {

    NEGATE;

    @Override
    public String format(Expression expression) {
        return String.format("-%s", expression);
    }

    @Override
    public boolean canPerform(Expression operand) {
        return operand instanceof IntegerConstantExpression || operand instanceof FloatConstantExpression
                || operand instanceof LongConstantExpression || operand instanceof DoubleConstantExpression;
    }

    @Override
    public Expression perform(Expression operand) {
        if(operand instanceof IntegerConstantExpression) {
            return ((IntegerConstantExpression) operand).negate();
        } else if(operand instanceof FloatConstantExpression) {
            return ((FloatConstantExpression) operand).negate();
        } else if(operand instanceof LongConstantExpression) {
            return ((LongConstantExpression) operand).negate();
        } else if(operand instanceof DoubleConstantExpression) {
            return ((DoubleConstantExpression) operand).negate();
        } else {
            throw new IllegalArgumentException();
        }
    }
}
