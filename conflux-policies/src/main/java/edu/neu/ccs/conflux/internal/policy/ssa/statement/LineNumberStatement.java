package edu.neu.ccs.conflux.internal.policy.ssa.statement;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LineNumberNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;

public final class LineNumberStatement implements Statement {

    private final int line;
    private final Label start;

    public LineNumberStatement(LineNumberNode insn) {
        this.line = insn.line;
        this.start = insn.start.getLabel();
        if(start == null) {
            throw new NullPointerException();
        }
    }

    public int getLine() {
        return line;
    }

    public Label getStart() {
        return start;
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
        return String.format("Line %d - %s", line, start);
    }

    @Override
    public String toString(Map<Label, String> labelNames) {
        if(labelNames.containsKey(start)) {
            return String.format("Line %d - %s", line, labelNames.get(start));
        } else {
            return toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof LineNumberStatement)) {
            return false;
        }
        LineNumberStatement that = (LineNumberStatement) o;
        if(line != that.line) {
            return false;
        }
        return start.equals(that.start);
    }

    @Override
    public int hashCode() {
        int result = line;
        result = 31 * result + start.hashCode();
        return result;
    }
}
