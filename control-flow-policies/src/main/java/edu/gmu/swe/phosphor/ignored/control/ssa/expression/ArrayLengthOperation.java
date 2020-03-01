package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public enum ArrayLengthOperation implements UnaryOperation {

    ARRAY_LENGTH;

    @Override
    public String format(Expression expression) {
        return String.format("%s.length", expression);
    }
}

