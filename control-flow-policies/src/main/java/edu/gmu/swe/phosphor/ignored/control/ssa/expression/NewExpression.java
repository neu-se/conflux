package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public class NewExpression implements Expression {
    private final String desc;

    public NewExpression(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return String.format("new %s", desc);
    }
}
