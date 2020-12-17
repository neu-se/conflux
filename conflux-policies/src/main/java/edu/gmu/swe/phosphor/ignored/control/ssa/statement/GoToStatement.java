package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;

public final class GoToStatement implements Statement {

    private final Label target;

    public GoToStatement(Label target) {
        if(target == null) {
            throw new NullPointerException();
        }
        this.target = target;
    }

    public Label getTarget() {
        return target;
    }

    @Override
    public <V> V accept(StatementVisitor<V> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <V, S> V accept(StatefulStatementVisitor<V, S> visitor, S state) {
        return visitor.visit(this, state);
    }

    @Override
    public String toString() {
        return "goto " + target;
    }

    @Override
    public String toString(Map<Label, String> labelNames) {
        if(labelNames.containsKey(target)) {
            return "goto " + labelNames.get(target);
        } else {
            return toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof GoToStatement)) {
            return false;
        }
        GoToStatement that = (GoToStatement) o;
        return target.equals(that.target);
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }
}
