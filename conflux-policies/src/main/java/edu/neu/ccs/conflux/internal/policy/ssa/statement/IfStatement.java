package edu.neu.ccs.conflux.internal.policy.ssa.statement;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.Expression;

public final class IfStatement implements Statement {

    private final Expression expression;
    private final Label target;

    public IfStatement(Expression expression, Label target) {
        if(expression == null || target == null) {
            throw new NullPointerException();
        }
        this.expression = expression;
        this.target = target;
    }

    public Label getTarget() {
        return target;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public <V> V accept(StatementVisitor<V> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <V, S> V accept(StatefulStatementVisitor<V, S> visitor, S state) {
        return visitor.visit(this, state);
    }

    @Override
    public String toString() {
        return String.format("if %s goto %s", expression, target);
    }

    @Override
    public String toString(Map<Label, String> labelNames) {
        if(labelNames.containsKey(target)) {
            return String.format("if %s goto %s", expression, labelNames.get(target));
        } else {
            return toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof IfStatement)) {
            return false;
        }
        IfStatement that = (IfStatement) o;
        if(!expression.equals(that.expression)) {
            return false;
        }
        return target.equals(that.target);
    }

    @Override
    public int hashCode() {
        int result = expression.hashCode();
        result = 31 * result + target.hashCode();
        return result;
    }
}
