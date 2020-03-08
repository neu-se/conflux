package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LabelNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;

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

    @Override
    public LabelStatement transform(VariableTransformer transformer) {
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
