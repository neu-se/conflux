package edu.neu.ccs.conflux.internal.policy.conflux;

import edu.columbia.cs.psl.phosphor.instrumenter.MethodRecord;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.runtime.Taint;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.INVOKESTATIC;
import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public enum ConfluxMethodRecord implements MethodRecord {

    CONFLUX_CONTROL_STACK_START_FRAME(INVOKEVIRTUAL, ConfluxControlFlowStack.class, "startFrame", ConfluxControlFlowStack.class, false, int.class, int.class),
    CONFLUX_CONTROL_STACK_SET_ARG_STABLE(INVOKEVIRTUAL, ConfluxControlFlowStack.class, "setNextFrameArgStable", ConfluxControlFlowStack.class, false),
    CONFLUX_CONTROL_STACK_SET_ARG_DEPENDENT(INVOKEVIRTUAL, ConfluxControlFlowStack.class, "setNextFrameArgDependent", ConfluxControlFlowStack.class, false, int[].class),
    CONFLUX_CONTROL_STACK_SET_ARG_UNSTABLE(INVOKEVIRTUAL, ConfluxControlFlowStack.class, "setNextFrameArgUnstable", ConfluxControlFlowStack.class, false, int.class),
    CONFLUX_CONTROL_STACK_COPY_TAG_STABLE(INVOKEVIRTUAL, ConfluxControlFlowStack.class, "copyTagStable", Taint.class, false),
    CONFLUX_CONTROL_STACK_COPY_TAG_DEPENDENT(INVOKEVIRTUAL, ConfluxControlFlowStack.class, "copyTagDependent", Taint.class, false, int[].class),
    CONFLUX_CONTROL_STACK_COPY_TAG_UNSTABLE(INVOKEVIRTUAL, ConfluxControlFlowStack.class, "copyTagUnstable", Taint.class, false, int.class),
    CONFLUX_CONTROL_STACK_PUSH_STABLE(INVOKEVIRTUAL, ConfluxControlFlowStack.class, "pushStable", Void.TYPE, false, int.class, int.class),
    CONFLUX_CONTROL_STACK_PUSH_DEPENDENT(INVOKEVIRTUAL, ConfluxControlFlowStack.class, "pushDependent", Void.TYPE, false, int.class, int.class, int[].class),
    CONFLUX_CONTROL_STACK_PUSH_UNSTABLE(INVOKEVIRTUAL, ConfluxControlFlowStack.class, "pushUnstable", Void.TYPE, false, int.class, int.class, int.class),
    CONFLUX_CONTROL_STACK_SET_NEXT_BRANCH_TAG(INVOKEVIRTUAL, ConfluxControlFlowStack.class, "setNextBranchTag", Void.TYPE, false, Taint.class),
    CONFLUX_CONTROL_STACK_EXIT_LOOP_LEVEL(INVOKEVIRTUAL, ConfluxControlFlowStack.class, "exitLoopLevel", Void.TYPE, false, int.class),
    CONFLUX_CONTROL_STACK_POP(INVOKEVIRTUAL, ConfluxControlFlowStack.class, "pop", Void.TYPE, false, int.class),
    CONFLUX_CONTROL_STACK_FACTORY(INVOKESTATIC, ConfluxControlFlowStack.class, "factory", ConfluxControlFlowStack.class, false, boolean.class);

    private final int opcode;
    private final String owner;
    private final String name;
    private final String descriptor;
    private final boolean isInterface;
    private final Class<?> returnType;

    /**
     * Constructs a new method.
     *
     * @param opcode         the opcode of the type instruction associated with the method
     * @param owner          the internal name of the method's owner class
     * @param name           the method's name
     * @param returnType     the class of the method's return type
     * @param isInterface    if the method's owner class is an interface
     * @param parameterTypes the types of the parameters of the method
     */
    ConfluxMethodRecord(int opcode, Class<?> owner, String name, Class<?> returnType, boolean isInterface, Class<?>... parameterTypes) {
        this.opcode = opcode;
        this.owner = Type.getInternalName(owner);
        this.name = name;
        this.isInterface = isInterface;
        this.descriptor = MethodRecord.createDescriptor(returnType, parameterTypes);
        this.returnType = returnType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOpcode() {
        return opcode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOwner() {
        return owner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescriptor() {
        return descriptor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInterface() {
        return isInterface;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getReturnType() {
        return returnType;
    }
}
