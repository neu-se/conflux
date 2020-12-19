package edu.neu.ccs.conflux.internal.policy.ssa;

import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.control.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.LocalVariable;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.StackElement;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.VariableExpression;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.Statement;
import edu.neu.ccs.conflux.internal.policy.FlowGraphUtil;
import edu.neu.ccs.conflux.internal.policy.tac.ThreeAddressBasicBlock;
import edu.neu.ccs.conflux.internal.policy.tac.ThreeAddressControlFlowGraphCreator;
import edu.neu.ccs.conflux.internal.policy.tac.ThreeAddressMethod;
import jdk.internal.org.objectweb.asm.Opcodes;

import static edu.columbia.cs.psl.phosphor.control.type.TypeValue.UNINITIALIZED_VALUE;

/**
 * Uses algorithms from the following for placing phi functions and renaming variables:
 * <p>Ron Cytron, Jeanne Ferrante, Barry K. Rosen, Mark N. Wegman, and F. Kenneth Zadeck.
 * 1991. Efficiently computing static single assignment form and the control dependence graph.
 * ACM Trans. Program. Lang. Syst. 13, 4 (October 1991), 451–490. DOI: https://doi.org/10.1145/115372.115320
 */
public class SSAMethod {

    /**
     * The control flow graph for the method includes edges from instructions that can potentially throw an exception
     * handled by one of its exception handlers to the start of that exception handler.
     */
    private final FlowGraph<AnnotatedBasicBlock> controlFlowGraph;

    /**
     * An unmodifiable mapping from each instruction in the method to a frame representing the state of local variables
     * and the runtime stack immediately before the instruction is executed.
     */
    private final Map<AbstractInsnNode, Frame<TypeValue>> frameMap;

    private final VersionAssigningVisitor versionAssigner;

    private final List<Type> parameterTypes;

    private final Type returnType;

    public SSAMethod(String owner, MethodNode method) throws AnalyzerException {
        ThreeAddressMethod threeAddressMethod = new ThreeAddressMethod(owner, method);
        parameterTypes = Collections.unmodifiableList(createParameterTypes(owner, method));
        returnType = Type.getReturnType(method.desc);
        FlowGraph<ThreeAddressBasicBlock> tacGraph = new ThreeAddressControlFlowGraphCreator(threeAddressMethod)
                .createControlFlowGraph(method, threeAddressMethod.calculateExplicitExceptions());
        frameMap = Collections.unmodifiableMap(new HashMap<>(threeAddressMethod.getFrameMap()));
        placePhiFunctions(tacGraph);
        versionAssigner = new VersionAssigningVisitor(threeAddressMethod.collectDefinedVariables());
        renameVariables(tacGraph, tacGraph.getEntryPoint());
        controlFlowGraph = FlowGraphUtil.convertVertices(tacGraph, ThreeAddressBasicBlock::createSSABasicBlock);
    }

    public Map<AbstractInsnNode, Frame<TypeValue>> getFrameMap() {
        return frameMap;
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<Type> getParameterTypes() {
        return parameterTypes;
    }

    public FlowGraph<AnnotatedBasicBlock> getControlFlowGraph() {
        return controlFlowGraph;
    }

    public Map<VariableExpression, VersionStack> getVersionStacks() {
        return versionAssigner.getVersionStacks();
    }

    private List<Type> createParameterTypes(String owner, MethodNode method) {
        Type[] argTypes = Type.getArgumentTypes(method.desc);
        LinkedList<Type> types = new LinkedList<>(Arrays.asList(argTypes));
        if((method.access & Opcodes.ACC_STATIC) == 0) {
            types.addFirst(Type.getObjectType(owner));
        }
        return types;
    }

    private void placePhiFunctions(FlowGraph<ThreeAddressBasicBlock> tacGraph) {
        Map<VariableExpression, Set<ThreeAddressBasicBlock>> persistentVarDefs = locatePersistentVarDefs(tacGraph);
        for(VariableExpression expr : persistentVarDefs.keySet()) {
            LinkedList<ThreeAddressBasicBlock> workingSet = new LinkedList<>(persistentVarDefs.get(expr));
            Set<ThreeAddressBasicBlock> visited = new HashSet<>(workingSet);
            while(!workingSet.isEmpty()) {
                ThreeAddressBasicBlock x = workingSet.poll();
                if(tacGraph.getDominanceFrontiers().containsKey(x)) {
                    for(ThreeAddressBasicBlock y : tacGraph.getDominanceFrontiers().get(x)) {
                        if(isDefinedAtFrame(frameMap.get(y.getFirstInsn()), expr)) {
                            y.addPhiFunctionForVariable(expr);
                        }
                        if(visited.add(y)) {
                            workingSet.add(y);
                        }
                    }
                }
            }
        }
    }

    private Map<VariableExpression, Set<ThreeAddressBasicBlock>> locatePersistentVarDefs(FlowGraph<ThreeAddressBasicBlock> tacGraph) {
        Map<VariableExpression, Set<ThreeAddressBasicBlock>> persistentVarDefs = new HashMap<>();
        for(ThreeAddressBasicBlock block : tacGraph.getVertices()) {
            for(Statement s : block.getThreeAddressStatements()) {
                if(s.definesVariable()) {
                    VariableExpression expr = s.getDefinedVariable();
                    if(isPersistentDefinition(tacGraph, block, expr)) {
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

    private boolean isPersistentDefinition(FlowGraph<ThreeAddressBasicBlock> tacGraph,
                                           ThreeAddressBasicBlock block, VariableExpression expr) {
        for(ThreeAddressBasicBlock successor : tacGraph.getSuccessors(block)) {
            if(isDefinedAtFrame(frameMap.get(successor.getFirstInsn()), expr)) {
                return true;
            }
        }
        return false;
    }

    private void renameVariables(FlowGraph<ThreeAddressBasicBlock> tacGraph, ThreeAddressBasicBlock block) {
        versionAssigner.processingBlock();
        block.processStatements(versionAssigner);
        for(ThreeAddressBasicBlock successor : tacGraph.getSuccessors(block)) {
            successor.addPhiFunctionValues(versionAssigner);
        }
        for(ThreeAddressBasicBlock child : tacGraph.getDominatorTree().get(block)) {
            renameVariables(tacGraph, child);
        }
        versionAssigner.finishedProcessingBlock();
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
