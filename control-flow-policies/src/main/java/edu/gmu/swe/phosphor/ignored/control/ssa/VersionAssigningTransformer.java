package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.VariableTransformer;

public final class VersionAssigningTransformer implements VariableTransformer {

    private final Map<VariableExpression, VersionStack> versionStacks;

    public VersionAssigningTransformer(Map<VariableExpression, VersionStack> versionStacks) {
        this.versionStacks = versionStacks;
    }

    @Override
    public boolean foldingAllowed() {
        return false;
    }

    @Override
    public VariableExpression transformUse(VariableExpression expression) {
        if(versionStacks.containsKey(expression)) {
            return versionStacks.get(expression).getCurrentExpression();
        } else {
            return expression;
        }
    }

    @Override
    public VariableExpression transformDefinition(VariableExpression expression) {
        return versionStacks.get(expression).createNewVersion();
    }
}
