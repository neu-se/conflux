package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;

public final class IfStatement implements Statement {

    private final Expression conditionExpression;
    private final Label target;
    private final transient List<VariableExpression> usedVariables;

    public IfStatement(Expression conditionExpression, Label target) {
        if(conditionExpression == null || target == null) {
            throw new NullPointerException();
        }
        this.conditionExpression = conditionExpression;
        this.target = target;
        usedVariables = Statement.gatherVersionedExpressions(conditionExpression);
    }

    @Override
    public String toString() {
        return String.format("if(%s) goto %s", conditionExpression, target);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof IfStatement)) {
            return false;
        }
        IfStatement that = (IfStatement) o;
        if(!conditionExpression.equals(that.conditionExpression)) {
            return false;
        }
        return target.equals(that.target);
    }

    @Override
    public int hashCode() {
        int result = conditionExpression.hashCode();
        result = 31 * result + target.hashCode();
        return result;
    }

    @Override
    public IfStatement transform(VariableTransformer transformer) {
        return new IfStatement(conditionExpression.transform(transformer), target);
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
