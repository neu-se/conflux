package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.VariableTransformer;

public final class BinaryExpression implements Expression {

    private final BinaryOperation operation;
    private final Expression operand1;
    private final Expression operand2;

    public BinaryExpression(BinaryOperation operation, Expression operand1, Expression operand2) {
        if(operation == null || operand1 == null || operand2 == null) {
            throw new NullPointerException();
        }
        this.operation = operation;
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    public BinaryOperation getOperation() {
        return operation;
    }

    public Expression getOperand1() {
        return operand1;
    }

    public Expression getOperand2() {
        return operand2;
    }

    @Override
    public String toString() {
        return operation.format(operand1, operand2);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof BinaryExpression)) {
            return false;
        }
        BinaryExpression that = (BinaryExpression) o;
        if(operation != that.operation) {
            return false;
        }
        if(!operand1.equals(that.operand1)) {
            return false;
        }
        return operand2.equals(that.operand2);
    }

    @Override
    public int hashCode() {
        int result = operation.hashCode();
        result = 31 * result + operand1.hashCode();
        result = 31 * result + operand2.hashCode();
        return result;
    }

    @Override
    public List<VariableExpression> referencedVariables() {
        return Statement.gatherVersionedExpressions(operand1, operand2);
    }

    @Override
    public Expression transform(VariableTransformer transformer) {
        BinaryExpression expr = new BinaryExpression(operation, operand1.transform(transformer),
                operand2.transform(transformer));
        if(transformer.foldingAllowed() && operation.canPerform(expr.operand1, expr.operand2)) {
            return operation.perform(expr.operand1, expr.operand2);
        }
        return expr;
    }
}
