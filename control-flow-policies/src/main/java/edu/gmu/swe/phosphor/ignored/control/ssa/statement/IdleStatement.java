package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VersionedExpression;

public enum IdleStatement implements Statement {
    NOP, POP, POP2, UNIMPLEMENTED;

    @Override
    public IdleStatement process(Map<VersionedExpression, VersionStack> versionStacks) {
        return this;
    }
}
