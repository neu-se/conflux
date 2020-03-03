package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public final class LocalVariable implements Expression {

    private final int index;
    private final int version;

    public LocalVariable(int index) {
        this.index = index;
        this.version = -1;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        if(version == -1) {
            return String.format("v%d", index);
        }
        return String.format("v%d_%d", index, version);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof LocalVariable)) {
            return false;
        }
        LocalVariable that = (LocalVariable) o;
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
