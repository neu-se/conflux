package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public final class StackElement extends VariableExpression {

    private final int index;

    public StackElement(int index) {
        super(-1);
        this.index = index;
    }

    private StackElement(int index, int version) {
        super(version);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public StackElement setVersion(int version) {
        return new StackElement(this.index, version);
    }

    @Override
    public <V> V accept(ExpressionVisitor<V> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <V, S> V accept(StatefulExpressionVisitor<V, ? super S> visitor, S state) {
        return visitor.visit(this, state);
    }

    @Override
    public String toString() {
        if(getVersion() == -1) {
            return String.format("s%d", index);
        }
        return String.format("s%d_%d", index, getVersion());
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof StackElement) || !super.equals(o)) {
            return false;
        }
        StackElement that = (StackElement) o;
        return index == that.index;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + index;
        return result;
    }
}
