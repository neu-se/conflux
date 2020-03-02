package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.gmu.swe.phosphor.ignored.control.ssa.expression.InvokeExpression;

public final class InvokeStatement implements Statement {

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

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof InvokeStatement)) {
            return false;
        }
        InvokeStatement that = (InvokeStatement) o;
        return expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        return expression.hashCode();
    }
}
