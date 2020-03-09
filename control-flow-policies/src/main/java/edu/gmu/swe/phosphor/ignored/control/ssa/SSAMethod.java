package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.control.graph.*;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph.NaturalLoop;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.gmu.swe.phosphor.ignored.control.FlowGraphUtil;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.LocalVariable;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.StackElement;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;
import edu.gmu.swe.phosphor.ignored.control.tac.ThreeAddressBasicBlock;
import edu.gmu.swe.phosphor.ignored.control.tac.ThreeAddressControlFlowGraphCreator;
import edu.gmu.swe.phosphor.ignored.control.tac.ThreeAddressEntryPoint;
import edu.gmu.swe.phosphor.ignored.control.tac.ThreeAddressMethod;

import java.util.function.Function;

import static edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeValue.UNINITIALIZED_VALUE;
import static edu.gmu.swe.phosphor.ignored.control.FlowGraphUtil.calculateContainingLoops;
import static edu.gmu.swe.phosphor.ignored.control.FlowGraphUtil.createInsnBlockMap;

/**
 * Uses algorithms from the following for placing phi functions and renaming variables:
 * <p>Ron Cytron, Jeanne Ferrante, Barry K. Rosen, Mark N. Wegman, and F. Kenneth Zadeck.
 * 1991. Efficiently computing static single assignment form and the control dependence graph.
 * ACM Trans. Program. Lang. Syst. 13, 4 (October 1991), 451–490. DOI: https://doi.org/10.1145/115372.115320
 */
public class SSAMethod {

    private final List<Statement> parameterDefinitions;

    /**
     * The control flow graph for the method includes edges from instructions that can potentially throw an exception
     * handled by one of its exception handlers to the start of that exception handler.
     */
    private final FlowGraph<BasicBlock> controlFlowGraph;

    /**
     * A unmodifiable mapping from each instruction in the method to the basic block that contains it.
     */
    private final Map<AbstractInsnNode, BasicBlock> insnBlockMap;

    /**
     * An unmodifiable mapping from each instruction in the method to a frame representing the state of local variables
     * and the runtime stack immediately before the instruction is executed.
     */
    private final Map<AbstractInsnNode, Frame<TypeValue>> frameMap;

    /**
     * An unmodifiable mapping from each instruction in the method to an unmodifiable set containing of
     * natural loops that contain the instruction.
     */
    private final Map<BasicBlock, Set<NaturalLoop<BasicBlock>>> containingLoopsMap;

    private final Map<BasicBlock, ThreeAddressBasicBlock> blockShadowMap;
    private final FlowGraph<SSABasicBlock> ssaControlFlowGraph;

    public SSAMethod(String owner, MethodNode method) throws AnalyzerException {
        ThreeAddressMethod threeAddressMethod = new ThreeAddressMethod(owner, method);
        ThreeAddressControlFlowGraphCreator creator = new ThreeAddressControlFlowGraphCreator(threeAddressMethod);
        controlFlowGraph = creator.createControlFlowGraph(method, threeAddressMethod.calculateExplicitExceptions());
        blockShadowMap = creator.getShadowMap();
        insnBlockMap = Collections.unmodifiableMap(createInsnBlockMap(controlFlowGraph));
        frameMap = Collections.unmodifiableMap(new HashMap<>(threeAddressMethod.getFrameMap()));
        containingLoopsMap = Collections.unmodifiableMap(calculateContainingLoops(controlFlowGraph));
        placePhiFunctions();
        renameVariables(threeAddressMethod.collectDefinedVariables());
        parameterDefinitions = initializeParameterDefinitions();
        ssaControlFlowGraph = createSSAControlFlowGraph();
    }

    private List<Statement> initializeParameterDefinitions() {
        ThreeAddressEntryPoint entry = (ThreeAddressEntryPoint) blockShadowMap.get(controlFlowGraph.getEntryPoint());
        return entry.getSsaStatements();
    }

    public FlowGraph<BasicBlock> getControlFlowGraph() {
        return controlFlowGraph;
    }

    public FlowGraph<SSABasicBlock> getSsaControlFlowGraph() {
        return ssaControlFlowGraph;
    }

    public List<Statement> getParameterDefinitions() {
        return parameterDefinitions;
    }

    private void placePhiFunctions() {
        Map<VariableExpression, Set<BasicBlock>> persistentVarDefs = locatePersistentVarDefs();
        for(VariableExpression expr : persistentVarDefs.keySet()) {
            LinkedList<BasicBlock> workingSet = new LinkedList<>(persistentVarDefs.get(expr));
            Set<BasicBlock> visited = new HashSet<>(workingSet);
            while(!workingSet.isEmpty()) {
                BasicBlock x = workingSet.poll();
                for(BasicBlock y : controlFlowGraph.getDominanceFrontiers().get(x)) {
                    if(isDefinedAtFrame(frameMap.get(y.getFirstInsn()), expr)) {
                        addPhiFunctionForVariable(y, expr);
                    }
                    if(visited.add(y)) {
                        workingSet.add(y);
                    }
                }
            }
        }
    }

