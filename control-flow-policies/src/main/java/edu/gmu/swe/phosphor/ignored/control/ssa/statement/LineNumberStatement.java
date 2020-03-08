package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LineNumberNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;

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
    public String toString() {
        return String.format("Line %d - %s", line, start);
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

    @Override
    public LineNumberStatement transform(VariableTransformer transformer) {
        return this;
    }

    @Override
    public VariableExpression getDefinedVariable() {
        return null;
    }

    @Override
    public List<VariableExpression> getUsedVariables() {
        return Collections.emptyList();
    }
}
