package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public abstract class VariableExpression implements Expression {

    private final int version;

    VariableExpression(int version) {
        this.version = version;
    }

    public abstract VariableExpression setVersion(int version);

    public int getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof VariableExpression)) {
            return false;
        }
        VariableExpression that = (VariableExpression) o;
        return version == that.version;
    }

    @Override
    public int hashCode() {
        return version;
    }
}
