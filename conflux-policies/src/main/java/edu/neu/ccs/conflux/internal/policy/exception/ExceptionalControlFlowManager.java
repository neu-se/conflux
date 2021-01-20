package edu.neu.ccs.conflux.internal.policy.exception;

import edu.columbia.cs.psl.phosphor.control.ControlFlowManager;
import edu.columbia.cs.psl.phosphor.control.ControlFlowStack;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.MethodVisitor;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes;
import edu.columbia.cs.psl.phosphor.runtime.StringUtils;

public class ExceptionalControlFlowManager implements ControlFlowManager {
    @Override
    public Class<? extends ControlFlowStack> getControlStackClass() {
        return ExceptionalControlFlowStackImpl.class;
    }

    @Override
    public void visitCreateStack(MethodVisitor mv, boolean disabled) {
        mv.visitInsn(disabled ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
        ExceptionalMethodRecord.EXCEPTION_CONTROL_STACK_FACTORY.delegateVisit(mv);
    }

    @Override
    public ControlFlowStack getStack(boolean disabled) {
        return ExceptionalControlFlowStackImpl.factory(disabled);
    }

    @Override
    public ExceptionalControlFlowPropagationPolicy createPropagationPolicy(int access, String owner, String name,
                                                                           String descriptor) {
        return new ExceptionalControlFlowPropagationPolicy(ExceptionMarkingAnalyzer.MARKER);
    }

    @Override
    public boolean isIgnoredFromControlTrack(String className, String methodName) {
        return false;
    }

    @Override
    public boolean isIgnoredClass(String className) {
        return StringUtils.startsWith(className, "edu/neu/ccs/conflux/internal");
    }

    @Override
    public boolean isInternalTaintingClass(String className) {
        return StringUtils.startsWith(className, "edu/neu/ccs/conflux/internal/runtime");
    }
}
