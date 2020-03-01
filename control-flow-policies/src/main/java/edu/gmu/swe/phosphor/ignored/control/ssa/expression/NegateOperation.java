package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public enum NegateOperation implements UnaryOperation {

    NEGATE;

    @Override
    public String toString() {
        return "-";
    }
}
