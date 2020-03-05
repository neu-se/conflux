package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.MonitorOperation;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VersionedExpression;

public final class MonitorStatement implements Statement {

    private final MonitorOperation operation;
    private final Expression operand;

    public MonitorStatement(MonitorOperation operation, Expression operand) {
        if(operation == null || operand == null) {
            throw new NullPointerException();
        }
        this.operation = operation;
        this.operand = operand;
    }

    @Override
    public String toString() {
        return operation.format(operand);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof MonitorStatement)) {
            return false;
        }
        MonitorStatement that = (MonitorStatement) o;
        if(operation != that.operation) {
            return false;
        }
        return operand.equals(that.operand);
    }

    @Override
    public int hashCode() {
        int result = operation.hashCode();
        result = 31 * result + operand.hashCode();
        return result;
    }

    @Override
    public MonitorStatement process(Map<VersionedExpression, VersionStack> versionStacks) {
        return new MonitorStatement(operation, operand.process(versionStacks));
    }
}
