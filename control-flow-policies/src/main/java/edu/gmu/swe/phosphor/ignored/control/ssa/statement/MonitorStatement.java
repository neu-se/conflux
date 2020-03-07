package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.MonitorOperation;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;

public final class MonitorStatement implements Statement {

    private final MonitorOperation operation;
    private final Expression operand;
    private final transient List<VariableExpression> usedVariables;

    public MonitorStatement(MonitorOperation operation, Expression operand) {
        if(operation == null || operand == null) {
            throw new NullPointerException();
        }
        this.operation = operation;
        this.operand = operand;
        usedVariables = Statement.gatherVersionedExpressions(operand);
    }

    public MonitorOperation getOperation() {
        return operation;
    }

    public Expression getOperand() {
        return operand;
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
    public MonitorStatement transform(VariableTransformer transformer) {
        return new MonitorStatement(operation, operand.transform(transformer));
    }

    @Override
    public VariableExpression definedVariable() {
        return null;
    }

    @Override
    public List<VariableExpression> usedVariables() {
        return usedVariables;
    }
}
