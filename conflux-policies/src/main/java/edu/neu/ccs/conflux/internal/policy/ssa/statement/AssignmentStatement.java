package edu.neu.ccs.conflux.internal.policy.ssa.statement;

import edu.neu.ccs.conflux.internal.policy.ssa.expression.Expression;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.VariableExpression;

public final class AssignmentStatement implements Statement {

    private final Expression leftHandSide;
    private final Expression rightHandSide;

    public AssignmentStatement(Expression leftHandSide, Expression rightHandSide) {
        if(leftHandSide == null || rightHandSide == null) {
            throw new NullPointerException();
        }
        this.leftHandSide = leftHandSide;
        this.rightHandSide = rightHandSide;
    }

    public Expression getLeftHandSide() {
        return leftHandSide;
    }

    public Expression getRightHandSide() {
        return rightHandSide;
    }

    @Override
    public boolean definesVariable() {
        return leftHandSide instanceof VariableExpression;
    }

    @Override
    public VariableExpression getDefinedVariable() {
        if(leftHandSide instanceof VariableExpression) {
            return (VariableExpression) leftHandSide;
        } else {
            throw new IllegalStateException();
        }
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
        return String.format("%s = %s", leftHandSide, rightHandSide);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof AssignmentStatement)) {
            return false;
        }
        AssignmentStatement that = (AssignmentStatement) o;
        if(!leftHandSide.equals(that.leftHandSide)) {
            return false;
        }
        return rightHandSide.equals(that.rightHandSide);
    }

    @Override
    public int hashCode() {
        int result = leftHandSide.hashCode();
        result = 31 * result + rightHandSide.hashCode();
        return result;
    }
}
