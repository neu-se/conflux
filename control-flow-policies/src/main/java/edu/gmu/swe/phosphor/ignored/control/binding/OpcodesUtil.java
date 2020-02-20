package edu.gmu.swe.phosphor.ignored.control.binding;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.*;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;


public class OpcodesUtil {

    /**
     * @param opcode the opcode to be checked
     * @return true if the operation associated with the specified opcode stores a value into a field
     */
    public static boolean isFieldStoreInsn(int opcode) {
        return opcode == PUTFIELD || opcode == PUTSTATIC;
    }

    /**
     * @param opcode the opcode to be checked
     * @return true if the operation associated with the specified opcode loads a value from a field
     */
    public static boolean isFieldLoadInsn(int opcode) {
        return opcode == GETFIELD || opcode == GETSTATIC;
    }

    /**
     * @param opcode the opcode to be checked
     * @return true if the operation associated with the specified opcode stores a value into an array
     */
    public static boolean isArrayStore(int opcode) {
        return opcode >= IASTORE && opcode <= SASTORE;
    }

    /**
     * @param opcode the opcode to be checked
     * @return true if the operation associated with the specified opcode loads a value from an array
     */
    public static boolean isArrayLoad(int opcode) {
        return opcode >= IALOAD && opcode <= SALOAD;
    }

    /**
     * @param opcode the opcode to be checked
     * @return true if the operation associated with the specified opcode stores a value into a local variable
     */
    public static boolean isLocalVariableStoreInsn(int opcode) {
        return opcode >= ISTORE && opcode <= ASTORE;
    }

    /**
     * @param opcode the opcode to be checked
     * @return true if the operation associated with the specified opcode pushes a constant onto the runtime stack
     */
    public static boolean isPushConstantOpcode(int opcode) {
        return opcode >= ACONST_NULL && opcode <= LDC;
    }

    /**
     * @param opcode the opcode to be checked
     * @return true if the operation associated with the specified opcode performs an arithmetic or logical computation
     * runtime stack and pushes the result onto the runtime stack
     */
    public static boolean isArithmeticOrLogicalInsn(int opcode) {
        return opcode >= IADD && opcode <= DCMPG;
    }

    /**
     * @param opcode the opcode to be checked
     * @return true if the specified opcode is associated with a return operation
     */
    public static boolean isReturnOpcode(int opcode) {
        switch(opcode) {
            case ARETURN:
            case IRETURN:
            case RETURN:
            case DRETURN:
            case FRETURN:
            case LRETURN:
                return true;
            default:
                return false;
        }
    }

    /**
     * @param insn the instruction whose associated type's size is being calculated
     * @return the size of the type associated with the specified instruction
     */
    public static int getSize(AbstractInsnNode insn) {
        switch(insn.getOpcode()) {
            case LCONST_0:
            case LCONST_1:
            case DCONST_0:
            case DCONST_1:
            case LALOAD:
            case DALOAD:
            case LADD:
            case DADD:
            case LSUB:
            case DSUB:
            case LMUL:
            case DMUL:
            case LDIV:
            case DDIV:
            case LREM:
            case DREM:
            case LSHL:
            case LSHR:
            case LUSHR:
            case LAND:
            case LOR:
            case LXOR:
            case LNEG:
            case DNEG:
            case I2L:
            case I2D:
            case L2D:
            case F2L:
            case F2D:
            case D2L:
                return 2;
            case LDC:
                Object value = ((LdcInsnNode) insn).cst;
                return value instanceof Long || value instanceof Double ? 2 : 1;
            case GETSTATIC:
            case GETFIELD:
                return Type.getType(((FieldInsnNode) insn).desc).getSize();
            case INVOKEDYNAMIC:
                return Type.getReturnType(((InvokeDynamicInsnNode) insn).desc).getSize();
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
            case INVOKESTATIC:
            case INVOKEINTERFACE:
                return Type.getReturnType(((MethodInsnNode) insn).desc).getSize();
            default:
                return 1;
        }
    }

    public static boolean couldThrowException(int opcode, String type) {
        if(type == null) {
            return true;
        }
        switch(type) {
            case "java/lang/Throwable":
            case "java/lang/Error":
            case "java/lang/VirtualMachineError":
            case "java/lang/InternalError":
            case "java/lang/OutOfMemoryError":
            case "java/lang/StackOverflowError":
            case "java/lang/UnknownError":
                return true;
            case "java/lang/Exception":
            case "java/lang/RuntimeException":
                switch(opcode) {
                    case ANEWARRAY:
                    case MULTIANEWARRAY:
                    case NEWARRAY:
                    case ARRAYLENGTH:
                    case ATHROW:
                    case CHECKCAST:
                    case GETFIELD:
                    case IDIV:
                    case IREM:
                    case LDIV:
                    case LREM:
                    case INVOKEINTERFACE:
                    case INVOKESPECIAL:
                    case INVOKEVIRTUAL:
                    case MONITORENTER:
                    case MONITOREXIT:
                    case PUTFIELD:
                        return true;
                    default:
                        return isReturnOpcode(opcode) || isArrayStore(opcode) || isArrayLoad(opcode);
                }
            case "java/lang/NullPointerException":
                switch(opcode) {
                    case ARRAYLENGTH:
                    case ATHROW:
                    case GETFIELD:
                    case INVOKEINTERFACE:
                    case INVOKESPECIAL:
                    case INVOKEVIRTUAL:
                    case MONITORENTER:
                    case MONITOREXIT:
                    case PUTFIELD:
                        return true;
                    default:
                        return isArrayStore(opcode) || isArrayLoad(opcode);
                }
            case "java/lang/IndexOutOfBoundsException":
            case "java/lang/ArrayIndexOutOfBoundsException":
                return isArrayStore(opcode) || isArrayLoad(opcode);
            case "java/lang/IllegalMonitorStateException":
                return isReturnOpcode(opcode) || opcode == ATHROW || opcode == MONITOREXIT;
            case "java/lang/invoke/WrongMethodTypeException":
                return opcode == INVOKEVIRTUAL;
            case "java/lang/ArrayStoreException":
                return opcode == AASTORE;
            case "java/lang/ArithmeticException":
                return opcode == IDIV || opcode == IREM || opcode == LDIV || opcode == LREM;
            case "java/lang/NegativeArraySizeException":
                return opcode == ANEWARRAY || opcode == MULTIANEWARRAY || opcode == NEWARRAY;
            case "java/lang/ClassCastException":
                return opcode == CHECKCAST;
            case "java/lang/LinkageError":
            case "java/lang/UnsatisfiedLinkError":
                return opcode == INVOKEINTERFACE || opcode == INVOKESPECIAL || opcode == INVOKESTATIC
                        || opcode == INVOKEVIRTUAL;
            case "java/lang/IncompatibleClassChangeError":
            case "java/lang/AbstractMethodError":
                return opcode == INVOKEINTERFACE || opcode == INVOKESPECIAL || opcode == INVOKEVIRTUAL;
            case "java/lang/IllegalAccessError":
                return opcode == INVOKEINTERFACE;
            default:
                return false;
        }
    }
}
