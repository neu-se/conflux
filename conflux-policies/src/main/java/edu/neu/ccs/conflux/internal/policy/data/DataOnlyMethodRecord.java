package edu.neu.ccs.conflux.internal.policy.data;

import edu.columbia.cs.psl.phosphor.instrumenter.MethodRecord;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.INVOKESTATIC;

enum DataOnlyMethodRecord implements MethodRecord {
    EXCEPTIONAL_CONTROL_STACK_FACTORY(INVOKESTATIC, ExceptionalControlFlowStackImpl.class, "factory", ExceptionalControlFlowStackImpl.class, false, boolean.class);

    private final int opcode;
    private final String owner;
    private final String name;
    private final String descriptor;
    private final boolean isInterface;
    private final Class<?> returnType;

    DataOnlyMethodRecord(int opcode, Class<?> owner, String name, Class<?> returnType, boolean isInterface,
                         Class<?>... parameterTypes) {
        this.opcode = opcode;
        this.owner = Type.getInternalName(owner);
        this.name = name;
        this.isInterface = isInterface;
        this.descriptor = MethodRecord.createDescriptor(returnType, parameterTypes);
        this.returnType = returnType;
    }

    @Override
    public int getOpcode() {
        return opcode;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescriptor() {
        return descriptor;
    }

    @Override
    public boolean isInterface() {
        return isInterface;
    }

    @Override
    public Class<?> getReturnType() {
        return returnType;
    }
}
