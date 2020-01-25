package edu.gmu.swe.phosphor.ignored.control;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.ClassReader;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.ClassNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;

import java.io.IOException;

public class ControlAnalysisTestUtil {

    private ControlAnalysisTestUtil() {

    }

    public static MethodNode getMethodNode(Class<?> clazz, String methodName) throws NoSuchMethodException, IOException {
        ClassReader cr = new ClassReader(clazz.getName());
        ClassNode classNode = new ClassNode();
        cr.accept(classNode, 0);
        for(MethodNode mn : classNode.methods) {
            if(mn.name.equals(methodName)) {
                return mn;
            }
        }
        throw new NoSuchMethodException();
    }
}
