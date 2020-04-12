package edu.gmu.swe.phosphor.ignored.control.binding.tracer;

import edu.columbia.cs.psl.phosphor.control.OpcodesUtil;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.InvokeDynamicInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodInsnNode;

import java.util.function.Predicate;

enum PossibleFieldDefinitionPredicate implements Predicate<AbstractInsnNode> {
    FIELD_DEFINITION_PREDICATE;

    @Override
    public boolean test(AbstractInsnNode insn) {
        return insn instanceof InvokeDynamicInsnNode
                || insn instanceof MethodInsnNode || OpcodesUtil.isFieldStoreInsn(insn.getOpcode());
    }
}
