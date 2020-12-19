package edu.neu.ccs.conflux.internal.policy;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.ClassReader;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.ClassNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.LinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Predicate;

public class ControlAnalysisTestUtil {

    public static MethodNode getMethodNode(Class<?> clazz, String methodName) throws NoSuchMethodException, IOException {
        ClassReader cr = new ClassReader(clazz.getName());
        ClassNode classNode = new ClassNode();
        cr.accept(classNode, ClassReader.EXPAND_FRAMES);
        for(MethodNode mn : classNode.methods) {
            if(mn.name.equals(methodName)) {
                return mn;
            }
        }
        throw new NoSuchMethodException();
    }
    public static List<AbstractInsnNode> filterInstructions(MethodNode mn, Predicate<AbstractInsnNode> predicate) {
        List<AbstractInsnNode> filtered = new LinkedList<>();
        Iterator<AbstractInsnNode> itr = mn.instructions.iterator();
        while(itr.hasNext()) {
            AbstractInsnNode insn = itr.next();
            if(predicate.test(insn)) {
                filtered.add(insn);
            }
        }
        return filtered;
    }
}
