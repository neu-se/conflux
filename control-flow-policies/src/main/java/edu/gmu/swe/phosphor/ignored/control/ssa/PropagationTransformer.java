package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.PhiFunction;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.VariableTransformer;

public class PropagationTransformer implements VariableTransformer {

    private final Map<VariableExpression, Expression> currentDefinitions;

    public PropagationTransformer(Map<VariableExpression, Expression> currentDefinitions) {
        this.currentDefinitions = currentDefinitions;
    }

    @Override
    public boolean foldingAllowed() {
        return true;
    }

    @Override
    public Expression transformUse(VariableExpression expression) {
        Expression currentDefinition = currentDefinitions.get(expression);
        if(currentDefinition instanceof PhiFunction) {
            return expression;
        } else {
            return currentDefinition;
        }
    }

    @Override
    public VariableExpression transformDefinition(VariableExpression expression) {
        return expression;
    }
}
