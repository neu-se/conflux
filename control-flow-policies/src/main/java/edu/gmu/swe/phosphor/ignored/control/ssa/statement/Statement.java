package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.LinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;

public interface Statement {

    Statement transform(VariableTransformer transformer);

    default boolean definesVariable() {
        return definedVariable() != null;
    }

    VariableExpression definedVariable();

    List<VariableExpression> usedVariables();

    static List<VariableExpression> gatherVersionedExpressions(Expression e1) {
        List<VariableExpression> expressions = new LinkedList<>();
        if(e1 != null) {
            expressions.addAll(e1.referencedVariables());
        }
        return Collections.unmodifiableList(expressions);
    }

    static List<VariableExpression> gatherVersionedExpressions(Expression e1, Expression e2) {
        List<VariableExpression> expressions = new LinkedList<>();
        if(e1 != null) {
            expressions.addAll(e1.referencedVariables());
        }
        if(e2 != null) {
            expressions.addAll(e2.referencedVariables());
        }
        return Collections.unmodifiableList(expressions);
    }

    static List<VariableExpression> gatherVersionedExpressions(Expression e1, Expression e2, Expression e3) {
        List<VariableExpression> expressions = new LinkedList<>();
        if(e1 != null) {
            expressions.addAll(e1.referencedVariables());
        }
        if(e2 != null) {
            expressions.addAll(e2.referencedVariables());
        }
        if(e3 != null) {
            expressions.addAll(e3.referencedVariables());
        }
        return Collections.unmodifiableList(expressions);
    }

    static List<VariableExpression> gatherVersionedExpressions(Expression e1, Expression[] rest) {
        List<VariableExpression> expressions = new LinkedList<>();
        if(e1 != null) {
            expressions.addAll(e1.referencedVariables());
        }
        for(Expression e : rest) {
            if(e != null) {
                expressions.addAll(e.referencedVariables());
            }
        }
        return Collections.unmodifiableList(expressions);
    }

    static List<VariableExpression> gatherVersionedExpressions(Expression[] rest) {
        List<VariableExpression> expressions = new LinkedList<>();
        for(Expression e : rest) {
            if(e != null) {
                expressions.addAll(e.referencedVariables());
            }
        }
        return Collections.unmodifiableList(expressions);
    }
}
