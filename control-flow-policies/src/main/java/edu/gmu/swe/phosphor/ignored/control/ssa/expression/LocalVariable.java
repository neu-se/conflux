package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public final class LocalVariable implements Expression {

    private final int index;
    private final int version;

    public LocalVariable(int index) {
        if(index < 0) {
            throw new IllegalArgumentException();
        }
        this.index = index;
        this.version = -1;
    }

    @Override
    public String toString() {
        if(version == -1) {
            return String.format("v%d", index);
        }
        return String.format("v%d_%d", index, version);
    }
}
