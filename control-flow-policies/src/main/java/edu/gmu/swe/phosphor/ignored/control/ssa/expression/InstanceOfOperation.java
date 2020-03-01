package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.TypeInsnNode;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.INSTANCEOF;

public class InstanceOfOperation implements UnaryOperation {

    private final String desc;

    private InstanceOfOperation(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return String.format("instanceof %s", desc);
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
