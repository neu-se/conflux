package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LabelNode;

public final class LabelStatement implements Statement {

    private final Label label;

    public LabelStatement(LabelNode insn) {
        label = insn.getLabel();
        if(label == null) {
            throw new NullPointerException();
        }
    }

    @Override
    public String toString() {
        return label.toString();
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
