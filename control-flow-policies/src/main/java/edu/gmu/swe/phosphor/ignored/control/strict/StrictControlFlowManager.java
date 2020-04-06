package edu.gmu.swe.phosphor.ignored.control.strict;

import edu.columbia.cs.psl.phosphor.control.ControlFlowManager;
import edu.columbia.cs.psl.phosphor.control.ControlFlowPropagationPolicy;
import edu.columbia.cs.psl.phosphor.control.ControlFlowStack;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.MethodVisitor;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes;

import static edu.gmu.swe.phosphor.ignored.control.strict.StrictMethodRecord.STRICT_CONTROL_STACK_FACTORY;

public class StrictControlFlowManager implements ControlFlowManager {

    @Override
    public Class<? extends ControlFlowStack> getControlStackClass() {
        return StrictControlFlowStack.class;
    }

    @Override
    public void visitCreateStack(MethodVisitor mv, boolean disabled) {
        mv.visitInsn(disabled ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
        STRICT_CONTROL_STACK_FACTORY.delegateVisit(mv);
    }

    @Override
    public ControlFlowStack getStack(boolean disabled) {
        return StrictControlFlowStack.factory(disabled);
    }

    @Override
    public ControlFlowPropagationPolicy createPropagationPolicy(int access, String owner, String name, String descriptor) {
        return new StrictControlFlowPropagationPolicy(new StrictControlFlowAnalyzer());
    }

    @Override
    public boolean isIgnoredFromControlTrack(String className, String methodName) {
        return false;
    }
}
