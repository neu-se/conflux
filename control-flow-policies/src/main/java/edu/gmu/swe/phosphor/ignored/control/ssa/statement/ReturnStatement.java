package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;

public class ReturnStatement implements Statement {

    Expression returnValue;

    public ReturnStatement(Expression returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public String toString() {
        if(returnValue == null) {
            return "return";
        }
        return String.format("return %s", returnValue);
    }
}
