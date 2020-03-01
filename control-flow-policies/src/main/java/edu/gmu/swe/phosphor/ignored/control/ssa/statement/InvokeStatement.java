package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.gmu.swe.phosphor.ignored.control.ssa.expression.InvokeExpression;

public class InvokeStatement implements Statement {

    private InvokeExpression expression;

    public InvokeStatement(InvokeExpression expression) {
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