    private void addPhiFunctionForVariable(BasicBlock y, VariableExpression expr) {
        blockShadowMap.get(y).addPhiFunctionForVariable(expr);
    }

    private void renameVariables(Set<VariableExpression> definedExpressions) {
        Map<VariableExpression, VersionStack> versionStacks = new HashMap<>();
        for(VariableExpression expression : definedExpressions) {
            versionStacks.put(expression, new VersionStack(expression));
        }
        search(controlFlowGraph.getEntryPoint(), versionStacks);
    }

    private void search(BasicBlock block, Map<VariableExpression, VersionStack> versionStacks) {
        for(VersionStack stack : versionStacks.values()) {
            stack.processingBlock();
        }
        processStatements(block, versionStacks);
        for(BasicBlock successor : controlFlowGraph.getSuccessors(block)) {
            addPhiFunctionValues(successor, versionStacks);
        }
        for(BasicBlock child : controlFlowGraph.getDominatorTree().get(block)) {
            search(child, versionStacks);
        }
        for(VersionStack stack : versionStacks.values()) {
            stack.finishedProcessingBlock();
        }
    }

    private void addPhiFunctionValues(BasicBlock successor, Map<VariableExpression, VersionStack> versionStacks) {
        blockShadowMap.get(successor).addPhiFunctionValues(versionStacks);
    }

    private void processStatements(BasicBlock block, Map<VariableExpression, VersionStack> versionStacks) {
        blockShadowMap.get(block).processStatements(versionStacks);
    }

    public List<Statement> createStatementList() {
        List<Statement> list = new LinkedList<>();
        List<SSABasicBlock> blocks = new LinkedList<>(ssaControlFlowGraph.getVertices());
        Collections.sort(blocks, (object1, object2) -> Integer.compare(object1.getIndex(), object2.getIndex()));
        for(SSABasicBlock block : blocks) {
            list.addAll(block.getStatements());
        }
        return list;
    }

    private Map<VariableExpression, Set<BasicBlock>> locatePersistentVarDefs() {
        Map<VariableExpression, Set<BasicBlock>> persistentVarDefs = new HashMap<>();
        for(BasicBlock block : controlFlowGraph.getVertices()) {
            for(Statement s : getThreeAddressStatements(block)) {
                if(s.definesVariable()) {
                    VariableExpression expr = s.getDefinedVariable();
                    if(isPersistentDefinition(block, expr)) {
                        if(!persistentVarDefs.containsKey(expr)) {
                            persistentVarDefs.put(expr, new HashSet<>());
                        }
                        persistentVarDefs.get(expr).add(block);
                    }
                }
            }
        }
        return persistentVarDefs;
    }

    private Iterable<? extends Statement> getThreeAddressStatements(BasicBlock block) {
        return blockShadowMap.get(block).getThreeAddressStatements();
    }

    private boolean isPersistentDefinition(BasicBlock block, VariableExpression expr) {
        for(BasicBlock successor : controlFlowGraph.getSuccessors(block)) {
            if(isDefinedAtFrame(frameMap.get(successor.getFirstInsn()), expr)) {
                return true;
            }
        }
        return false;
    }

    private FlowGraph<SSABasicBlock> createSSAControlFlowGraph() {
        Function<BasicBlock, SSABasicBlock> converter = new Function<BasicBlock, SSABasicBlock>() {
            @Override
            public SSABasicBlock apply(BasicBlock vertex) {
                int rank = -2;
                if(vertex instanceof EntryPoint) {
                    rank = -1;
                } else if(vertex instanceof ExitPoint) {
                    rank = controlFlowGraph.getVertices().size() - 1;
                } else if(vertex instanceof SimpleBasicBlock) {
                    rank = ((SimpleBasicBlock) vertex).getIdentifier() + 1;
                }
                return blockShadowMap.get(vertex).createSSABasicBlock(rank);
            }
        };
        return FlowGraphUtil.convertVertices(controlFlowGraph, converter);
    }

    private static boolean isDefinedAtFrame(Frame<TypeValue> frame, VariableExpression expr) {
        if(frame != null) {
            if(expr instanceof LocalVariable) {
                int index = ((LocalVariable) expr).getIndex();
                return index < frame.getLocals() && frame.getLocal(index) != UNINITIALIZED_VALUE;
            } else if(expr instanceof StackElement) {
                int index = ((StackElement) expr).getIndex();
                return index < frame.getStackSize() && frame.getStack(index) != UNINITIALIZED_VALUE;
            }
        }
        return false;
    }
}
