package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.VariableTransformer;

public interface Expression {

    Expression transform(VariableTransformer transformer);

    default Expression transform(VariableTransformer transformer, VariableExpression assignee) {
        return transform(transformer);
    }

    List<VariableExpression> referencedVariables();
}
