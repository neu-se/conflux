package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.VariableTransformer;

public interface Expression {

    Expression transform(VariableTransformer transformer);
    List<VariableExpression> referencedVariables();
}
