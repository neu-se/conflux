package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public final class ArrayExpression implements Expression {

    private final Expression arrayRef;
    private final Expression index;

    public ArrayExpression(Expression arrayRef, Expression index) {
        if(arrayRef == null || index == null) {
            throw new NullPointerException();
        }
        this.arrayRef = arrayRef;
        this.index = index;
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", arrayRef, index);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof ArrayExpression)) {
            return false;
        }
        ArrayExpression that = (ArrayExpression) o;
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
