package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.control.graph.BaseControlFlowGraphCreator;
import edu.columbia.cs.psl.phosphor.control.graph.BasicBlock;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashMap;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.ATHROW;

class SSAControlFlowGraphCreator extends BaseControlFlowGraphCreator {

    private final Map<AbstractInsnNode, AnnotatedInstruction> annotationMap;
    private final MethodNode methodNode;

    public SSAControlFlowGraphCreator(MethodNode methodNode, Map<AbstractInsnNode, AnnotatedInstruction> annotationMap) {
        super(true);
        this.methodNode = methodNode;
        this.annotationMap = annotationMap;
    }

    public FlowGraph<BasicBlock> createControlFlowGraph() {
        return super.createControlFlowGraph(methodNode, calculateExplicitExceptions(annotationMap.values()));
    }

    @Override
    protected AnnotatedBasicBlock addBasicBlock(AbstractInsnNode[] instructions, int index) {
        AnnotatedInstruction[] annotatedInstructions = new AnnotatedInstruction[instructions.length];
        for(int i = 0; i < instructions.length; i++) {
            annotatedInstructions[i] = annotationMap.get(instructions[i]);
        }
        AnnotatedBasicBlock basicBlock = new AnnotatedBasicBlock(annotatedInstructions, index);
        builder.addVertex(basicBlock);
        for(AnnotatedInstruction annotatedInstruction : annotatedInstructions) {
            annotatedInstruction.setBasicBlock(basicBlock);
        }
        return basicBlock;
    }


    private static Map<AbstractInsnNode, String> calculateExplicitExceptions(Iterable<AnnotatedInstruction> instructions) {
        Map<AbstractInsnNode, String> explicitExceptions = new HashMap<>();
        for(AnnotatedInstruction instruction : instructions) {
            Frame<TypeValue> frame = instruction.getFrame();
            if(instruction.getInstruction().getOpcode() == ATHROW && frame != null) {
                TypeValue top = frame.getStack(frame.getStackSize() - 1);
                Type type = top.getType();
                explicitExceptions.put(instruction.getInstruction(), type.getClassName().replace(".", "/"));
            }
        }
        return explicitExceptions;
    }
}
