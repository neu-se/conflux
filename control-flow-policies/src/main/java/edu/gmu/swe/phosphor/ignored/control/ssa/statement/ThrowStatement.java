package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;

public final class ThrowStatement implements Statement {

    private final Expression expression;
    private final transient List<VariableExpression> usedVariables;

    public ThrowStatement(Expression expression) {
        if(expression == null) {
            throw new NullPointerException();
        }
        this.expression = expression;
        usedVariables = Statement.gatherVersionedExpressions(expression);
    }

    public Expression getExpression() {
        return expression;
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
    public ThrowStatement transform(VariableTransformer transformer) {
        return new ThrowStatement(expression.transform(transformer));
    }

    @Override
    public VariableExpression getDefinedVariable() {
        return null;
    }

    @Override
    public List<VariableExpression> getUsedVariables() {
        return usedVariables;
    }
}
