package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.control.graph.BaseControlFlowGraphCreator;
import edu.columbia.cs.psl.phosphor.control.graph.BasicBlock;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.control.graph.SimpleBasicBlock;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.PhosphorOpcodeIgnoringAnalyzer;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeInterpreter;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.LocalVariable;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.StackElement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.AssignmentStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.IdleStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.ATHROW;

public class SSAAnalyzer {

    private static final InsnConverter insnToStatementConverter = InsnConverter.getChain();
    private final AbstractInsnNode[] instructions;
    private final Map<AbstractInsnNode, Frame<TypeValue>> frameMap = new HashMap<>();
    private final Map<AbstractInsnNode, String> explicitExceptions = new HashMap<>();
    private final Statement[][] statements;
    private final FlowGraph<BasicBlock> cfg;
    private final Map<AbstractInsnNode, BasicBlock> insnBasicBlockMap = new HashMap<>();
    private final Map<Expression, Set<BasicBlock>> persistentVariableDefinitions = new HashMap<>();
    private final Map<Expression, Set<BasicBlock>> phiFunctionLocations = new HashMap<>();

    public SSAAnalyzer(String owner, MethodNode methodNode) throws AnalyzerException {
        instructions = methodNode.instructions.toArray();
        Frame<TypeValue>[] frames = new PhosphorOpcodeIgnoringAnalyzer<>(new TypeInterpreter(owner, methodNode))
                .analyze(owner, methodNode);
        for(int i = 0; i < frames.length; i++) {
            frameMap.put(instructions[i], frames[i]);
        }
        calculateExplicitExceptions(frames);
        cfg = new SSAControlFlowGraphCreator(insnBasicBlockMap).createControlFlowGraph(methodNode, explicitExceptions);
        statements = convertInstructionsToThreeAddress(instructions, frames);
        locatePersistentVariableDefinitions();
        calculatePhiFunctionLocations();
    }

    List<Statement> getFlattenedStatementsList() {
        return flatten(statements);
    }

    private void calculateExplicitExceptions(Frame<TypeValue>[] frames) {
        for(int i = 0; i < instructions.length; i++) {
            if(instructions[i].getOpcode() == ATHROW) {
                Frame<TypeValue> frame = frames[i];
                TypeValue top = frame.getStack(frame.getStackSize() - 1);
                Type type = top.getType();
                explicitExceptions.put(instructions[i], type.getClassName().replace(".", "/"));
            }
        }
    }

    private void locatePersistentVariableDefinitions() {
        for(int i = 0; i < instructions.length; i++) {
            BasicBlock block = insnBasicBlockMap.get(instructions[i]);
            for(Statement s : statements[i]) {
                if(s instanceof AssignmentStatement) {
                    Expression expr = ((AssignmentStatement) s).getLeftHandSide();
                    if(isPersistentDefinition(block, expr)) {
                        if(!persistentVariableDefinitions.containsKey(expr)) {
                            persistentVariableDefinitions.put(expr, new HashSet<>());
                        }
                        persistentVariableDefinitions.get(expr).add(block);
                    }
                }
            }
        }
    }

    private boolean isPersistentDefinition(BasicBlock block, Expression expr) {
        if(expr instanceof LocalVariable || expr instanceof StackElement) {
            int index = expr instanceof LocalVariable ? ((LocalVariable) expr).getIndex() : ((StackElement) expr).getIndex();
            for(BasicBlock successor : cfg.getSuccessors(block)) {
                AbstractInsnNode succeedingInsn = successor.getFirstInsn();
                Frame<TypeValue> succeedingFrame = frameMap.get(succeedingInsn);
                if(succeedingFrame != null) {
                    TypeValue type = TypeValue.UNINITIALIZED_VALUE;
                    if(expr instanceof LocalVariable) {
                        if(index < succeedingFrame.getLocals()) {
                            type = succeedingFrame.getLocal(index);
                        }
                    } else {
                        if(index < succeedingFrame.getStackSize()) {
                            type = succeedingFrame.getStack(index);
                        }
                    }
                    if(type != TypeValue.UNINITIALIZED_VALUE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void calculatePhiFunctionLocations() {
        Map<BasicBlock, Set<BasicBlock>> dominanceFrontiers = cfg.getDominanceFrontiers();
        for(Expression variable : persistentVariableDefinitions.keySet()) {
            Set<BasicBlock> locations = calculatePhiFunctionLocations(persistentVariableDefinitions.get(variable),
                    dominanceFrontiers);
            phiFunctionLocations.put(variable, locations);
        }
    }

    private static Set<BasicBlock> calculatePhiFunctionLocations(Set<BasicBlock> definingBlocks,
                                                                 Map<BasicBlock, Set<BasicBlock>> dominanceFrontiers) {
        LinkedList<BasicBlock> workingSet = new LinkedList<>(definingBlocks);
        Set<BasicBlock> phiFunctionLocations = new HashSet<>();
        while(!workingSet.isEmpty()) {
            BasicBlock x = workingSet.poll();
            for(BasicBlock y : dominanceFrontiers.get(x)) {
                if(phiFunctionLocations.add(y) && !definingBlocks.contains(y)) {
                    workingSet.add(y);
                }
            }
        }
        return phiFunctionLocations;
    }

    private static Statement[][] convertInstructionsToThreeAddress(AbstractInsnNode[] instructions, Frame<TypeValue>[] frames) {
        Statement[][] statements = new Statement[instructions.length][];
        for(int i = 0; i < instructions.length; i++) {
            if(frames[i] == null) {
                statements[i] = new Statement[]{IdleStatement.NOP};
            } else {
                statements[i] = insnToStatementConverter.convert(instructions[i], frames[i]);
            }
        }
        return statements;
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

    private static class SSAControlFlowGraphCreator extends BaseControlFlowGraphCreator {

        private final Map<AbstractInsnNode, BasicBlock> insnBasicBlockMap;

        public SSAControlFlowGraphCreator(Map<AbstractInsnNode, BasicBlock> insnBasicBlockMap) {
            super(true);
            this.insnBasicBlockMap = insnBasicBlockMap;
        }

        @Override
        protected BasicBlock addBasicBlock(AbstractInsnNode[] instructions, int index) {
            BasicBlock basicBlock = new SimpleBasicBlock(instructions, index);
            builder.addVertex(basicBlock);
            for(AbstractInsnNode insn : instructions) {
                insnBasicBlockMap.put(insn, basicBlock);
            }
            return basicBlock;
        }
    }
}
