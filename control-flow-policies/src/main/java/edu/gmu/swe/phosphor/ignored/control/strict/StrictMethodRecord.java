package edu.gmu.swe.phosphor.ignored.control.strict;

import edu.columbia.cs.psl.phosphor.instrumenter.MethodRecord;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.runtime.Taint;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.INVOKESTATIC;
import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public enum StrictMethodRecord implements MethodRecord {

    STRICT_CONTROL_STACK_SET_NEXT_BRANCH_TAG(INVOKEVIRTUAL, StrictControlFlowStack.class, "setNextBranchTag", Void.TYPE, false, Taint.class),
    STRICT_CONTROL_STACK_PUSH(INVOKEVIRTUAL, StrictControlFlowStack.class, "push", boolean[].class, false, boolean[].class, int.class, int.class),
    STRICT_CONTROL_STACK_FACTORY(INVOKESTATIC, StrictControlFlowStack.class, "factory", StrictControlFlowStack.class, false, boolean.class);

    private final int opcode;
    private final String owner;
    private final String name;
    private final String descriptor;
    private final boolean isInterface;
    private final Class<?> returnType;

    StrictMethodRecord(int opcode, Class<?> owner, String name, Class<?> returnType, boolean isInterface, Class<?>... parameterTypes) {
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
