package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LabelNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LookupSwitchInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.TableSwitchInsnNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;

import java.util.Arrays;
import java.util.Collection;

public final class SwitchStatement implements Statement {

    private final Label defaultLabel;
    private final Label[] labels;
    private final int[] keys;
    private final Expression value;

    SwitchStatement(Expression value, LabelNode defaultLabel, Collection<LabelNode> labels) {
        if(value == null) {
            throw new NullPointerException();
        }
        this.value = value;
        this.defaultLabel = defaultLabel == null ? null : defaultLabel.getLabel();
        this.labels = new Label[labels.size()];
        int i = 0;
        for(LabelNode label : labels) {
            this.labels[i] = label.getLabel();
        }
        this.keys = new int[labels.size()];
    }

    public SwitchStatement(Expression value, TableSwitchInsnNode insn) {
        this(value, insn.dflt, insn.labels);
        for(int i = 0; i < keys.length; i++) {
            keys[i] = i + insn.min;
        }
    }

    public SwitchStatement(Expression value, LookupSwitchInsnNode insn) {
        this(value, insn.dflt, insn.labels);
        int i = 0;
        for(int key : insn.keys) {
            keys[i++] = key;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("switch(").append(value).append(") {");
        for(int i = 0; i < labels.length; i++) {
            builder.append("\n\tcase ").append(keys[i]).append(": goto").append(labels[i]).append(";");
        }
        if(defaultLabel != null) {
            builder.append("\n\tdefault: goto ").append(defaultLabel).append(";");
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
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        int result = defaultLabel != null ? defaultLabel.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(labels);
        result = 31 * result + Arrays.hashCode(keys);
        result = 31 * result + value.hashCode();
        return result;
    }
}
