package edu.gmu.swe.phosphor.ignored.control.binding;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

class LoopLevelTracerConstancyTestMethods {

    public static final String OWNER = Type.getInternalName(LoopLevelTracerConstancyTestMethods.class);

    public static MethodNode arrayFieldSelfComputation() {
        //for(/* constant */ int i = 0; i < 5; i++) {
        //    arg0.a1[0] = arg0.a1[0] * 2; // this.a1 may have been redefined - variant +1
        //}
        MethodNode mn = new MethodNode(ACC_PUBLIC, "arrayFieldSelfComputation", "()V",
                null, null);
        mn.visitCode();
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 1);
        Label l0 = new Label();
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 1, new Object[]{INTEGER}, 0, new Object[0]);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitInsn(ICONST_5);
        Label l1 = new Label();
        mn.visitJumpInsn(IF_ICMPGE, l1);
        mn.visitVarInsn(ALOAD, 0);
        mn.visitFieldInsn(GETFIELD, OWNER, "a1", "[I");
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ALOAD, 0);
        mn.visitFieldInsn(GETFIELD, OWNER, "a1", "[I");
        mn.visitInsn(ICONST_0);
        mn.visitInsn(IALOAD);
        mn.visitInsn(ICONST_2);
        mn.visitInsn(IMUL);
        mn.visitInsn(IASTORE);
        mn.visitIincInsn(1, 1);
        mn.visitJumpInsn(GOTO, l0);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(4, 2);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode fieldSelfComputation() {
        //for(/* constant */ int i = 0; i < 5; i++) {
        //    arg0.a = arg0.a * 2; // dependent on arg0 (this)
        //}
        MethodNode mn = new MethodNode(ACC_PUBLIC, "fieldSelfComputation", "()V",
                null, null);
        mn.visitCode();
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 1);
        Label l0 = new Label();
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 1, new Object[]{INTEGER}, 0, new Object[0]);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitInsn(ICONST_5);
        Label l1 = new Label();
        mn.visitJumpInsn(IF_ICMPGE, l1);
        mn.visitVarInsn(ALOAD, 0);
        mn.visitVarInsn(ALOAD, 0);
        mn.visitFieldInsn(GETFIELD, OWNER, "a", "I");
        mn.visitInsn(ICONST_2);
        mn.visitInsn(IMUL);
        mn.visitFieldInsn(PUTFIELD, OWNER, "a", "I");
        mn.visitIincInsn(1, 1);
        mn.visitJumpInsn(GOTO, l0);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(3, 2);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode fieldAssignedVariantValue() {
        //for(/* constant */ int i = 0; i < 5; i++) {
        //    arg0.a = i; // variant +1
        //}
        MethodNode mn = new MethodNode(ACC_PUBLIC, "fieldAssignedVariantValue", "()V",
                null, null);
        mn.visitCode();
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 1);
        Label l0 = new Label();
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 2, new Object[]{OWNER, INTEGER}, 0, new Object[0]);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitInsn(ICONST_5);
        Label l1 = new Label();
        mn.visitJumpInsn(IF_ICMPGE, l1);
        mn.visitVarInsn(ALOAD, 0);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitFieldInsn(PUTFIELD, OWNER, "a", "I");
        mn.visitIincInsn(1, 1);
        mn.visitJumpInsn(GOTO, l0);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(2, 2);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode arrayFieldAssignedVariantValue() {
        //for(/* constant */ int i = 0; i < 5; i++) {
        //    arg0.a1[0] = i; // variant +1
        //}
        MethodNode mn = new MethodNode(ACC_PUBLIC, "arrayFieldAssignedVariantValue", "()V",
                null, null);
        mn.visitCode();
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 1);
        Label l0 = new Label();
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 2, new Object[]{OWNER, INTEGER}, 0, new Object[0]);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitInsn(ICONST_5);
        Label l1 = new Label();
        mn.visitJumpInsn(IF_ICMPGE, l1);
        mn.visitVarInsn(ALOAD, 0);
        mn.visitFieldInsn(GETFIELD, OWNER, "a1", "[I");
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitInsn(IASTORE);
        mn.visitIincInsn(1, 1);
        mn.visitJumpInsn(GOTO, l0);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(3, 2);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode variantArray() {
        //for(/* constant */ int i = 0; i < 5; i++) {
        //    arg0[i][0] = 5; // variant +1
        //}
        MethodNode mn = new MethodNode(ACC_PUBLIC, "variantArray", "([[I)V",
                null, null);
        mn.visitCode();
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 2);
        Label l0 = new Label();
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 3, new Object[]{OWNER, "[[I", INTEGER}, 0, new Object[0]);
        mn.visitVarInsn(ILOAD, 2);
        mn.visitInsn(ICONST_5);
        Label l1 = new Label();
        mn.visitJumpInsn(IF_ICMPGE, l1);
        mn.visitVarInsn(ALOAD, 1);
        mn.visitVarInsn(ILOAD, 2);
        mn.visitInsn(AALOAD);
        mn.visitInsn(ICONST_0);
        mn.visitInsn(ICONST_5);
        mn.visitInsn(IASTORE);
        mn.visitIincInsn(2, 1);
        mn.visitJumpInsn(GOTO, l0);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(3, 3);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode variantArray2() {
        //java.util.Iterator<int[]> itr = arg0.iterator(); // variant +0
        //for(/* constant */ int i = 0; i < 5; i++) {
        //    int[] b = itr.next(); // variant +1
        //    b[0] = 5; // variant +1
        //}
        MethodNode mn = new MethodNode(ACC_PUBLIC, "variantArray2", "(Ljava/util/LinkedList;)V",
                "(Ljava/util/LinkedList<[I>;)V", null);
        mn.visitCode();
        mn.visitVarInsn(ALOAD, 1);
        mn.visitMethodInsn(INVOKEVIRTUAL, "java/util/LinkedList", "iterator",
                "()Ljava/util/Iterator;", false);
        mn.visitVarInsn(ASTORE, 2);
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 3);
        Label l0 = new Label();
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 4, new Object[]{OWNER, "Ljava/util/LinkedList;", "java/util/Iterator", INTEGER}, 0, new Object[0]);
        mn.visitVarInsn(ILOAD, 3);
        mn.visitInsn(ICONST_5);
        Label l1 = new Label();
        mn.visitJumpInsn(IF_ICMPGE, l1);
        mn.visitVarInsn(ALOAD, 2);
        mn.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
        mn.visitTypeInsn(CHECKCAST, "[I");
        mn.visitVarInsn(ASTORE, 4);
        mn.visitVarInsn(ALOAD, 4);
        mn.visitInsn(ICONST_0);
        mn.visitInsn(ICONST_5);
        mn.visitInsn(IASTORE);
        mn.visitIincInsn(3, 1);
        mn.visitJumpInsn(GOTO, l0);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(3, 5);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode allLocalAssignmentsConstant() {
        //int a = 1;
        //int b = a + 5;
        //long c = b * a;
        //c = c * c;
        //int d;
        //if(arg0) {
        //    d = 9;
        //} else {
        //    d = 9;
        //}
        //int e = d;
        //int f = 6 * 7 + 88;
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "allLocalAssignmentsConstant", "(Z)V",
                null, null);
        mn.visitCode();
        mn.visitInsn(ICONST_1);
        mn.visitVarInsn(ISTORE, 1);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitInsn(ICONST_5);
        mn.visitInsn(IADD);
        mn.visitVarInsn(ISTORE, 2);
        mn.visitVarInsn(ILOAD, 2);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitInsn(IMUL);
        mn.visitInsn(I2L);
        mn.visitVarInsn(LSTORE, 3);
        mn.visitVarInsn(LLOAD, 3);
        mn.visitVarInsn(LLOAD, 3);
        mn.visitInsn(LMUL);
        mn.visitVarInsn(LSTORE, 3);
        mn.visitVarInsn(ILOAD, 0);
        Label l0 = new Label();
        mn.visitJumpInsn(IFEQ, l0);
        mn.visitIntInsn(BIPUSH, 9);
        mn.visitVarInsn(ISTORE, 5);
        Label l1 = new Label();
        mn.visitJumpInsn(GOTO, l1);
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 6, new Object[]{INTEGER, INTEGER, INTEGER, LONG, TOP, INTEGER}, 0, new Object[0]);
        mn.visitIntInsn(BIPUSH, 9);
        mn.visitVarInsn(ISTORE, 5);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 6, new Object[]{INTEGER, INTEGER, INTEGER, LONG, TOP, INTEGER}, 0, new Object[0]);
        mn.visitVarInsn(ILOAD, 5);
        mn.visitVarInsn(ISTORE, 6);
        mn.visitIntInsn(SIPUSH, 130);
        mn.visitVarInsn(ISTORE, 7);
        mn.visitInsn(RETURN);
        mn.visitMaxs(4, 8);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode allLocalAssignmentsConstant2() {
        //int b = 0;
        //int a = 77;
        //if(arg0) {
        //    b += 1;
        //} else {
        //    b = 1;
        //}
        //if(arg0) {
        //    a = a * b;
        //}
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "allLocalAssignmentsConstant2", "(Z)V",
                null, null);
        mn.visitCode();
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 1);
        mn.visitIntInsn(BIPUSH, 77);
        mn.visitVarInsn(ISTORE, 2);
        mn.visitVarInsn(ILOAD, 0);
        Label l0 = new Label();
        mn.visitJumpInsn(IFEQ, l0);
        mn.visitIincInsn(1, 1);
        Label l1 = new Label();
        mn.visitJumpInsn(GOTO, l1);
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 3, new Object[]{INTEGER, INTEGER, INTEGER}, 0, new Object[0]);
        mn.visitInsn(ICONST_1);
        mn.visitVarInsn(ISTORE, 1);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 3, new Object[]{INTEGER, INTEGER, INTEGER}, 0, new Object[0]);
        mn.visitVarInsn(ILOAD, 0);
        Label l2 = new Label();
        mn.visitJumpInsn(IFEQ, l2);
        mn.visitVarInsn(ILOAD, 2);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitInsn(IMUL);
        mn.visitVarInsn(ISTORE, 2);
        mn.visitLabel(l2);
        mn.visitFrame(F_NEW, 3, new Object[]{INTEGER, INTEGER, INTEGER}, 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(2, 3);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode allLocalAssignmentsConstant3() {
        //int c = 7; // constant
        //int v = 2; // constant
        //int w = 300; // constant
        //int x = -4; // constant
        //int y = 44; // constant
        //int z = 5; // constant
        //for(int i = 0; i < 55; i++) {
        //    for(int j = 0; j < 10; j++) {
        //        if(arg0) {
        //            v = (v * v + v + 5 * 77 - 99) / (2 * v); // constant: x = x OP C
        //            w = c; // constant
        //            x = c + x; // constant: x = x OP C
        //            y = y / c; // constant: x = x OP C
        //            z = -z; // constant: x = x OP C
        //        }
        //    }
        //}
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "allLocalAssignmentsConstant3", "(Z)V",
                null, null);
        mn.visitCode();
        mn.visitIntInsn(BIPUSH, 7);
        mn.visitVarInsn(ISTORE, 1);
        mn.visitInsn(ICONST_2);
        mn.visitVarInsn(ISTORE, 2);
        mn.visitIntInsn(SIPUSH, 300);
        mn.visitVarInsn(ISTORE, 3);
        mn.visitIntInsn(BIPUSH, -4);
        mn.visitVarInsn(ISTORE, 4);
        mn.visitIntInsn(BIPUSH, 44);
        mn.visitVarInsn(ISTORE, 5);
        mn.visitInsn(ICONST_5);
        mn.visitVarInsn(ISTORE, 6);
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 7);
        Label l0 = new Label();
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 8, new Object[]{INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, INTEGER},
                0, new Object[0]);
        mn.visitVarInsn(ILOAD, 7);
        mn.visitIntInsn(BIPUSH, 55);
        Label l1 = new Label();
        mn.visitJumpInsn(IF_ICMPGE, l1);
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 8);
        Label l2 = new Label();
        mn.visitLabel(l2);
        mn.visitFrame(F_NEW, 1, new Object[]{INTEGER}, 0, new Object[0]);
        mn.visitVarInsn(ILOAD, 8);
        mn.visitIntInsn(BIPUSH, 10);
        Label l3 = new Label();
        mn.visitJumpInsn(IF_ICMPGE, l3);
        mn.visitVarInsn(ILOAD, 0);
        Label l4 = new Label();
        mn.visitJumpInsn(IFEQ, l4);
        mn.visitVarInsn(ILOAD, 2);
        mn.visitVarInsn(ILOAD, 2);
        mn.visitInsn(IMUL);
        mn.visitVarInsn(ILOAD, 2);
        mn.visitInsn(IADD);
        mn.visitIntInsn(SIPUSH, 385);
        mn.visitInsn(IADD);
        mn.visitIntInsn(BIPUSH, 99);
        mn.visitInsn(ISUB);
        mn.visitInsn(ICONST_2);
        mn.visitVarInsn(ILOAD, 2);
        mn.visitInsn(IMUL);
        mn.visitInsn(IDIV);
        mn.visitVarInsn(ISTORE, 2);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitVarInsn(ISTORE, 3);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitVarInsn(ILOAD, 4);
        mn.visitInsn(IADD);
        mn.visitVarInsn(ISTORE, 4);
        mn.visitVarInsn(ILOAD, 5);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitInsn(IDIV);
        mn.visitVarInsn(ISTORE, 5);
        mn.visitVarInsn(ILOAD, 6);
        mn.visitInsn(INEG);
        mn.visitVarInsn(ISTORE, 6);
        mn.visitLabel(l4);
        mn.visitFrame(F_NEW, 1, new Object[]{INTEGER}, 0, new Object[0]);
        mn.visitIincInsn(8, 1);
        mn.visitJumpInsn(GOTO, l2);
        mn.visitLabel(l3);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitIincInsn(7, 1);
        mn.visitJumpInsn(GOTO, l0);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(3, 9);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode argDependentAssignment() {
        //int d = b + 7; // dependent on arg1
        //int e = -a; // dependent on arg0
        //int f = 6 + c; // dependent on arg2
        //int g = a + b; // dependent on arg0 and arg1
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "argDependentAssignment", "(III)V",
                null, null);
        mn.visitCode();
        mn.visitVarInsn(ILOAD, 1);
        mn.visitIntInsn(BIPUSH, 7);
        mn.visitInsn(IADD);
        mn.visitVarInsn(ISTORE, 3);
        mn.visitVarInsn(ILOAD, 0);
        mn.visitInsn(INEG);
        mn.visitVarInsn(ISTORE, 4);
        mn.visitIntInsn(BIPUSH, 6);
        mn.visitVarInsn(ILOAD, 2);
        mn.visitInsn(IADD);
        mn.visitVarInsn(ISTORE, 5);
        mn.visitVarInsn(ILOAD, 0);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitInsn(IADD);
        mn.visitVarInsn(ISTORE, 6);
        mn.visitInsn(RETURN);
        mn.visitMaxs(2, 7);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode argDependentBranching() {
        //int a = -88; // constant
        //int b;
        //if(arg0) {
        //    b = 77;  // constant
        //} else {
        //    b = 144; // constant
        //}
        //b = b * a; // constant:  x = x OP C
        //int c = b;  // variant 0: reaching definition of b varies
        //int d = a + 77; // constant
        //a = a + b; // variant 0: reaching definition of b varies
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "argDependentBranching", "(Z)V",
                null, null);
        mn.visitCode();
        mn.visitIntInsn(BIPUSH, -88);
        mn.visitVarInsn(ISTORE, 1);
        mn.visitVarInsn(ILOAD, 0);
        Label l0 = new Label();
        mn.visitJumpInsn(IFEQ, l0);
        mn.visitIntInsn(BIPUSH, 77);
        mn.visitVarInsn(ISTORE, 2);
        Label l1 = new Label();
        mn.visitJumpInsn(GOTO, l1);
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 3, new Object[]{INTEGER, INTEGER, INTEGER}, 0, new Object[0]);
        mn.visitIntInsn(SIPUSH, 144);
        mn.visitVarInsn(ISTORE, 2);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 3, new Object[]{INTEGER, INTEGER, INTEGER}, 0, new Object[0]);
        mn.visitVarInsn(ILOAD, 2);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitInsn(IMUL);
        mn.visitVarInsn(ISTORE, 2);
        mn.visitVarInsn(ILOAD, 2);
        mn.visitVarInsn(ISTORE, 3);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitIntInsn(BIPUSH, 77);
        mn.visitInsn(IADD);
        mn.visitVarInsn(ISTORE, 4);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitVarInsn(ILOAD, 2);
        mn.visitInsn(IADD);
        mn.visitVarInsn(ISTORE, 1);
        mn.visitInsn(RETURN);
        mn.visitMaxs(2, 5);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode localSelfComputation() {
        //for(/* constant */ int i = 0; i < 5; i++) {
        //    a = a * 2; // constant
        //}
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "localSelfComputation", "(I)V",
                null, null);
        mn.visitCode();
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 1);
        Label l0 = new Label();
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 1, new Object[]{INTEGER}, 0, new Object[0]);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitInsn(ICONST_5);
        Label l1 = new Label();
        mn.visitJumpInsn(IF_ICMPGE, l1);
        mn.visitVarInsn(ILOAD, 0);
        mn.visitInsn(ICONST_2);
        mn.visitInsn(IMUL);
        mn.visitVarInsn(ISTORE, 0);
        mn.visitIincInsn(1, 1);
        mn.visitJumpInsn(GOTO, l0);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(2, 2);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode arraySelfComputation() {
        //for(/* constant */ int i = 0; i < 5; i++) {
        //    a[0] = a[0] * 2; // dependent on arg0
        //}
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "arraySelfComputation", "([I)V",
                null, null);
        mn.visitCode();
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 1);
        Label l0 = new Label();
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 1, new Object[]{INTEGER}, 0, new Object[0]);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitInsn(ICONST_5);
        Label l1 = new Label();
        mn.visitJumpInsn(IF_ICMPGE, l1);
        mn.visitVarInsn(ALOAD, 0);
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ALOAD, 0);
        mn.visitInsn(ICONST_0);
        mn.visitInsn(IALOAD);
        mn.visitInsn(ICONST_2);
        mn.visitInsn(IMUL);
        mn.visitInsn(IASTORE);
        mn.visitIincInsn(1, 1);
        mn.visitJumpInsn(GOTO, l0);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(4, 2);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode multiArraySelfComputation() {
        //for(/* constant */ int i = 0; i < 5; i++) {
        //    a[0][0] = a[0][0] * 2; // a[0] may have been redefined - variant +1
        //}
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "multiArraySelfComputation", "([[I)V",
                null, null);
        mn.visitCode();
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 1);
        Label l0 = new Label();
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 1, new Object[]{INTEGER}, 0, new Object[0]);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitInsn(ICONST_5);
        Label l1 = new Label();
        mn.visitJumpInsn(IF_ICMPGE, l1);
        mn.visitVarInsn(ALOAD, 0);
        mn.visitInsn(ICONST_0);
        mn.visitInsn(AALOAD);
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ALOAD, 0);
        mn.visitInsn(ICONST_0);
        mn.visitInsn(AALOAD);
        mn.visitInsn(ICONST_0);
        mn.visitInsn(IALOAD);
        mn.visitInsn(ICONST_2);
        mn.visitInsn(IMUL);
        mn.visitInsn(IASTORE);
        mn.visitIincInsn(1, 1);
        mn.visitJumpInsn(GOTO, l0);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(4, 2);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode localAssignedVariantValue() {
        //for(/* constant */ int i = 0; i < 5; i++) {
        //    a = i; // variant +1
        //}
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "localAssignedVariantValue", "(I)V",
                null, null);
        mn.visitCode();
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 1);
        Label l0 = new Label();
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 2, new Object[]{INTEGER, INTEGER}, 0, new Object[0]);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitInsn(ICONST_5);
        Label l1 = new Label();
        mn.visitJumpInsn(IF_ICMPGE, l1);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitVarInsn(ISTORE, 0);
        mn.visitIincInsn(1, 1);
        mn.visitJumpInsn(GOTO, l0);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 2, new Object[]{INTEGER, INTEGER}, 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(2, 2);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode arrayAssignedVariantValue() {
        //for(/* constant */ int i = 0; i < 5; i++) {
        //    a[0] = i; // variant +1
        //}
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "arrayAssignedVariantValue", "([I)V",
                null, null);
        mn.visitCode();
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 1);
        Label l0 = new Label();
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 2, new Object[]{"[I", INTEGER}, 0, new Object[0]);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitInsn(ICONST_5);
        Label l1 = new Label();
        mn.visitJumpInsn(IF_ICMPGE, l1);
        mn.visitVarInsn(ALOAD, 0);
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitInsn(IASTORE);
        mn.visitIincInsn(1, 1);
        mn.visitJumpInsn(GOTO, l0);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(3, 2);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode multiArrayAssignedVariantValue() {
        //for(/* constant */ int i = 0; i < 5; i++) {
        //    a[0][0] = i; // variant +1
        //}
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "multiArrayAssignedVariantValue", "([[I)V",
                null, null);
        mn.visitCode();
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 1);
        Label l0 = new Label();
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 1, new Object[]{INTEGER}, 0, new Object[0]);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitInsn(ICONST_5);
        Label l1 = new Label();
        mn.visitJumpInsn(IF_ICMPGE, l1);
        mn.visitVarInsn(ALOAD, 0);
        mn.visitInsn(ICONST_0);
        mn.visitInsn(AALOAD);
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitInsn(IASTORE);
        mn.visitIincInsn(1, 1);
        mn.visitJumpInsn(GOTO, l0);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(3, 2);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode twoArrays() {
        //for(/* constant */ int i = 0; i < 5; i++) {
        //    b[0]++; // dependent on arg1
        //    a[0] = b[0] + 27; // variant +1
        //}
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "twoArrays", "([I[I)V",
                null, null);
        mn.visitCode();
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 2);
        Label l0 = new Label();
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 3, new Object[]{"[I", "[I", INTEGER}, 0, new Object[0]);
        mn.visitVarInsn(ILOAD, 2);
        mn.visitInsn(ICONST_5);
        Label l1 = new Label();
        mn.visitJumpInsn(IF_ICMPGE, l1);
        mn.visitVarInsn(ALOAD, 1);
        mn.visitInsn(ICONST_0);
        mn.visitInsn(DUP2);
        mn.visitInsn(IALOAD);
        mn.visitInsn(ICONST_1);
        mn.visitInsn(IADD);
        mn.visitInsn(IASTORE);
        mn.visitVarInsn(ALOAD, 0);
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ALOAD, 1);
        mn.visitInsn(ICONST_0);
        mn.visitInsn(IALOAD);
        mn.visitIntInsn(BIPUSH, 27);
        mn.visitInsn(IADD);
        mn.visitInsn(IASTORE);
        mn.visitIincInsn(2, 1);
        mn.visitJumpInsn(GOTO, l0);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(4, 3);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode arrayAliasing() {
        //b = a; // dependent on arg0
        //a[0] = b[0] + 1; // dependent on arg0
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "arrayAliasing", "([I[I)V",
                null, null);
        mn.visitCode();
        mn.visitVarInsn(ALOAD, 0);
        mn.visitVarInsn(ASTORE, 1);
        mn.visitVarInsn(ALOAD, 0);
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ALOAD, 1);
        mn.visitInsn(ICONST_0);
        mn.visitInsn(IALOAD);
        mn.visitInsn(ICONST_1);
        mn.visitInsn(IADD);
        mn.visitInsn(IASTORE);
        mn.visitInsn(RETURN);
        mn.visitMaxs(4, 2);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode arrayAliasingVariant() {
        //if(arg2) {
        //    arg1 = arg0; // dependent on arg0
        //}
        //arg0[0] = arg1[0] + 1; // variant +0
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "arrayAliasingVariant", "([I[IZ)V",
                null, null);
        mn.visitCode();
        mn.visitVarInsn(ILOAD, 2);
        Label l0 = new Label();
        mn.visitJumpInsn(IFEQ, l0);
        mn.visitVarInsn(ALOAD, 0);
        mn.visitVarInsn(ASTORE, 1);
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 2, new Object[]{"[I", "[I", INTEGER}, 0, new Object[0]);
        mn.visitVarInsn(ALOAD, 0);
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ALOAD, 1);
        mn.visitInsn(ICONST_0);
        mn.visitInsn(IALOAD);
        mn.visitInsn(ICONST_1);
        mn.visitInsn(IADD);
        mn.visitInsn(IASTORE);
        mn.visitInsn(RETURN);
        mn.visitMaxs(4, 3);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode arrayElementRedefined() {
        //int[] arg0 = new int[5];
        //int i = arg0[0]; // variant +0
        //arg0[0] = 9; // dependent on arg0
        //arg0[0] = i + 6; // variant +0
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "arrayElementRedefined", "([I)V",
                null, null);
        mn.visitCode();
        mn.visitVarInsn(ALOAD, 0);
        mn.visitInsn(ICONST_0);
        mn.visitInsn(IALOAD);
        mn.visitVarInsn(ISTORE, 1);
        mn.visitVarInsn(ALOAD, 0);
        mn.visitInsn(ICONST_0);
        mn.visitIntInsn(BIPUSH, 9);
        mn.visitInsn(IASTORE);
        mn.visitVarInsn(ALOAD, 0);
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitIntInsn(BIPUSH, 6);
        mn.visitInsn(IADD);
        mn.visitInsn(IASTORE);
        mn.visitInsn(RETURN);
        mn.visitMaxs(4, 2);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode methodCallBetweenUses() {
        //int[] arg0 = new int[5];
        //int i = arg0[0]; // variant +0
        //arrayElementRedefined(arg0);
        //arg0[0] = i + 6; // variant +0
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "methodCallBetweenUses", "([I)V",
                null, null);
        mn.visitCode();
        mn.visitVarInsn(ALOAD, 0);
        mn.visitInsn(ICONST_0);
        mn.visitInsn(IALOAD);
        mn.visitVarInsn(ISTORE, 1);
        mn.visitVarInsn(ALOAD, 0);
        mn.visitMethodInsn(INVOKESTATIC, OWNER, "arrayElementRedefined", "([I)V", false);
        mn.visitVarInsn(ALOAD, 0);
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ILOAD, 1);
        mn.visitIntInsn(BIPUSH, 6);
        mn.visitInsn(IADD);
        mn.visitInsn(IASTORE);
        mn.visitInsn(RETURN);
        mn.visitMaxs(4, 2);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode indexOf() {
        //int z = 0; // constant
        //int[] a = new int[5]; // variant +0
        //for(/* constant */ int i = 0; i < a.length; i++) {
        //    if(a[i] == 0) {
        //        z = i; // variant +1
        //    }
        //}
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "indexOf", "()V",
                null, null);
        mn.visitCode();
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 0);
        mn.visitInsn(ICONST_5);
        mn.visitIntInsn(NEWARRAY, T_INT);
        mn.visitVarInsn(ASTORE, 1);
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 2);
        Label l0 = new Label();
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 3, new Object[]{INTEGER, "[I", INTEGER}, 0, new Object[0]);
        mn.visitVarInsn(ILOAD, 2);
        mn.visitVarInsn(ALOAD, 1);
        mn.visitInsn(ARRAYLENGTH);
        Label l1 = new Label();
        mn.visitJumpInsn(IF_ICMPGE, l1);
        mn.visitVarInsn(ALOAD, 1);
        mn.visitVarInsn(ILOAD, 2);
        mn.visitInsn(IALOAD);
        Label l2 = new Label();
        mn.visitJumpInsn(IFNE, l2);
        mn.visitVarInsn(ILOAD, 2);
        mn.visitVarInsn(ISTORE, 0);
        mn.visitLabel(l2);
        mn.visitFrame(F_NEW, 3, new Object[]{INTEGER, "[I", INTEGER}, 0, new Object[0]);
        mn.visitIincInsn(2, 1);
        mn.visitJumpInsn(GOTO, l0);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(2, 3);
        mn.visitEnd();
        return mn;
    }

    public static MethodNode indexOfBreak() {
        //int z = 0; // constant
        //int[] a = new int[5]; // variant +0
        //for(/*constant */ int i = 0; i < a.length; i++) {
        //    if(a[i] == 0) {
        //        z = i; // variant +0
        //        break;
        //    }
        //}
        MethodNode mn = new MethodNode(ACC_PUBLIC + ACC_STATIC, "indexOfBreak", "()V",
                null, null);
        mn.visitCode();
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 0);
        mn.visitInsn(ICONST_5);
        mn.visitIntInsn(NEWARRAY, T_INT);
        mn.visitVarInsn(ASTORE, 1);
        mn.visitInsn(ICONST_0);
        mn.visitVarInsn(ISTORE, 2);
        Label l0 = new Label();
        mn.visitLabel(l0);
        mn.visitFrame(F_NEW, 3, new Object[]{INTEGER, "[I", INTEGER}, 0, new Object[0]);
        mn.visitVarInsn(ILOAD, 2);
        mn.visitVarInsn(ALOAD, 1);
        mn.visitInsn(ARRAYLENGTH);
        Label l1 = new Label();
        mn.visitJumpInsn(IF_ICMPGE, l1);
        mn.visitVarInsn(ALOAD, 1);
        mn.visitVarInsn(ILOAD, 2);
        mn.visitInsn(IALOAD);
        Label l2 = new Label();
        mn.visitJumpInsn(IFNE, l2);
        mn.visitVarInsn(ILOAD, 2);
        mn.visitVarInsn(ISTORE, 0);
        mn.visitJumpInsn(GOTO, l1);
        mn.visitLabel(l2);
        mn.visitFrame(F_NEW, 3, new Object[]{INTEGER, "[I", INTEGER}, 0, new Object[0]);
        mn.visitIincInsn(2, 1);
        mn.visitJumpInsn(GOTO, l0);
        mn.visitLabel(l1);
        mn.visitFrame(F_NEW, 0, new Object[0], 0, new Object[0]);
        mn.visitInsn(RETURN);
        mn.visitMaxs(2, 3);
        mn.visitEnd();
        return mn;
    }
}
