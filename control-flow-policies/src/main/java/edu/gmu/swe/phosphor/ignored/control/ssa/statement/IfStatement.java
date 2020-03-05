package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.ConditionExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VersionedExpression;

public final class IfStatement implements Statement {

    private final ConditionExpression conditionExpression;
    private final Label target;

    public IfStatement(ConditionExpression conditionExpression, Label target) {
        if(conditionExpression == null || target == null) {
            throw new NullPointerException();
        }
        this.conditionExpression = conditionExpression;
        this.target = target;
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
    public IfStatement process(Map<VersionedExpression, VersionStack> versionStacks) {
        return new IfStatement(conditionExpression.process(versionStacks), target);
    }
}
