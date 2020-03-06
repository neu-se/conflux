package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;

public interface Expression {
    Expression process(Map<VersionedExpression, VersionStack> versionStacks);

    List<VersionedExpression> referencedVariables();
}
