package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;

public final class IfStatement implements Statement {

    private final Expression condition;
    private final Label target;
    private final transient List<VariableExpression> usedVariables;

    public IfStatement(Expression condition, Label target) {
        if(condition == null || target == null) {
            throw new NullPointerException();
        }
        this.condition = condition;
        this.target = target;
        usedVariables = Statement.gatherVersionedExpressions(condition);
    }

    @Override
    public String toString() {
        return String.format("if %s goto %s", condition, target);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof IfStatement)) {
            return false;
        }
        IfStatement that = (IfStatement) o;
        if(!condition.equals(that.condition)) {
            return false;
        }
        return target.equals(that.target);
    }

    @Override
    public int hashCode() {
        int result = condition.hashCode();
        result = 31 * result + target.hashCode();
        return result;
    }

    @Override
    public IfStatement transform(VariableTransformer transformer) {
        return new IfStatement(condition.transform(transformer), target);
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
