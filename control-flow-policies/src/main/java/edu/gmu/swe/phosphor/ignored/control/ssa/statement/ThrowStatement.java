package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VersionedExpression;

public final class ThrowStatement implements Statement {

    private final Expression expression;

    public ThrowStatement(Expression expression) {
        if(expression == null) {
            throw new NullPointerException();
        }
        this.expression = expression;
    }

    @Override
    public String toString() {
        return String.format("throw %s", expression);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof ThrowStatement)) {
            return false;
        }
        ThrowStatement that = (ThrowStatement) o;
        return expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        return expression.hashCode();
    }

    @Override
    public ThrowStatement process(Map<VersionedExpression, VersionStack> versionStacks) {
        return new ThrowStatement(expression.process(versionStacks));
    }
}
