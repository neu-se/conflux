package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.VariableTransformer;

public class ParameterExpression implements Expression {

    private final int parameterNumber;

    public ParameterExpression(int parameterNumber) {
        this.parameterNumber = parameterNumber;
    }

    public int getParameterNumber() {
        return parameterNumber;
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
        return "param" + parameterNumber;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof ParameterExpression)) {
            return false;
        }
        ParameterExpression that = (ParameterExpression) o;
        return parameterNumber == that.parameterNumber;
    }

    @Override
    public int hashCode() {
        return parameterNumber;
    }
}
