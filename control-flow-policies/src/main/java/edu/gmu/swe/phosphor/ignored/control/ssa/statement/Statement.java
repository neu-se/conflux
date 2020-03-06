package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.LinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VersionedExpression;

public interface Statement {

    Statement process(Map<VersionedExpression, VersionStack> versionStacks);

    default boolean definesVariable() {
        return definedVariable() != null;
    }

    default VersionedExpression definedVariable() {
        return null;
    }

    default List<VersionedExpression> usedVariables() {
        return Collections.emptyList();
    }

    static List<VersionedExpression> gatherVersionedExpressions(Expression e1) {
        List<VersionedExpression> expressions = new LinkedList<>();
        if(e1 != null) {
            expressions.addAll(e1.referencedVariables());
        }
        return Collections.unmodifiableList(expressions);
    }

    static List<VersionedExpression> gatherVersionedExpressions(Expression e1, Expression e2) {
        List<VersionedExpression> expressions = new LinkedList<>();
        if(e1 != null) {
            expressions.addAll(e1.referencedVariables());
        }
        if(e2 != null) {
            expressions.addAll(e2.referencedVariables());
        }
        return Collections.unmodifiableList(expressions);
    }

    static List<VersionedExpression> gatherVersionedExpressions(Expression e1, Expression e2, Expression e3) {
        List<VersionedExpression> expressions = new LinkedList<>();
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

    static List<VersionedExpression> gatherVersionedExpressions(Expression e1, Expression[] rest) {
        List<VersionedExpression> expressions = new LinkedList<>();
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

    static List<VersionedExpression> gatherVersionedExpressions(Expression[] rest) {
        List<VersionedExpression> expressions = new LinkedList<>();
        for(Expression e : rest) {
            if(e != null) {
                expressions.addAll(e.referencedVariables());
            }
        }
        return Collections.unmodifiableList(expressions);
    }
}
