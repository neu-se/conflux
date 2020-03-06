package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VersionedExpression;

public final class AssignmentStatement implements Statement {

    private final Expression leftHandSide;
    private final Expression rightHandSide;
    private final transient VersionedExpression definedVariable;
    private final transient List<VersionedExpression> usedVariables;

    public AssignmentStatement(Expression leftHandSide, Expression rightHandSide) {
        if(leftHandSide == null || rightHandSide == null) {
            throw new NullPointerException();
        }
        this.leftHandSide = leftHandSide;
        this.rightHandSide = rightHandSide;
        if(leftHandSide instanceof VersionedExpression) {
            definedVariable = (VersionedExpression) leftHandSide;
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
    public AssignmentStatement process(Map<VersionedExpression, VersionStack> versionStacks) {
        Expression newRightHandSide = rightHandSide.process(versionStacks);
        Expression newLeftHandSide;
        if(versionStacks.containsKey(leftHandSide)) {
            newLeftHandSide = versionStacks.get(leftHandSide).createNewVersion();
        } else {
            newLeftHandSide = leftHandSide.process(versionStacks);
        }
        return new AssignmentStatement(newLeftHandSide, newRightHandSide);
    }

    @Override
    public VersionedExpression definedVariable() {
        return definedVariable;
    }

    @Override
    public List<VersionedExpression> usedVariables() {
        return usedVariables;
    }
}
