package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

public interface StatementVisitor<V> {
    V visit(AssignmentStatement statement);

    V visit(FrameStatement statement);

    V visit(GoToStatement statement);

    V visit(IdleStatement statement);

    V visit(IfStatement statement);

    V visit(InvokeStatement statement);

    V visit(LabelStatement statement);

    V visit(LineNumberStatement statement);

    V visit(MonitorStatement statement);

    V visit(ReturnStatement statement);

    V visit(SwitchStatement statement);

    V visit(ThrowStatement statement);
}
