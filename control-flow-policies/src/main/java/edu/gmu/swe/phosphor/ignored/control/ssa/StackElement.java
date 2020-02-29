package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;

public class StackElement implements Expression {

    private final int index;
    private int version;

    public StackElement(int index) {
        if(index < 0) {
            throw new IllegalArgumentException();
        }
        this.index = index;
        this.version = -1;
    }

    @Override
    public String toString() {
        if(version == -1) {
            return String.format("s%d", index);
        }
        return String.format("s%d_%d", index, version);
    }
}
