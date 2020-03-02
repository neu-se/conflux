package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import jdk.nashorn.internal.codegen.types.Type;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public class InsnConverterTestMethods {

    public static final String OWNER = Type.getInternalName(InsnConverterTestMethods.class);

    public static MethodNode pushPopConstants() {
        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_STATIC, "pushPopConstants", "()V",
                null, null);
        methodNode.visitCode();
        methodNode.visitInsn(ACONST_NULL); // s0
        methodNode.visitInsn(ICONST_M1); // s1
        methodNode.visitInsn(ICONST_0); // s2
        methodNode.visitInsn(ICONST_1); //s3
        methodNode.visitInsn(ICONST_2); //s4
        methodNode.visitInsn(ICONST_3); // s5
        methodNode.visitInsn(ICONST_4); // s6
        methodNode.visitInsn(ICONST_5); // s7
        methodNode.visitInsn(FCONST_0); // s8
        methodNode.visitInsn(FCONST_1); // s9
        methodNode.visitInsn(FCONST_2); // s10
        methodNode.visitIntInsn(BIPUSH, 17); // s11
        methodNode.visitIntInsn(SIPUSH, 34); // s12
        methodNode.visitLdcInsn("Hello"); // s13
        methodNode.visitInsn(DCONST_0); // s14
        methodNode.visitInsn(DCONST_1); // s15
        methodNode.visitInsn(LCONST_0); // s16
        methodNode.visitInsn(LCONST_1); // s17
        for(int i = 0; i < 4; i++) {
            methodNode.visitInsn(POP2);
        }
        for(int i = 0; i < 14; i++) {
            methodNode.visitInsn(POP);
        }
        methodNode.visitInsn(RETURN);
        methodNode.visitMaxs(18, 1);
        methodNode.visitEnd();
        return methodNode;
    }

    public static MethodNode loadLocals() {
        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_STATIC, "loadLocals", "(IJFDLjava/lang/Object;)V",
                null, null);
        methodNode.visitCode();
        methodNode.visitVarInsn(ILOAD, 0);
        methodNode.visitVarInsn(LLOAD, 1);
        methodNode.visitVarInsn(FLOAD, 3);
        methodNode.visitVarInsn(DLOAD, 4);
        methodNode.visitVarInsn(ALOAD, 6);
        methodNode.visitMethodInsn(INVOKESTATIC, OWNER, "example", "(IJFDLjava/lang/Object;)V", false);
        methodNode.visitInsn(RETURN);
        methodNode.visitMaxs(7, 7);
        methodNode.visitEnd();
        return methodNode;
    }

    public static MethodNode storeLocals() {
        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_STATIC, "storeLocals", "(IJFDLjava/lang/Object;)V",
                null, null);
        methodNode.visitCode();
        methodNode.visitInsn(ICONST_0);
        methodNode.visitVarInsn(ISTORE, 0);
        methodNode.visitInsn(LCONST_0);
        methodNode.visitVarInsn(LSTORE, 1);
        methodNode.visitInsn(FCONST_0);
        methodNode.visitVarInsn(FSTORE, 3);
        methodNode.visitInsn(DCONST_0);
        methodNode.visitVarInsn(DSTORE, 4);
        methodNode.visitInsn(ACONST_NULL);
        methodNode.visitVarInsn(ASTORE, 6);
        methodNode.visitInsn(RETURN);
        methodNode.visitMaxs(2, 7);
        methodNode.visitEnd();
        return methodNode;
    }

    public static MethodNode loadArrayElements() {
        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_STATIC, "loadArrayElements",
                "([I[J[F[D[B[C[S[Ljava/lang/Object;)V", null, null);
        methodNode.visitCode();
        methodNode.visitVarInsn(ALOAD, 0);
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(IALOAD);
        methodNode.visitVarInsn(ALOAD, 1);
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(LALOAD);
        methodNode.visitVarInsn(ALOAD, 2);
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(FALOAD);
        methodNode.visitVarInsn(ALOAD, 3);
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(DALOAD);
        methodNode.visitVarInsn(ALOAD, 4);
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(BALOAD);
        methodNode.visitVarInsn(ALOAD, 5);
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(CALOAD);
        methodNode.visitVarInsn(ALOAD, 6);
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(SALOAD);
        methodNode.visitVarInsn(ALOAD, 7);
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(AALOAD);
        methodNode.visitMethodInsn(INVOKESTATIC, OWNER, "example", "(IJFDBCSLjava/lang/Object;)V", false);
        methodNode.visitInsn(RETURN);
        methodNode.visitMaxs(11, 8);
        methodNode.visitEnd();
        return methodNode;
    }

    public static MethodNode storeArrayElements() {
        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_STATIC, "storeArrayElements",
                "([I[J[F[D[B[C[S[Ljava/lang/Object;)V", null, null);
        methodNode.visitCode();
        methodNode.visitVarInsn(ALOAD, 0);
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(IASTORE);
        methodNode.visitVarInsn(ALOAD, 1);
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(LCONST_0);
        methodNode.visitInsn(LASTORE);
        methodNode.visitVarInsn(ALOAD, 2);
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(FCONST_0);
        methodNode.visitInsn(FASTORE);
        methodNode.visitVarInsn(ALOAD, 3);
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(DCONST_0);
        methodNode.visitInsn(DASTORE);
        methodNode.visitVarInsn(ALOAD, 4);
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(BASTORE);
        methodNode.visitVarInsn(ALOAD, 5);
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(CASTORE);
        methodNode.visitVarInsn(ALOAD, 6);
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(SASTORE);
        methodNode.visitVarInsn(ALOAD, 7);
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(ACONST_NULL);
        methodNode.visitInsn(AASTORE);
        methodNode.visitInsn(RETURN);
        methodNode.visitMaxs(4, 8);
        methodNode.visitEnd();
        return methodNode;
    }

    public static MethodNode dup() {
        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_STATIC, "dup", "()V",
                null, null);
        methodNode.visitCode();
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(DUP);
        methodNode.visitInsn(POP2);
        methodNode.visitInsn(RETURN);
        methodNode.visitMaxs(2, 0);
        methodNode.visitEnd();
        return methodNode;
    }

    public static MethodNode dup_x1() {
        // v2 v1 -> v1 v2 v1
        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_STATIC, "dup_x1", "()V",
                null, null);
        methodNode.visitCode();
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(ICONST_1);
        methodNode.visitInsn(DUP_X1);
        methodNode.visitInsn(POP2);
        methodNode.visitInsn(POP);
        methodNode.visitInsn(RETURN);
        methodNode.visitMaxs(3, 0);
        methodNode.visitEnd();
        return methodNode;
    }

    public static MethodNode dup_x2() {
        // {value3, value2}, value1 -> value1, {value3, value2}, value1
        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_STATIC, "dup_x2", "()V",
                null, null);
        methodNode.visitCode();
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(ICONST_1);
        methodNode.visitInsn(ICONST_2);
        methodNode.visitInsn(DUP_X2);
        methodNode.visitInsn(POP2);
        methodNode.visitInsn(POP2);
        //
        methodNode.visitInsn(LCONST_0);
        methodNode.visitInsn(ICONST_1);
        methodNode.visitInsn(DUP_X2);
        methodNode.visitInsn(POP);
        methodNode.visitInsn(POP2);
        methodNode.visitInsn(POP);
        methodNode.visitInsn(RETURN);
        methodNode.visitMaxs(4, 0);
        methodNode.visitEnd();
        return methodNode;
    }

    public static MethodNode dup2() {
        // {value2, value1} -> {value2, value1}, {value2, value1}
        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_STATIC, "dup2", "()V",
                null, null);
        methodNode.visitCode();
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(ICONST_1);
        methodNode.visitInsn(DUP2);
        methodNode.visitInsn(POP2);
        methodNode.visitInsn(POP2);
        //
        methodNode.visitInsn(LCONST_0);
        methodNode.visitInsn(DUP2);
        methodNode.visitInsn(POP2);
        methodNode.visitInsn(POP2);
        methodNode.visitInsn(RETURN);
        methodNode.visitMaxs(4, 0);
        methodNode.visitEnd();
        return methodNode;
    }

    public static MethodNode dup2_x1() {
        // value3, {value2, value1} -> {value2, value1}, value3, {value2, value1}
        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_STATIC, "dup2_x1", "()V",
                null, null);
        methodNode.visitCode();
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(ICONST_1);
        methodNode.visitInsn(ICONST_2);
        methodNode.visitInsn(DUP2_X1);
        methodNode.visitInsn(POP2);
        methodNode.visitInsn(POP2);
        methodNode.visitInsn(POP);
        //
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(LCONST_1);
        methodNode.visitInsn(DUP2_X1);
        methodNode.visitInsn(POP2);
        methodNode.visitInsn(POP);
        methodNode.visitInsn(POP2);
        methodNode.visitInsn(RETURN);
        methodNode.visitMaxs(5, 0);
        methodNode.visitEnd();
        return methodNode;
    }

    public static MethodNode dup2_x2() {
        // {value4, value3}, {value2, value1} -> {value2, value1}, {value4, value3}, {value2, value1}
        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_STATIC, "dup2_x2", "()V",
                null, null);
        methodNode.visitCode();
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(ICONST_1);
        methodNode.visitInsn(ICONST_2);
        methodNode.visitInsn(ICONST_3);
        methodNode.visitInsn(DUP2_X2);
        methodNode.visitInsn(POP2);
        methodNode.visitInsn(POP2);
        methodNode.visitInsn(POP2);
        //
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(ICONST_1);
        methodNode.visitInsn(LCONST_1);
        methodNode.visitInsn(DUP2_X2);
        methodNode.visitInsn(POP2);
        methodNode.visitInsn(POP2);
        methodNode.visitInsn(POP2);
        //
        methodNode.visitInsn(LCONST_0);
        methodNode.visitInsn(ICONST_1);
        methodNode.visitInsn(ICONST_2);
        methodNode.visitInsn(DUP2_X2);
        methodNode.visitInsn(POP2);
        methodNode.visitInsn(POP2);
        methodNode.visitInsn(POP2);
        //
        methodNode.visitInsn(LCONST_0);
        methodNode.visitInsn(LCONST_1);
        methodNode.visitInsn(DUP2_X2);
        methodNode.visitInsn(POP2);
        methodNode.visitInsn(POP2);
        methodNode.visitInsn(POP2);
        methodNode.visitInsn(RETURN);
        methodNode.visitMaxs(6, 0);
        methodNode.visitEnd();
        return methodNode;
    }

    public static MethodNode swap() {
        // value2, value1 -> value1, value2
        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_STATIC, "swap", "()V",
                null, null);
        methodNode.visitCode();
        methodNode.visitInsn(ICONST_0);
        methodNode.visitInsn(ICONST_1);
        methodNode.visitInsn(SWAP);
        methodNode.visitInsn(POP2);
        methodNode.visitInsn(RETURN);
        methodNode.visitMaxs(2, 0);
        methodNode.visitEnd();
        return methodNode;
    }

    public static MethodNode add() {
        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_STATIC, "add", "(IJFD)V",
                null, null);
        methodNode.visitCode();
        methodNode.visitVarInsn(ILOAD, 0);
        methodNode.visitVarInsn(ILOAD, 0);
        methodNode.visitInsn(IADD);
        methodNode.visitVarInsn(LLOAD, 1);
        methodNode.visitVarInsn(LLOAD, 1);
        methodNode.visitInsn(LADD);
        methodNode.visitVarInsn(FLOAD, 3);
        methodNode.visitVarInsn(FLOAD, 3);
        methodNode.visitInsn(FADD);
        methodNode.visitVarInsn(DLOAD, 4);
        methodNode.visitVarInsn(DLOAD, 4);
        methodNode.visitInsn(DADD);
        methodNode.visitMethodInsn(INVOKESTATIC, OWNER, "example", "(IJFD)V", false);
        methodNode.visitInsn(RETURN);
        methodNode.visitMaxs(8, 6);
        methodNode.visitEnd();
        return methodNode;
    }

    public static MethodNode subtract() {
        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_STATIC, "subtract", "(IJFD)V",
                null, null);
        methodNode.visitCode();
        methodNode.visitVarInsn(ILOAD, 0);
        methodNode.visitVarInsn(ILOAD, 0);
        methodNode.visitInsn(ISUB);
        methodNode.visitVarInsn(LLOAD, 1);
        methodNode.visitVarInsn(LLOAD, 1);
        methodNode.visitInsn(LSUB);
        methodNode.visitVarInsn(FLOAD, 3);
        methodNode.visitVarInsn(FLOAD, 3);
        methodNode.visitInsn(FSUB);
        methodNode.visitVarInsn(DLOAD, 4);
        methodNode.visitVarInsn(DLOAD, 4);
        methodNode.visitInsn(DSUB);
        methodNode.visitMethodInsn(INVOKESTATIC, OWNER, "example", "(IJFD)V", false);
        methodNode.visitInsn(RETURN);
        methodNode.visitMaxs(8, 6);
        methodNode.visitEnd();
        return methodNode;
    }

    public static MethodNode multiply() {
        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_STATIC, "multiply", "(IJFD)V",
                null, null);
        methodNode.visitCode();
        methodNode.visitVarInsn(ILOAD, 0);
        methodNode.visitVarInsn(ILOAD, 0);
        methodNode.visitInsn(IMUL);
        methodNode.visitVarInsn(LLOAD, 1);
        methodNode.visitVarInsn(LLOAD, 1);
        methodNode.visitInsn(LMUL);
        methodNode.visitVarInsn(FLOAD, 3);
        methodNode.visitVarInsn(FLOAD, 3);
        methodNode.visitInsn(FMUL);
        methodNode.visitVarInsn(DLOAD, 4);
        methodNode.visitVarInsn(DLOAD, 4);
        methodNode.visitInsn(DMUL);
        methodNode.visitMethodInsn(INVOKESTATIC, OWNER, "example", "(IJFD)V", false);
        methodNode.visitInsn(RETURN);
        methodNode.visitMaxs(8, 6);
        methodNode.visitEnd();
        return methodNode;
    }

    public static MethodNode divide() {
        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_STATIC, "divide", "(IJFD)V",
                null, null);
        methodNode.visitCode();
        methodNode.visitVarInsn(ILOAD, 0);
        methodNode.visitVarInsn(ILOAD, 0);
        methodNode.visitInsn(IDIV);
        methodNode.visitVarInsn(LLOAD, 1);
        methodNode.visitVarInsn(LLOAD, 1);
        methodNode.visitInsn(LDIV);
        methodNode.visitVarInsn(FLOAD, 3);
        methodNode.visitVarInsn(FLOAD, 3);
        methodNode.visitInsn(FDIV);
        methodNode.visitVarInsn(DLOAD, 4);
        methodNode.visitVarInsn(DLOAD, 4);
        methodNode.visitInsn(DDIV);
        methodNode.visitMethodInsn(INVOKESTATIC, OWNER, "example", "(IJFD)V", false);
        methodNode.visitInsn(RETURN);
        methodNode.visitMaxs(8, 6);
        methodNode.visitEnd();
        return methodNode;
    }

    public static MethodNode remainder() {
        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_STATIC, "remainder", "(IJFD)V",
                null, null);
        methodNode.visitCode();
        methodNode.visitVarInsn(ILOAD, 0);
        methodNode.visitVarInsn(ILOAD, 0);
        methodNode.visitInsn(IREM);
        methodNode.visitVarInsn(LLOAD, 1);
        methodNode.visitVarInsn(LLOAD, 1);
        methodNode.visitInsn(LREM);
        methodNode.visitVarInsn(FLOAD, 3);
        methodNode.visitVarInsn(FLOAD, 3);
        methodNode.visitInsn(FREM);
        methodNode.visitVarInsn(DLOAD, 4);
        methodNode.visitVarInsn(DLOAD, 4);
        methodNode.visitInsn(DREM);
        methodNode.visitMethodInsn(INVOKESTATIC, OWNER, "example", "(IJFD)V", false);
        methodNode.visitInsn(RETURN);
        methodNode.visitMaxs(8, 6);
        methodNode.visitEnd();
        return methodNode;
    }

    public static MethodNode shift() {
        //    case ISHL:
        //    case IUSHR:
        //    case ISHR:
        //    case LSHL:
        //    case LSHR:
        //    case LUSHR:
        return null;
    }

    public static MethodNode and() {
        //    case IAND:
        //    case LAND:
        return null;
    }

    public static MethodNode or() {
        //    case IOR:
        //    case LOR:
        return null;
    }

    public static MethodNode xor() {
        //    case IXOR:
        //    case LXOR:
        return null;
    }

    public static MethodNode negate() {
        //    case INEG:
        //    case LNEG:
        //    case FNEG:
        //    case DNEG:
        return null;
    }

    public static MethodNode iinc() {
        //    case IINC:
        return null;
    }

    public static MethodNode cast() {
        //    case I2L:
        //    case I2F:
        //    case I2D:
        //    case L2I:
        //    case L2F:
        //    case L2D:
        //    case F2I:
        //    case F2L:
        //    case F2D:
        //    case D2I:
        //    case D2L:
        //    case D2F:
        //    case I2B:
        //    case I2C:
        //    case I2S:
        //    case CHECKCAST:
        return null;
    }

    public static MethodNode compare() {
        //    case LCMP:
        //    case FCMPL:
        //    case FCMPG:
        //    case DCMPL:
        //    case DCMPG:
        return null;
    }

    public static MethodNode unaryIf() {
        //    case IFEQ:
        //    case IFNE:
        //    case IFLT:
        //    case IFGE:
        //    case IFGT:
        //    case IFLE:
        //    case IFNULL:
        //    case IFNONNULL:
        return null;
    }

    public static MethodNode binaryIf() {
        //    case IF_ICMPEQ:
        //    case IF_ICMPNE:
        //    case IF_ICMPLT:
        //    case IF_ICMPGE:
        //    case IF_ICMPGT:
        //    case IF_ICMPLE:
        //    case IF_ACMPEQ:
        //    case IF_ACMPNE:
        return null;
    }

    public static MethodNode gotoStatement() {
        //    case GOTO:
        return null;
    }

    public static MethodNode tableSwitch() {
        //    case TABLESWITCH:
        return null;
    }

    public static MethodNode lookupSwitch() {
        //    case LOOKUPSWITCH:
        return null;
    }

    public static MethodNode iReturn() {
        //    case IRETURN:
        return null;
    }

    public static MethodNode lReturn() {
        //    case LRETURN:
        return null;
    }

    public static MethodNode fReturn() {
        //    case FRETURN:
        return null;
    }

    public static MethodNode dReturn() {
        //    case FRETURN:
        return null;
    }

    public static MethodNode aReturn() {
        //    case ARETURN:
        return null;
    }

    public static MethodNode getFields() {
        //    case GETSTATIC:
        //    case GETFIELD:
        return null;
    }

    public static MethodNode putFields() {
        //    case PUTSTATIC:
        //    case PUTFIELD:
        return null;
    }

    public static MethodNode methodCall() {
        //    case INVOKEVIRTUAL:
        //    case INVOKEINTERFACE:
        //    case INVOKEDYNAMIC:
        return null;
    }

    public static MethodNode monitor() {
        //    case MONITORENTER:
        //    case MONITOREXIT:
        return null;
    }

    public static MethodNode constructorCall() {
        //    case NEW:
        //    case INVOKESPECIAL:
        return null;
    }

    public static MethodNode newArray() {
        //    case NEWARRAY:
        //    case ANEWARRAY:
        return null;
    }

    public static MethodNode multiNewArray() {
        //    case MULTIANEWARRAY:
        return null;
    }

    public static MethodNode arrayLength() {
        //    case ARRAYLENGTH:
        return null;
    }

    public static MethodNode athrow() {
        //    case ATHROW:
        return null;
    }

    public static MethodNode instanceOf() {
        //    case INSTANCEOF:
        return null;
    }
}
