package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
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
        if(currentDefinitions.containsKey(expression)) {
            return currentDefinitions.get(expression);
        } else {
            return expression;
        }
    }

    @Override
    public VariableExpression transformDefinition(VariableExpression expression) {
        return expression;
    }
}
