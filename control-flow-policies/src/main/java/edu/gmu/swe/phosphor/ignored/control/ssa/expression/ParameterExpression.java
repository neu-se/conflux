package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;

public class ParameterExpression implements Expression {

    private final int parameterNumber;

    public ParameterExpression(int parameterNumber) {
        this.parameterNumber = parameterNumber;
    }

    @Override
    public Expression process(Map<VersionedExpression, VersionStack> versionStacks) {
        return this;
    }

    @Override
    public List<VersionedExpression> referencedVariables() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "parameter " + parameterNumber;
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
