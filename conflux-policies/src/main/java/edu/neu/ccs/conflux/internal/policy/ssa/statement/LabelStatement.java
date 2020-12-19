package edu.neu.ccs.conflux.internal.policy.ssa.statement;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LabelNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;

public final class LabelStatement implements Statement {

    private final Label label;

    public LabelStatement(Label label) {
        if(label == null) {
            throw new NullPointerException();
        }
        this.label = label;
    }

    public LabelStatement(LabelNode insn) {
        label = insn.getLabel();
        if(label == null) {
            throw new NullPointerException();
        }
    }

    public Label getLabel() {
        return label;
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
        return label.toString();
    }

    @Override
    public String toString(Map<Label, String> labelNames) {
        if(labelNames.containsKey(label)) {
            return labelNames.get(label);
        } else {
            return toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof LabelStatement)) {
            return false;
        }
        LabelStatement that = (LabelStatement) o;
        return label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return label.hashCode();
    }
}
