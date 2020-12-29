package edu.neu.ccs.conflux.internal.policy.conflux.tracer;

import edu.columbia.cs.psl.phosphor.control.OpcodesUtil;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.InvokeDynamicInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodInsnNode;

import java.util.function.Predicate;

enum PossibleArrayDefinitionPredicate implements Predicate<AbstractInsnNode> {
    ARRAY_DEFINITION_PREDICATE;

    @Override
    public boolean test(AbstractInsnNode insn) {
        return insn instanceof InvokeDynamicInsnNode
                || insn instanceof MethodInsnNode || OpcodesUtil.isArrayStore(insn.getOpcode());
    }
}
