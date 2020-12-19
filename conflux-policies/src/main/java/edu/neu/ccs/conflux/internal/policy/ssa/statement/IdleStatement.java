package edu.neu.ccs.conflux.internal.policy.ssa.statement;

public enum IdleStatement implements Statement {
    NOP, POP, POP2, UNIMPLEMENTED;

    @Override
    public <V> V accept(StatementVisitor<V> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <V, S> V accept(StatefulStatementVisitor<V, S> visitor, S state) {
        return visitor.visit(this, state);
    }
}
