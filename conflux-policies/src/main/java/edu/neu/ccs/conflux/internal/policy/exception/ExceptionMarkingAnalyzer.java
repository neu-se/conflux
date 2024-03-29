package edu.neu.ccs.conflux.internal.policy.exception;

import edu.columbia.cs.psl.phosphor.control.ControlFlowAnalyzer;
import edu.columbia.cs.psl.phosphor.control.standard.ExceptionHandlerStart;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.*;

import java.util.List;

import static edu.neu.ccs.conflux.internal.policy.FlowGraphUtil.findNextPrecedableInstruction;

public enum ExceptionMarkingAnalyzer implements ControlFlowAnalyzer {
    MARKER;

    @Override
    public void annotate(String owner, MethodNode methodNode) {
        if (methodNode.instructions.size() > 0) {
            markExceptionHandlerStarts(methodNode.tryCatchBlocks, methodNode.instructions);
            markExceptionThrowingInstructions(methodNode.instructions);
        }
    }

    private void markExceptionHandlerStarts(List<TryCatchBlockNode> exceptionHandlers, InsnList instructions) {
        for (TryCatchBlockNode handler : exceptionHandlers) {
            instructions.insertBefore(findNextPrecedableInstruction(handler.handler),
                    new LdcInsnNode(new ExceptionHandlerStart(handler.type)));
        }
    }

    private void markExceptionThrowingInstructions(InsnList instructions) {
        for (AbstractInsnNode instruction : instructions.toArray()) {
            if (MaybeThrownException.mayThrowException(instruction.getOpcode())) {
                instructions.insertBefore(instruction,
                        new LdcInsnNode(MaybeThrownException.getInstance(instruction)));
            }
        }
    }
}
