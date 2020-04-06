package edu.gmu.swe.phosphor.ignored.control.basic;

import edu.columbia.cs.psl.phosphor.control.ControlFlowManager;
import edu.columbia.cs.psl.phosphor.control.ControlFlowPropagationPolicy;
import edu.columbia.cs.psl.phosphor.control.ControlFlowStack;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.MethodVisitor;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes;

import static edu.gmu.swe.phosphor.ignored.control.basic.BasicMethodRecord.BASIC_CONTROL_STACK_FACTORY;

public class BasicControlFlowManager implements ControlFlowManager {

    @Override
    public Class<? extends ControlFlowStack> getControlStackClass() {
        return BasicControlFlowStack.class;
    }

    @Override
    public void visitCreateStack(MethodVisitor mv, boolean disabled) {
        mv.visitInsn(disabled ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
        BASIC_CONTROL_STACK_FACTORY.delegateVisit(mv);
    }

    @Override
    public ControlFlowStack getStack(boolean disabled) {
        return BasicControlFlowStack.factory(disabled);
    }

    @Override
    public ControlFlowPropagationPolicy createPropagationPolicy(int access, String owner, String name, String descriptor) {
        return new BasicControlFlowPropagationPolicy(new BasicControlFlowAnalyzer());
    }
}
