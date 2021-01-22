package edu.neu.ccs.conflux.internal.policy.data;

import edu.columbia.cs.psl.phosphor.control.ControlFlowAnalyzer;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.InsnList;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.neu.ccs.conflux.internal.runtime.DataOnlyCustomLogic;

import static edu.neu.ccs.conflux.internal.policy.exception.ExceptionMarkingAnalyzer.MARKER;

public class DataOnlyAnalyzer implements ControlFlowAnalyzer {
    @Override
    public void annotate(String owner, MethodNode methodNode) {
        if (methodNode.instructions.size() > 0) {
            replaceInstructions(methodNode.instructions);
            MARKER.annotate(owner, methodNode);
        }
    }

    private void replaceInstructions(InsnList instructions) {
        // Phosphor replaces calls to certain methods (e.g., System.arraycopy) with calls to propagation wrappers.
        // Some of these wrappers determine the propagation logic based on whether the call to the wrapper contains a
        // ControlFlowStack argument.
        // The data-only policy is using a ControlFlowStack in order to propagation from the operands of
        // an instruction that directly triggers an exception to the thrown exception.
        // Thus, Phosphor will use the propagation wrappers that contain implicit/control flow logic for the policy.
        // This is not desirable since the data-only policy is intended to propagation taint tag only along explicit
        // flows.
        // Therefore, we must replace the original calls with our own wrapper to prevent Phosphor from using an
        // incorrect wrapper.
        for (AbstractInsnNode instruction : instructions.toArray()) {
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
                if (isArrayCopy(methodInsnNode)) {
                    methodInsnNode.owner = Type.getInternalName(DataOnlyCustomLogic.class);
                }
            }
        }
    }

    private boolean isArrayCopy(MethodInsnNode instruction) {
        String owner = instruction.owner;
        String name = instruction.name;
        String desc = instruction.desc;
        return (owner.equals("java/lang/System") || owner.equals("java/lang/VMSystem")
                || owner.equals("java/lang/VMMemoryManager"))
                && name.equals("arraycopy")
                && desc.equals("(Ljava/lang/Object;ILjava/lang/Object;II)V");
    }
}
