package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.InvokeExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VersionedExpression;

public final class InvokeStatement implements Statement {

    private InvokeExpression expression;

    public InvokeStatement(InvokeExpression expression) {
        if(expression == null) {
            throw new NullPointerException();
        }
        this.expression = expression;
    }

    @Override
    public String toString() {
        return expression.toString();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof InvokeStatement)) {
            return false;
        }
        InvokeStatement that = (InvokeStatement) o;
        return expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        return expression.hashCode();
    }

    @Override
    public InvokeStatement process(Map<VersionedExpression, VersionStack> versionStacks) {
        return new InvokeStatement(expression.process(versionStacks));
    }
}
