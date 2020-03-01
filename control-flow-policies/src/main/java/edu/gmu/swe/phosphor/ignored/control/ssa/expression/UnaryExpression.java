package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public class UnaryExpression implements Expression {
    private final UnaryOperation operation;
    private final Expression operand;

    public UnaryExpression(UnaryOperation operation, Expression operand) {
        this.operation = operation;
        this.operand = operand;
    }

    @Override
    public String toString() {
        return String.format("%s%s", operation, operand);
    }
}
