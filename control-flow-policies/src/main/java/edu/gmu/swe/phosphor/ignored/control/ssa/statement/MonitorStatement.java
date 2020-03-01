package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.MonitorOperation;

public class MonitorStatement implements Statement {

    private final MonitorOperation operation;
    private final Expression expression;

    public MonitorStatement(MonitorOperation operation, Expression expression) {
        this.operation = operation;
        this.expression = expression;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", operation, expression);
    }
}
