package edu.neu.ccs.conflux.internal.policy.ssa.expression;

public interface Expression {
    <V> V accept(ExpressionVisitor<V> visitor);

    <V, S> V accept(StatefulExpressionVisitor<V, ? super S> visitor, S state);
}
