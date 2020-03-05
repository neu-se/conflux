package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VersionedExpression;

public final class ReturnStatement implements Statement {

    Expression returnValue;

    public ReturnStatement(Expression returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public String toString() {
        if(returnValue == null) {
            return "return";
        }
        return String.format("return %s", returnValue);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof ReturnStatement)) {
            return false;
        }
        ReturnStatement that = (ReturnStatement) o;
        return returnValue != null ? returnValue.equals(that.returnValue) : that.returnValue == null;
    }

    @Override
    public int hashCode() {
        return returnValue != null ? returnValue.hashCode() : 0;
    }

    @Override
    public ReturnStatement process(Map<VersionedExpression, VersionStack> versionStacks) {
        if(returnValue == null) {
            return this;
        } else {
            return new ReturnStatement(returnValue.process(versionStacks));
        }
    }
}
