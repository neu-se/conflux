package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public enum NegateOperation implements UnaryOperation {

    NEGATE;

    @Override
    public String format(Expression expression) {
        return String.format("-%s", expression);
    }
}