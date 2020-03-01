package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LineNumberNode;

public class LineNumberStatement implements Statement {

    private final int line;
    private final Label start;

    public LineNumberStatement(LineNumberNode insn) {
        this.line = insn.line;
        this.start = insn.start.getLabel();
    }

    @Override
    public String toString() {
        return String.format("Line %d - %s", line, start);
    }
}
