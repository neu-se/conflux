package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public class ConditionExpression {

    private final Condition condition;
    private final Expression leftHandSide;
    private final Expression rightHandSide;

    public ConditionExpression(Condition condition, Expression leftHandSide, Expression rightHandSide) {
        this.condition = condition;
        this.leftHandSide = leftHandSide;
        this.rightHandSide = rightHandSide;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", leftHandSide, condition, rightHandSide);
    }
}
