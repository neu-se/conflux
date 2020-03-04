package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public enum InvocationType {

    INVOKE_VIRTUAL(INVOKEVIRTUAL),
    INVOKE_SPECIAL(INVOKESPECIAL),
    INVOKE_INTERFACE(INVOKEINTERFACE),
    INVOKE_STATIC(INVOKESTATIC),
    INVOKE_DYNAMIC(INVOKEDYNAMIC);

    private final int opcode;

    InvocationType(int opcode) {
        this.opcode = opcode;
    }

    public int getOpcode() {
        return opcode;
    }

    public static InvocationType getInstance(int opcode) {
        switch(opcode) {
            case INVOKEVIRTUAL:
                return INVOKE_VIRTUAL;
            case INVOKESPECIAL:
                return INVOKE_SPECIAL;
            case INVOKESTATIC:
                return INVOKE_STATIC;
            case INVOKEINTERFACE:
                return INVOKE_INTERFACE;
            case INVOKEDYNAMIC:
                return INVOKE_DYNAMIC;
            default:
                throw new IllegalArgumentException();
        }
    }
}
