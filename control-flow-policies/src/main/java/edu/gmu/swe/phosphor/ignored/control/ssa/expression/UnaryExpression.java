package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.VariableTransformer;

public final class UnaryExpression implements Expression {

    private final UnaryOperation operation;
    private final Expression operand;

    public UnaryExpression(UnaryOperation operation, Expression operand) {
        if(operation == null || operand == null) {
            throw new NullPointerException();
        }
        this.operation = operation;
        this.operand = operand;
    }

    public UnaryOperation getOperation() {
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
        } else if(!(o instanceof UnaryExpression)) {
            return false;
        }
        UnaryExpression that = (UnaryExpression) o;
        if(!operation.equals(that.operation)) {
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
    public List<VariableExpression> referencedVariables() {
        return Statement.gatherVersionedExpressions(operand);
    }

    @Override
    public Expression transform(VariableTransformer transformer) {
        UnaryExpression expr = new UnaryExpression(operation, operand.transform(transformer));
        if(transformer.foldingAllowed() && operation.canPerform(expr.operand)) {
            return operation.perform(expr.operand);
        }
        return expr;
    }

    @Override
    public <V> V accept(ExpressionVisitor<V> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <V, S> V accept(StatefulExpressionVisitor<V, ? super S> visitor, S state) {
        return visitor.visit(this, state);
    }
}
