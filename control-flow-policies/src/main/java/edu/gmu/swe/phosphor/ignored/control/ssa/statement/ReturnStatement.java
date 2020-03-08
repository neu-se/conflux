package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;

public final class ReturnStatement implements Statement {

    private final Expression returnValue;
    private final transient List<VariableExpression> usedVariables;


    public ReturnStatement(Expression returnValue) {
        this.returnValue = returnValue;
        usedVariables = Statement.gatherVersionedExpressions(returnValue);
    }

    public Expression getReturnValue() {
        return returnValue;
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
    public ReturnStatement transform(VariableTransformer transformer) {
        if(returnValue == null) {
            return this;
        } else {
            return new ReturnStatement(returnValue.transform(transformer));
        }
    }

    @Override
    public VariableExpression getDefinedVariable() {
        return null;
    }

    @Override
    public List<VariableExpression> getUsedVariables() {
        return usedVariables;
    }

    @Override
    public Expression getDefinedExpression() {
        return null;
    }

    @Override
    public List<Expression> getUsedExpressions() {
        if(returnValue == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(returnValue);
        }
    }
}
