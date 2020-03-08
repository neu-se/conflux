package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.InvokeExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;

import java.util.Iterator;

public interface Statement {

    Statement transform(VariableTransformer transformer);

    default boolean definesVariable() {
        return getDefinedVariable() != null;
    }

    VariableExpression getDefinedVariable();

    List<VariableExpression> getUsedVariables();

    default boolean definesExpression() {
        return getDefinedExpression() != null;
    }

    Expression getDefinedExpression();

    List<Expression> getUsedExpressions();

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

    static List<Statement> removeDeadCode(Collection<Statement> statements) {
        List<Statement> copy = new LinkedList<>(statements);
        boolean changed;
        do {
            changed = false;
            Map<Statement, VariableExpression> definingStatements = new HashMap<>();
            Map<VariableExpression, VariableExpression> usedVariables = new HashMap<>();
            for(Statement s : copy) {
                if(s.definesVariable() && s instanceof AssignmentStatement) {
                    Expression rhs = ((AssignmentStatement) s).getRightHandSide();
                    if(!(rhs instanceof InvokeExpression)) {
                        definingStatements.put(s, s.getDefinedVariable());
                    }
                }
                for(VariableExpression use : s.getUsedVariables()) {
                    usedVariables.put(use, use);
                }
            }
            Iterator<Statement> itr = copy.iterator();
            while(itr.hasNext()) {
                Statement s = itr.next();
                if(definingStatements.containsKey(s)) {
                    VariableExpression e = definingStatements.get(s);
                    if(!usedVariables.containsKey(e)) {
                        itr.remove();
                        changed = true;
                    }
                }
            }

        } while(changed);
        return copy;
    }
}
