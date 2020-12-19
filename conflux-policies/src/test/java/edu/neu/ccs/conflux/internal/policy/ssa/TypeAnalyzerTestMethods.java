package edu.neu.ccs.conflux.internal.policy.ssa;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import jdk.nashorn.internal.codegen.types.Type;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public class TypeAnalyzerTestMethods {

    public static final String OWNER = Type.getInternalName(TypeAnalyzerTestMethods.class);

    public static MethodNode booleanReturnValue() {
        //public static void booleanReturnValue() {
        //    if(z()) {
        //        int x = -1;
        //    }
        //}
        MethodNode mv = new MethodNode(ACC_PUBLIC + ACC_STATIC, "booleanReturnValue", "()V", null, null);
        mv.visitCode();
        mv.visitMethodInsn(INVOKESTATIC, OWNER, "z", "()Z", false);
        Label l0 = new Label();
        mv.visitJumpInsn(IFEQ, l0);
        mv.visitInsn(ICONST_M1);
        mv.visitVarInsn(ISTORE, 0);
        mv.visitLabel(l0);
        mv.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
        return mv;
    }

    public static MethodNode intReturnValue() {
        //public static void intReturnValue() {
        //    if(i() == 0) {
        //        int x = -1;
        //    }
        //}
        MethodNode mv = new MethodNode(ACC_PUBLIC + ACC_STATIC, "intReturnValue", "()V", null, null);
        mv.visitCode();
        mv.visitMethodInsn(INVOKESTATIC, OWNER, "i", "()I", false);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNE, l0);
        mv.visitInsn(ICONST_M1);
        mv.visitVarInsn(ISTORE, 0);
        mv.visitLabel(l0);
        mv.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
        return mv;
    }

    public static MethodNode booleanParam() {
        //public static void booleanParam(boolean z) {
        //    if(z) {
        //        int x = -1;
        //    }
        //}
        MethodNode mv = new MethodNode(ACC_PUBLIC + ACC_STATIC, "booleanParam", "(Z)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ILOAD, 0);
        Label l0 = new Label();
        mv.visitJumpInsn(IFEQ, l0);
        mv.visitInsn(ICONST_M1);
        mv.visitVarInsn(ISTORE, 1);
        mv.visitLabel(l0);
        mv.visitFrame(F_NEW, 1, new Object[]{INTEGER}, 0, new Object[0]);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 2);
        mv.visitEnd();
        return mv;
    }

    public static MethodNode intParam() {
        //public static void intParam(int i) {
        //    if(i == 0) {
        //        int x = -1;
        //    }
        //}
        MethodNode mv = new MethodNode(ACC_PUBLIC + ACC_STATIC, "intParam", "(I)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ILOAD, 0);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNE, l0);
        mv.visitInsn(ICONST_M1);
        mv.visitVarInsn(ISTORE, 1);
        mv.visitLabel(l0);
        mv.visitFrame(F_NEW, 1, new Object[]{INTEGER}, 0, new Object[0]);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 2);
        mv.visitEnd();
        return mv;
    }

    public static MethodNode booleanField() {
        //public static void booleanField() {
        //    if(zField) {
        //        int x = -1;
        //    }
        //}
        //
        MethodNode mv = new MethodNode(ACC_PUBLIC + ACC_STATIC, "booleanField", "()V", null, null);
        mv.visitCode();
        mv.visitFieldInsn(GETSTATIC, OWNER, "zField", "Z");
        Label l0 = new Label();
        mv.visitJumpInsn(IFEQ, l0);
        mv.visitInsn(ICONST_M1);
        mv.visitVarInsn(ISTORE, 0);
        mv.visitLabel(l0);
        mv.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
        return mv;
    }

    public static MethodNode intField() {
        //public static void intField() {
        //    if(iField == 0) {
        //        int x = -1;
        //    }
        //}
        MethodNode mv = new MethodNode(ACC_PUBLIC + ACC_STATIC, "intField", "()V", null, null);
        mv.visitCode();
        mv.visitFieldInsn(GETSTATIC, OWNER, "iField", "I");
        Label l0 = new Label();
        mv.visitJumpInsn(IFNE, l0);
        mv.visitInsn(ICONST_M1);
        mv.visitVarInsn(ISTORE, 0);
        mv.visitLabel(l0);
        mv.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
        return mv;
    }

    public static MethodNode booleanArray() {
        //public static void booleanArray(boolean[] za) {
        //    if(za[0]) {
        //        int x = -1;
        //    }
        //}
        MethodNode mv = new MethodNode(ACC_PUBLIC + ACC_STATIC, "booleanArray", "([Z)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(BALOAD);
        Label l0 = new Label();
        mv.visitJumpInsn(IFEQ, l0);
        mv.visitInsn(ICONST_M1);
        mv.visitVarInsn(ISTORE, 1);
        mv.visitLabel(l0);
        mv.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
        return mv;
    }

    public static MethodNode intArray() {
        //public static void intArray(int[] ia) {
        //    if(ia[0] == 0) {
        //        int x = -1;
        //    }
        //}
        MethodNode mv = new MethodNode(ACC_PUBLIC + ACC_STATIC, "intArray", "([I)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IALOAD);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNE, l0);
        mv.visitInsn(ICONST_M1);
        mv.visitVarInsn(ISTORE, 1);
        mv.visitLabel(l0);
        mv.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
        return mv;
    }

    public static MethodNode booleanConstant() {
        //public static void booleanConstant() {
        //    boolean z = true;
        //    if(z) {
        //        int x = -1;
        //    }
        //}
        MethodNode mv = new MethodNode(ACC_PUBLIC + ACC_STATIC, "booleanConstant", "()V", null, null);
        mv.visitCode();
        mv.visitInsn(ICONST_1);
        mv.visitVarInsn(ISTORE, 0);
        mv.visitVarInsn(ILOAD, 0);
        Label l0 = new Label();
        mv.visitJumpInsn(IFEQ, l0);
        mv.visitInsn(ICONST_M1);
        mv.visitVarInsn(ISTORE, 1);
        mv.visitLabel(l0);
        mv.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 2);
        mv.visitEnd();
        return mv;
    }

    public static MethodNode intConstant() {
        //public static void intConstant() {
        //    int i = 7;
        //    if(i == 0) {
        //        int x = -1;
        //    }
        //}
        MethodNode mv = new MethodNode(ACC_PUBLIC + ACC_STATIC, "intConstant", "()V", null, null);
        mv.visitCode();
        mv.visitIntInsn(BIPUSH, 7);
        mv.visitVarInsn(ISTORE, 0);
        mv.visitVarInsn(ILOAD, 0);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNE, l0);
        mv.visitInsn(ICONST_M1);
        mv.visitVarInsn(ISTORE, 1);
        mv.visitLabel(l0);
        mv.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 2);
        mv.visitEnd();
        return mv;
    }

    public static MethodNode booleanUnaryOperation() {
        //public static void booleanUnaryOperation(boolean z) {
        //    if(!z) {
        //        int x = -1;
        //    }
        //}
        MethodNode mv = new MethodNode(ACC_PUBLIC + ACC_STATIC, "booleanUnaryOperation", "(Z)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ILOAD, 0);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNE, l0);
        mv.visitInsn(ICONST_M1);
        mv.visitVarInsn(ISTORE, 1);
        mv.visitLabel(l0);
        mv.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 2);
        mv.visitEnd();
        return mv;
    }

    public static MethodNode intUnaryOperation() {
        //public static void intUnaryOperation(int i) {
        //    if(i == 0) {
        //        int x = -1;
        //    }
        //}
        MethodNode mv = new MethodNode(ACC_PUBLIC + ACC_STATIC, "intUnaryOperation", "(I)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ILOAD, 0);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNE, l0);
        mv.visitInsn(ICONST_M1);
        mv.visitVarInsn(ISTORE, 1);
        mv.visitLabel(l0);
        mv.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 2);
        mv.visitEnd();
        return mv;
    }

    public static MethodNode booleanBinaryOperation() {
        //public static void booleanBinaryOperation(boolean z1, boolean z2) {
        //    if(z1 | z2) {
        //        int x = -1;
        //    }
        //}
        MethodNode mv = new MethodNode(ACC_PUBLIC + ACC_STATIC, "booleanBinaryOperation", "(ZZ)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitInsn(IOR);
        Label l0 = new Label();
        mv.visitJumpInsn(IFEQ, l0);
        mv.visitInsn(ICONST_M1);
        mv.visitVarInsn(ISTORE, 2);
        mv.visitLabel(l0);
        mv.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 3);
        mv.visitEnd();
        return mv;
    }

    public static MethodNode intBinaryOperation() {
        //public static void intBinaryOperation(int i) {
        //    if((i & 5) == 0) {
        //        int x = -1;
        //    }
        //}
        MethodNode mv = new MethodNode(ACC_PUBLIC + ACC_STATIC, "intBinaryOperation", "(I)V",
                null, null);
        mv.visitCode();
        mv.visitVarInsn(ILOAD, 0);
        mv.visitInsn(ICONST_5);
        mv.visitInsn(IAND);
        Label l0 = new Label();
        mv.visitJumpInsn(IFNE, l0);
        mv.visitInsn(ICONST_M1);
        mv.visitVarInsn(ISTORE, 1);
        mv.visitLabel(l0);
        mv.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
        return mv;
    }

    public static MethodNode singleBitCheck() {
        //public static boolean singleBitCheck(long holder, int index) {
        //    return (holder & (1L << index)) != 0;
        //}
        MethodNode mv = new MethodNode(ACC_PUBLIC + ACC_STATIC, "singleBitCheck", "(JI)Z",
                null, null);
        mv.visitCode();
        mv.visitVarInsn(LLOAD, 0);
        mv.visitInsn(LCONST_1);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitInsn(LSHL);
        mv.visitInsn(LAND);
        mv.visitInsn(LCONST_0);
        mv.visitInsn(LCMP);
        Label l0 = new Label();
        mv.visitJumpInsn(IFEQ, l0);
        mv.visitInsn(ICONST_1);
        Label l1 = new Label();
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(l0);
        mv.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(l1);
        mv.visitFrame(F_NEW, 0, new Object[0], 1, new Object[] {INTEGER});
        mv.visitInsn(IRETURN);
        mv.visitMaxs(5, 3);
        mv.visitEnd();
        return mv;
    }

    public static MethodNode singleBitCheckArray() {
        //public static boolean singleBitCheckArray(long[] holders, int wordIndex, int bitIndex) {
        //    return (holders[wordIndex] & (1L << bitIndex)) != 0;
        //}
        MethodNode mv = new MethodNode(ACC_PUBLIC + ACC_STATIC, "singleBitCheckArray", "([JII)Z",
                null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitInsn(LALOAD);
        mv.visitInsn(LCONST_1);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitInsn(LSHL);
        mv.visitInsn(LAND);
        mv.visitInsn(LCONST_0);
        mv.visitInsn(LCMP);
        Label l0 = new Label();
        mv.visitJumpInsn(IFEQ, l0);
        mv.visitInsn(ICONST_1);
        Label l1 = new Label();
        mv.visitJumpInsn(GOTO, l1);
        mv.visitLabel(l0);
        mv.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(l1);
        mv.visitFrame(F_NEW, 0, new Object[0], 1, new Object[] {INTEGER});
        mv.visitInsn(IRETURN);
        mv.visitMaxs(5, 3);
        mv.visitEnd();
        return mv;
    }
}
