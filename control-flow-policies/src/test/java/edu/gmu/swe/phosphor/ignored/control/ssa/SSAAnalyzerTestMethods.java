package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import jdk.nashorn.internal.codegen.types.Type;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

@SuppressWarnings("unused")
public class SSAAnalyzerTestMethods {

    public static final String OWNER = Type.getInternalName(SSAAnalyzerTestMethods.class);
    private long j;

    public long dup2X1() {
        return j++;
    }

    public static MethodNode getMethodNodeDup2X1() {
        MethodNode methodNode = new MethodNode(ACC_PUBLIC, "dup2X1", "()J", null, null);
        methodNode.visitCode();
        methodNode.visitVarInsn(ALOAD, 0);
        methodNode.visitInsn(DUP);
        methodNode.visitFieldInsn(GETFIELD, OWNER, "j", "J");
        methodNode.visitInsn(DUP2_X1);
        methodNode.visitInsn(LCONST_1);
        methodNode.visitInsn(LADD);
        methodNode.visitFieldInsn(PUTFIELD, OWNER, "j", "J");
        methodNode.visitInsn(LRETURN);
        methodNode.visitMaxs(7, 1);
        methodNode.visitEnd();
        return methodNode;
    }

    public static void indexOfBreak() {
        int z = 0; // constant
        int[] a = new int[5]; // variant +0
        for(/*constant */ int i = 0; i < a.length; i++) {
            if(a[i] == 0) {
                z = i; // variant +1
                break;
            }
        }
    }
}
