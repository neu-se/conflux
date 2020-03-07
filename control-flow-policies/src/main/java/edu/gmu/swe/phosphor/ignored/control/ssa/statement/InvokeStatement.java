package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.InvokeExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;

public final class InvokeStatement implements Statement {

    private final InvokeExpression expression;
    private final transient List<VariableExpression> usedVariables;

    public InvokeStatement(InvokeExpression expression) {
        if(expression == null) {
            throw new NullPointerException();
        }
        this.expression = expression;
        usedVariables = Statement.gatherVersionedExpressions(expression);
    }

    public InvokeExpression getExpression() {
        return expression;
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
    public InvokeStatement transform(VariableTransformer transformer) {
        return new InvokeStatement(expression.transform(transformer));
    }

    @Override
    public VariableExpression definedVariable() {
        return null;
    }

    @Override
    public List<VariableExpression> usedVariables() {
        return usedVariables;
    }
}
