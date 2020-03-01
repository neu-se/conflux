package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public class ArrayExpression implements Expression {
    private Expression arrayRef;
    private Expression index;

    public ArrayExpression(Expression arrayRef, Expression index) {
        this.arrayRef = arrayRef;
        this.index = index;
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", arrayRef, index);
    }
}
