package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public class BinaryExpression implements Expression {

    private final BinaryOperation operation;
    private final Expression operand1;
    private final Expression operand2;

    public BinaryExpression(BinaryOperation operation, Expression operand1, Expression operand2) {
        this.operation = operation;
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", operand1, operation, operand2);
    }
}
