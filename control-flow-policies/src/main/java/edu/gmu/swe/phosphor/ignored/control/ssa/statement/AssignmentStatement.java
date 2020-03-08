package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;

public final class AssignmentStatement implements Statement {

    private final Expression leftHandSide;
    private final Expression rightHandSide;
    private final transient VariableExpression definedVariable;
    private final transient List<VariableExpression> usedVariables;

    public AssignmentStatement(Expression leftHandSide, Expression rightHandSide) {
        if(leftHandSide == null || rightHandSide == null) {
            throw new NullPointerException();
        }
        this.leftHandSide = leftHandSide;
        this.rightHandSide = rightHandSide;
        if(leftHandSide instanceof VariableExpression) {
            definedVariable = (VariableExpression) leftHandSide;
            usedVariables = Statement.gatherVersionedExpressions(rightHandSide);

        } else {
            definedVariable = null;
            usedVariables = Statement.gatherVersionedExpressions(leftHandSide, rightHandSide);
        }
    }

    public Expression getLeftHandSide() {
        return leftHandSide;
    }

    public Expression getRightHandSide() {
        return rightHandSide;
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

    @Override
    public AssignmentStatement transform(VariableTransformer transformer) {
        Expression newRightHandSide;
        Expression newLeftHandSide;
        if(leftHandSide instanceof VariableExpression) {
            newRightHandSide = rightHandSide.transform(transformer, (VariableExpression) leftHandSide);
        } else {
            newRightHandSide = rightHandSide.transform(transformer);
        }
        if(leftHandSide instanceof VariableExpression) {
            newLeftHandSide = transformer.transformDefinition((VariableExpression) leftHandSide);
        } else {
            newLeftHandSide = leftHandSide.transform(transformer);
        }
        return new AssignmentStatement(newLeftHandSide, newRightHandSide);
    }

    @Override
    public VariableExpression getDefinedVariable() {
        return definedVariable;
    }

    @Override
    public List<VariableExpression> getUsedVariables() {
        return usedVariables;
    }

    @Override
    public Expression getDefinedExpression() {
        return leftHandSide;
    }

    @Override
    public List<Expression> getUsedExpressions() {
        return Collections.singletonList(rightHandSide);
    }
}
