package edu.gmu.swe.phosphor.ignored.control.ssa;

public class ReturnStatement {

    StackElement returnValue;

    public ReturnStatement(StackElement returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public String toString() {
        return String.format("return %s", returnValue);
    }
}
