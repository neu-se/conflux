package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

public class PhiFunction implements Expression {

    private final VersionedExpression[] values;

    public PhiFunction(Set<VersionedExpression> values) {
        this.values = values.toArray(new VersionedExpression[0]);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("phi<");
        for(int i = 0; i < values.length; i++) {
            builder.append(values[i]);
            if((i + 1) < values.length) {
                builder.append(", ");
            }
        }
        return builder.append(">").toString();
    }

    @Override
    public PhiFunction process(Map<VersionedExpression, VersionStack> versionStacks) {
        return this;
    }

    @Override
    public List<VersionedExpression> referencedVariables() {
        return Statement.gatherVersionedExpressions(values);
    }
}
