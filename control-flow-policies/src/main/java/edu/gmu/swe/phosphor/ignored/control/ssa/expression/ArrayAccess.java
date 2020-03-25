package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.control.OpcodesUtil;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.VariableTransformer;

public final class ArrayAccess implements Expression {

    private final Expression arrayRef;
    private final Expression index;
    private final int originalOpcode;

    public ArrayAccess(Expression arrayRef, Expression index, int originalOpcode) {
        if(arrayRef == null || index == null) {
            throw new NullPointerException();
        }
        if(!OpcodesUtil.isArrayStore(originalOpcode) && !OpcodesUtil.isArrayLoad(originalOpcode)) {
            throw new IllegalArgumentException();
        }
        this.arrayRef = arrayRef;
        this.index = index;
        this.originalOpcode = originalOpcode;
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

    public Expression getArrayRef() {
        return arrayRef;
    }

    public Expression getIndex() {
        return index;
    }

    public int getOriginalOpcode() {
        return originalOpcode;
    }

    @Override
    public List<VariableExpression> referencedVariables() {
        return Statement.gatherVersionedExpressions(arrayRef, index);
    }

    @Override
    public ArrayAccess transform(VariableTransformer transformer) {
        return new ArrayAccess(arrayRef.transform(transformer), index.transform(transformer), originalOpcode);
    }
}
