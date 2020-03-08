package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;

public enum IdleStatement implements Statement {
    NOP, POP, POP2, UNIMPLEMENTED;

    @Override
    public IdleStatement transform(VariableTransformer transformer) {
        return this;
    }

    @Override
    public VariableExpression getDefinedVariable() {
        return null;
    }

    @Override
    public List<VariableExpression> getUsedVariables() {
        return Collections.emptyList();
    }
}
