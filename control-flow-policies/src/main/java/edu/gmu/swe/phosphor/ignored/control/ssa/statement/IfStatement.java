package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.ConditionExpression;

public class IfStatement implements Statement {

    private final ConditionExpression conditionExpression;
    private final Label target;

    public IfStatement(ConditionExpression conditionExpression, Label target) {
        this.conditionExpression = conditionExpression;
        this.target = target;
    }

    @Override
    public String toString() {
        return String.format("if(%s) goto %s", conditionExpression, target);
    }
}
