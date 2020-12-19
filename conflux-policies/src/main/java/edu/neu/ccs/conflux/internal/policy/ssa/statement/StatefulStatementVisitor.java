package edu.neu.ccs.conflux.internal.policy.ssa.statement;

public interface StatefulStatementVisitor<V, S> {
    V visit(AssignmentStatement statement, S state);

    V visit(FrameStatement statement, S state);

    V visit(GoToStatement statement, S state);

    V visit(IdleStatement statement, S state);

    V visit(IfStatement statement, S state);

    V visit(InvokeStatement statement, S state);

    V visit(LabelStatement statement, S state);

    V visit(LineNumberStatement statement, S state);

    V visit(MonitorStatement statement, S state);

    V visit(ReturnStatement statement, S state);

    V visit(SwitchStatement statement, S state);

    V visit(ThrowStatement statement, S state);
}
