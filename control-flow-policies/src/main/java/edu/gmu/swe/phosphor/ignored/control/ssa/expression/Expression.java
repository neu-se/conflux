package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public interface Expression {
    <V> V accept(ExpressionVisitor<V> visitor);

    <V, S> V accept(StatefulExpressionVisitor<V, ? super S> visitor, S state);
}
