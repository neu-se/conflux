package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;

public class ExpressionStatement implements Statement {

    private Expression expression;

    public ExpressionStatement(Expression expression) {
        if(expression == null) {
            throw new NullPointerException();
        }
        this.expression = expression;
    }

    @Override
    public String toString() {
        return expression.toString();
    }
}
