package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

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

    @Override
    public ArrayExpression process(Map<VersionedExpression, VersionStack> versionStacks) {
        return new ArrayExpression(arrayRef.process(versionStacks), index.process(versionStacks));
    }

    @Override
    public List<VersionedExpression> referencedVariables() {
        return Statement.gatherVersionedExpressions(arrayRef, index);
    }
}
