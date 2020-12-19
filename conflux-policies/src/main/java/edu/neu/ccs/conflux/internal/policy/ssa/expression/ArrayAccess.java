package edu.neu.ccs.conflux.internal.policy.ssa.expression;

public final class ArrayAccess implements Expression {

    private final Expression arrayRef;
    private final Expression index;

    public ArrayAccess(Expression arrayRef, Expression index) {
        if(arrayRef == null || index == null) {
            throw new NullPointerException();
        }
        this.arrayRef = arrayRef;
        this.index = index;
    }

    public Expression getArrayRef() {
        return arrayRef;
    }

    public Expression getIndex() {
        return index;
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
        return String.format("%s[%s]", arrayRef, index);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof ArrayAccess)) {
            return false;
        }
        ArrayAccess that = (ArrayAccess) o;
        if(!arrayRef.equals(that.arrayRef)) {
            return false;
        }
        return index.equals(that.index);
    }

    @Override
    public int hashCode() {
        int result = arrayRef.hashCode();
        result = 31 * result + index.hashCode();
        return result;
    }
}
