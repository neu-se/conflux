package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;

public interface VariableTransformer {

    boolean foldingAllowed();

    Expression transformUse(VariableExpression expression);

    VariableExpression transformDefinition(VariableExpression expression);
}
