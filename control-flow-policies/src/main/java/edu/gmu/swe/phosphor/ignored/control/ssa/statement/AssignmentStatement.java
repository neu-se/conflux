package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;

public class AssignmentStatement implements Statement {

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
}
