package edu.gmu.swe.phosphor.ignored.control.basic;

import edu.columbia.cs.psl.phosphor.control.ControlFlowStack;
import edu.columbia.cs.psl.phosphor.instrumenter.MethodRecord;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;

import static edu.columbia.cs.psl.phosphor.Configuration.TAINT_TAG_OBJ_CLASS;
import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.INVOKESTATIC;
import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public enum BasicMethodRecord implements MethodRecord {

    CONTROL_STACK_COPY_TAG(INVOKEVIRTUAL, ControlFlowStack.class, "copyTag", TAINT_TAG_OBJ_CLASS, false),
    BASIC_CONTROL_STACK_PUSH(INVOKEVIRTUAL, BasicControlFlowStack.class, "push", boolean[].class, false, TAINT_TAG_OBJ_CLASS, boolean[].class, int.class, int.class),
    BASIC_CONTROL_STACK_POP(INVOKEVIRTUAL, BasicControlFlowStack.class, "pop", Void.TYPE, false, boolean[].class, int.class),
    BASIC_CONTROL_STACK_POP_ALL(INVOKEVIRTUAL, BasicControlFlowStack.class, "pop", Void.TYPE, false, boolean[].class),
    BASIC_CONTROL_STACK_FACTORY(INVOKESTATIC, BasicControlFlowStack.class, "factory", BasicControlFlowStack.class, false, boolean.class);

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
    BasicMethodRecord(int opcode, Class<?> owner, String name, Class<?> returnType, boolean isInterface, Class<?>... parameterTypes) {
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
