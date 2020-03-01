package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;

public class ThrowStatement implements Statement {

    private final Expression expression;

    public ThrowStatement(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return String.format("throw %s", expression);
    }
}
