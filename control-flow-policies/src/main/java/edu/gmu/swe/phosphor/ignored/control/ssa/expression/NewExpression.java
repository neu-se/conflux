package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public final class NewExpression implements Expression {

    private final String desc;

    public NewExpression(String desc) {
        if(desc == null) {
            throw new NullPointerException();
        }
        this.desc = desc;
    }

    @Override
    public String toString() {
        return String.format("new %s", desc);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof NewExpression)) {
            return false;
        }
        NewExpression that = (NewExpression) o;
        return desc.equals(that.desc);
    }

    @Override
    public int hashCode() {
        return desc.hashCode();
    }
}
