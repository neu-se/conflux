package edu.neu.ccs.conflux.internal.policy.ssa;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.ClassReader;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.ClassNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Comparator;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

import static edu.columbia.cs.psl.phosphor.control.graph.BasicBlock.getNumericLabelNames;

public final class SSADotGraphDriver {
    private SSADotGraphDriver() {
    }

    public static void main(String[] args) throws Exception {
        File classFile = new File(args[0]);
        File outputDirectory = new File(args[1]);
        ClassNode classNode = new ClassNode();
        new ClassReader(Files.newInputStream(classFile.toPath())).accept(classNode, ClassReader.EXPAND_FRAMES);
        int methodNum = 0;
        for (MethodNode method : classNode.methods) {
            File f = new File(outputDirectory, String.format("%s%d.gv", method.name, methodNum));
            try (PrintWriter writer = new PrintWriter(f)) {
                writeGraph(classNode.name, method, writer);
                methodNum++;
            }
        }
    }

    private static void writeGraph(String owner, MethodNode mn, PrintWriter writer)
            throws AnalyzerException, IOException {
        SSAMethod ssaMethod = new SSAMethod(owner, mn);
        Comparator<AnnotatedBasicBlock> comparator = (b1, b2) -> Integer.compare(b1.getIndex(), b2.getIndex());
        Map<Label, String> labelNames = getNumericLabelNames(mn.instructions);
        ssaMethod.getControlFlowGraph()
                .write(writer, String.format("\"%s.%s%s\"", owner, mn.name, mn.desc), comparator,
                        (b) -> b.toDotString(labelNames), 20);
    }
}
