package edu.neu.ccs.conflux.internal.policy.ssa.expression;

public interface StatefulExpressionVisitor<V, S> {
    V visit(ArrayAccess expression, S state);

    V visit(BinaryExpression expression, S state);

    V visit(CaughtExceptionExpression expression, S state);

    V visit(DoubleConstantExpression expression, S state);

    V visit(FloatConstantExpression expression, S state);

    V visit(IntegerConstantExpression expression, S state);

    V visit(LongConstantExpression expression, S state);

    V visit(ObjectConstantExpression expression, S state);

    V visit(FieldAccess expression, S state);

    V visit(InvokeExpression expression, S state);

    V visit(NewArrayExpression expression, S state);

    V visit(NewExpression expression, S state);

    V visit(ParameterExpression expression, S state);

    V visit(PhiFunction expression, S state);

    V visit(UnaryExpression expression, S state);

    V visit(LocalVariable expression, S state);

    V visit(StackElement expression, S state);
}
