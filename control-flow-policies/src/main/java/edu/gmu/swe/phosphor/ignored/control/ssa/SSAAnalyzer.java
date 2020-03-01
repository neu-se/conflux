package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.control.graph.BaseControlFlowGraphCreator;
import edu.columbia.cs.psl.phosphor.control.graph.BasicBlock;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.PhosphorOpcodeIgnoringAnalyzer;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeInterpreter;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.InsnList;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashMap;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.LinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

import java.util.Iterator;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.ATHROW;

public class SSAAnalyzer {

    private static final InsnConverter insnToStatementConverter = InsnConverter.getChain();
    private final InsnList instructions;
    private final Frame<TypeValue>[] frames;
    private final Map<AbstractInsnNode, String> explicitExceptions = new HashMap<>();
    private final FlowGraph<BasicBlock> cfg;

    public SSAAnalyzer(String owner, MethodNode methodNode) throws AnalyzerException {
        instructions = methodNode.instructions;
        frames = new PhosphorOpcodeIgnoringAnalyzer<>(new TypeInterpreter(owner, methodNode)).analyze(owner, methodNode);
        calculateExplicitExceptions();
        cfg = new BaseControlFlowGraphCreator(true)
                .createControlFlowGraph(methodNode, explicitExceptions);
        Iterator<AbstractInsnNode> itr = instructions.iterator();
        int i = 0;
        Statement[][] statements = new Statement[instructions.size()][];
        while(itr.hasNext()) {
            statements[i] = insnToStatementConverter.convert(itr.next(), frames[i]);
            i++;
        }
        List<Statement> flat = flatten(statements);
    }

    private void calculateExplicitExceptions() {
        int i = 0;
        Iterator<AbstractInsnNode> itr = instructions.iterator();
        while(itr.hasNext()) {
            AbstractInsnNode insn = itr.next();
            if(insn.getOpcode() == ATHROW) {
                Frame<TypeValue> frame = frames[i];
                TypeValue top = frame.pop();
                Type type = top.getType();
                explicitExceptions.put(insn, type.getClassName().replace(".", "/"));
            }
            i++;
        }
    }

    private static List<Statement> flatten(Statement[][] statements) {
        List<Statement> flattenedList = new LinkedList<>();
        for(Statement[] arr : statements) {
            for(Statement s : arr) {
                flattenedList.add(s);
            }
        }
        return flattenedList;
    }
}
