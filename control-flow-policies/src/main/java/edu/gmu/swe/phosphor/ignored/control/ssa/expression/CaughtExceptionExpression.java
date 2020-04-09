package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.VariableTransformer;

public class CaughtExceptionExpression implements Expression {

    private final int id;

    public CaughtExceptionExpression(int number) {
        this.id = number;
    }

    public int getId() {
        return id;
    }

    @Override
    public List<VariableExpression> referencedVariables() {
        return Collections.emptyList();
    }

    @Override
    public Expression transform(VariableTransformer transformer) {
        return this;
    }

    @Override
    public String toString() {
        return "caughtException" + id;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof CaughtExceptionExpression)) {
            return false;
        }
        CaughtExceptionExpression that = (CaughtExceptionExpression) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
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
