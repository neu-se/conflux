package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public final class StackElement implements Expression {

    private final int index;
    private final int version;

    public StackElement(int index) {
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
