package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.gmu.swe.phosphor.ignored.control.ssa.StackElement;

public class ArrayExpression implements Expression {
    private StackElement arrayRef;
    private StackElement index;

    public ArrayExpression(StackElement arrayRef, StackElement index) {
        this.arrayRef = arrayRef;
        this.index = index;
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", arrayRef, index);
    }
}
