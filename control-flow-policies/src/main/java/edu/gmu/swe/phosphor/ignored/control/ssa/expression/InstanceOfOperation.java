package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.TypeInsnNode;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.INSTANCEOF;

public final class InstanceOfOperation implements UnaryOperation {

    private final String desc;

    public InstanceOfOperation(String desc) {
        if(desc == null) {
            throw new NullPointerException();
        }
        this.desc = desc;
    }

    @Override
    public String toString() {
        return String.format("instanceof %s", desc);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof InstanceOfOperation)) {
            return false;
        }
        InstanceOfOperation that = (InstanceOfOperation) o;
        return desc.equals(that.desc);
    }

    @Override
    public int hashCode() {
        return desc.hashCode();
    }

    @Override
    public String format(Expression expression) {
        return String.format("%s instanceof %s", expression, desc);
    }

    public static InstanceOfOperation getInstance(AbstractInsnNode insn) {
        if(insn.getOpcode() == INSTANCEOF) {
            return new InstanceOfOperation(((TypeInsnNode) insn).desc);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
