package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;

public final class GoToStatement implements Statement {

    private final Label target;

    public GoToStatement(Label target) {
        if(target == null) {
            throw new NullPointerException();
        }
        this.target = target;
    }

    public Label getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "goto " + target;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof GoToStatement)) {
            return false;
        }
        GoToStatement that = (GoToStatement) o;
        return target.equals(that.target);
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }

    @Override
    public GoToStatement transform(VariableTransformer transformer) {
        return this;
    }

    @Override
    public VariableExpression getDefinedVariable() {
        return null;
    }

    @Override
    public List<VariableExpression> getUsedVariables() {
        return Collections.emptyList();
    }

    @Override
    public Expression getDefinedExpression() {
        return null;
    }

    @Override
    public List<Expression> getUsedExpressions() {
        return Collections.emptyList();
    }
}
