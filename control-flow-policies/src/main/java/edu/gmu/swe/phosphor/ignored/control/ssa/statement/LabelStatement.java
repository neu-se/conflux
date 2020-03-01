package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LabelNode;

public class LabelStatement implements Statement {

    private final Label label;

    public LabelStatement(LabelNode insn) {
        label = insn.getLabel();
    }

    @Override
    public String toString() {
        return label.toString();
    }
}
