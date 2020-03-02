package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;

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
