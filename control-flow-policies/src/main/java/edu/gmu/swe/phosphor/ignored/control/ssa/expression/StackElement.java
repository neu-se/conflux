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

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof StackElement)) {
            return false;
        }
        StackElement that = (StackElement) o;
        if(index != that.index) {
            return false;
        }
        return version == that.version;
    }

    @Override
    public int hashCode() {
        int result = index;
        result = 31 * result + version;
        return result;
    }
}
