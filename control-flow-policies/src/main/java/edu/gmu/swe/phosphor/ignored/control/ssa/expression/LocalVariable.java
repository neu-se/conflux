package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public final class LocalVariable extends VariableExpression {

    private final int index;

    public LocalVariable(int index) {
        super(-1);
        this.index = index;
    }

    private LocalVariable(int index, int version) {
        super(version);
        this.index = index;
    }

    @Override
    public LocalVariable setVersion(int version) {
        return new LocalVariable(this.index, version);
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        if(getVersion() == -1) {
            return String.format("v%d", index);
        }
        return String.format("v%d_%d", index, getVersion());
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof LocalVariable) || !super.equals(o)) {
            return false;
        }
        LocalVariable that = (LocalVariable) o;
        return index == that.index;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + index;
        return result;
    }
}
