package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public interface ExpressionVisitor<V> {
    V visit(ArrayAccess expression);

    V visit(BinaryExpression expression);

    V visit(CaughtExceptionExpression expression);

    V visit(DoubleConstantExpression expression);

    V visit(FloatConstantExpression expression);

    V visit(IntegerConstantExpression expression);

    V visit(LongConstantExpression expression);

    V visit(ObjectConstantExpression expression);

    V visit(FieldAccess expression);

    V visit(InvokeExpression expression);

    V visit(NewArrayExpression expression);

    V visit(NewExpression expression);

    V visit(ParameterExpression expression);

    V visit(PhiFunction expression);

    V visit(UnaryExpression expression);

    V visit(LocalVariable expression);

    V visit(StackElement expression);
}
