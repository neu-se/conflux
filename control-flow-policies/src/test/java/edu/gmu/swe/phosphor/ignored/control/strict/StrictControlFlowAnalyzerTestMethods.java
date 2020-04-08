package edu.gmu.swe.phosphor.ignored.control.strict;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public class StrictControlFlowAnalyzerTestMethods {

    public static final String OWNER = Type.getInternalName(StrictControlFlowAnalyzerTestMethods.class);

    public static MethodNode switchMultipleBranchesSameTarget() {
        //switch(i) {
        //    case 0:
        //        return 2;
        //    case 10:
        //    case 78:
        //        return 5;
        //    case 100:
        //    default:
        //        return 7;
        //}
        MethodNode methodNode = new MethodNode(ACC_PUBLIC + ACC_STATIC, "switchMultipleBranchesSameTarget",
                "(I)I", null, null);
        methodNode.visitCode();
        methodNode.visitVarInsn(ILOAD, 0);
        Label l0 = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        methodNode.visitLookupSwitchInsn(l2, new int[]{0, 10, 78, 100}, new Label[]{l0, l1, l1, l2});
        methodNode.visitLabel(l0);
        methodNode.visitFrame(F_NEW, 1, new Object[]{INTEGER}, 0, new Object[0]);
        methodNode.visitInsn(ICONST_2);
        methodNode.visitInsn(IRETURN);
        methodNode.visitLabel(l1);
        methodNode.visitFrame(F_NEW, 1, new Object[]{INTEGER}, 0, new Object[0]);
        methodNode.visitInsn(ICONST_5);
        methodNode.visitInsn(IRETURN);
        methodNode.visitLabel(l2);
        methodNode.visitFrame(F_NEW, 1, new Object[]{INTEGER}, 0, new Object[0]);
        methodNode.visitIntInsn(BIPUSH, 7);
        methodNode.visitInsn(IRETURN);
        methodNode.visitMaxs(1, 1);
        methodNode.visitEnd();
        return methodNode;
    }
}
