package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LabelNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LookupSwitchInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.TableSwitchInsnNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Arrays;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;

public final class SwitchStatement implements Statement {

    private final Label defaultLabel;
    private final Label[] labels;
    private final int[] keys;
    private final Expression expression;

    public SwitchStatement(Expression expression, Label defaultLabel, Label[] labels, int[] keys) {
        if(expression == null) {
            throw new NullPointerException();
        }
        this.expression = expression;
        this.defaultLabel = defaultLabel;
        this.labels = labels.clone();
        this.keys = keys.clone();
    }

    SwitchStatement(Expression expression, LabelNode defaultLabel, java.util.Collection<LabelNode> labels) {
        if(expression == null) {
            throw new NullPointerException();
        }
        this.expression = expression;
        this.defaultLabel = defaultLabel == null ? null : defaultLabel.getLabel();
        this.labels = new Label[labels.size()];
        int i = 0;
        for(LabelNode label : labels) {
            this.labels[i++] = label.getLabel();
        }
        this.keys = new int[labels.size()];
    }

    public SwitchStatement(Expression expression, TableSwitchInsnNode insn) {
        this(expression, insn.dflt, insn.labels);
        for(int i = 0; i < keys.length; i++) {
            keys[i] = i + insn.min;
        }
    }

    public SwitchStatement(Expression expression, LookupSwitchInsnNode insn) {
        this(expression, insn.dflt, insn.labels);
        int i = 0;
        for(int key : insn.keys) {
            keys[i++] = key;
        }
    }

    public Label getDefaultLabel() {
        return defaultLabel;
    }

    public Label[] getLabels() {
        return labels.clone();
    }

    public int[] getKeys() {
        return keys.clone();
    }

    public Expression getExpression() {
        return expression;
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
        StringBuilder builder = new StringBuilder("switch(").append(expression).append(") {");
        for(int i = 0; i < labels.length; i++) {
            builder.append("\n\tcase ").append(keys[i]).append(": goto ").append(labels[i]).append(";");
        }
        if(defaultLabel != null) {
            builder.append("\n\tdefault: goto ").append(defaultLabel).append(";");
        }
        return builder.append("\n}").toString();
    }

    @Override
    public String toString(Map<Label, String> labelNames) {
        StringBuilder builder = new StringBuilder("switch(").append(expression).append(") {");
        for(int i = 0; i < labels.length; i++) {
            String labelName = labels[i].toString();
            if(labelNames.containsKey(labels[i])) {
                labelName = labelNames.get(labels[i]);
            }
            builder.append("\n\tcase ").append(keys[i]).append(": goto ").append(labelName).append(";");
        }
        if(defaultLabel != null) {
            String labelName = defaultLabel.toString();
            if(labelNames.containsKey(defaultLabel)) {
                labelName = labelNames.get(defaultLabel);
            }
            builder.append("\n\tdefault: goto ").append(labelName).append(";");
        }
        return builder.append("\n}").toString();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof SwitchStatement)) {
            return false;
        }
        SwitchStatement that = (SwitchStatement) o;
        if(defaultLabel != null ? !defaultLabel.equals(that.defaultLabel) : that.defaultLabel != null) {
            return false;
        }
        if(!Arrays.equals(labels, that.labels)) {
            return false;
        }
        if(!Arrays.equals(keys, that.keys)) {
            return false;
        }
        return expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        int result = defaultLabel != null ? defaultLabel.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(labels);
        result = 31 * result + Arrays.hashCode(keys);
        result = 31 * result + expression.hashCode();
        return result;
    }
}
