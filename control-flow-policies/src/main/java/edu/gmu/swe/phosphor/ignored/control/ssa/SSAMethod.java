package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.control.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.gmu.swe.phosphor.ignored.control.FlowGraphUtil;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.AssignmentStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;
import edu.gmu.swe.phosphor.ignored.control.tac.ThreeAddressBasicBlock;
import edu.gmu.swe.phosphor.ignored.control.tac.ThreeAddressControlFlowGraphCreator;
import edu.gmu.swe.phosphor.ignored.control.tac.ThreeAddressMethod;
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

    private final PropagationTransformer transformer;

    private final Map<VariableExpression, VersionStack> versionStacks = new HashMap<>();

    private final int numberOfParameters;

    private final List<Type> parameterTypes;

    public SSAMethod(String owner, MethodNode method) throws AnalyzerException {
        ThreeAddressMethod threeAddressMethod = new ThreeAddressMethod(owner, method);
        parameterTypes = Collections.unmodifiableList(createParameterTypes(owner, method));
        numberOfParameters = threeAddressMethod.getParameterDefinitions().size();
        FlowGraph<ThreeAddressBasicBlock> tacGraph = new ThreeAddressControlFlowGraphCreator(threeAddressMethod)
                .createControlFlowGraph(method, threeAddressMethod.calculateExplicitExceptions());
        frameMap = Collections.unmodifiableMap(new HashMap<>(threeAddressMethod.getFrameMap()));
        placePhiFunctions(tacGraph);
        renameVariables(tacGraph, threeAddressMethod.collectDefinedVariables());
        transformer = new PropagationTransformer(propagateVariables(tacGraph));
        controlFlowGraph = FlowGraphUtil.convertVertices(tacGraph, vertex -> vertex.createSSABasicBlock(transformer));
    }

    private List<Type> createParameterTypes(String owner, MethodNode method) {
        Type[] argTypes = Type.getArgumentTypes(method.desc);
        LinkedList<Type> types = new LinkedList<>(Arrays.asList(argTypes));
        if((method.access & Opcodes.ACC_STATIC) == 0) {
            types.addFirst(Type.getType("L" + owner + ";"));
        }
        return types;
    }

    public int getNumberOfParameters() {
        return numberOfParameters;
    }

    public List<Type> getParameterTypes() {
        return parameterTypes;
    }

    public FlowGraph<AnnotatedBasicBlock> getControlFlowGraph() {
        return controlFlowGraph;
    }

    public Map<VariableExpression, VersionStack> getVersionStacks() {
        return Collections.unmodifiableMap(versionStacks);
    }

    public PropagationTransformer getTransformer() {
        return transformer;
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

    private void renameVariables(FlowGraph<ThreeAddressBasicBlock> tacGraph, Set<VariableExpression> definedExpressions) {
        for(VariableExpression expression : definedExpressions) {
            versionStacks.put(expression, new VersionStack(expression));
        }
        search(tacGraph, tacGraph.getEntryPoint());
    }

    private void search(FlowGraph<ThreeAddressBasicBlock> tacGraph, ThreeAddressBasicBlock block) {
        for(VersionStack stack : versionStacks.values()) {
            stack.processingBlock();
        }
        block.processStatements(versionStacks);
        for(ThreeAddressBasicBlock successor : tacGraph.getSuccessors(block)) {
            successor.addPhiFunctionValues(versionStacks);
        }
        for(ThreeAddressBasicBlock child : tacGraph.getDominatorTree().get(block)) {
            search(tacGraph, child);
        }
        for(VersionStack stack : versionStacks.values()) {
            stack.finishedProcessingBlock();
        }
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

    public static Map<VariableExpression, Expression> propagateVariables(FlowGraph<ThreeAddressBasicBlock> tacGraph) {
        Map<VariableExpression, Expression> definitions = new HashMap<>();
        for(ThreeAddressBasicBlock block : tacGraph.getVertices()) {
            for(Statement statement : block.getSSAStatements()) {
                if(statement.definesVariable() && statement instanceof AssignmentStatement) {
                    Expression valueExpr = ((AssignmentStatement) statement).getRightHandSide();
                    if(canPropagate(valueExpr)) {
                        definitions.put(statement.getDefinedVariable(), valueExpr);
                    }
                }
            }
        }
        PropagationTransformer transformer = new PropagationTransformer(definitions);
        boolean changed;
        do {
            changed = false;
            for(VariableExpression assignee : definitions.keySet()) {
                Expression assigned = definitions.get(assignee);
                Expression transformed = assigned.transform(transformer, assignee);
                if(!assigned.equals(transformed)) {
                    changed = true;
                    definitions.put(assignee, transformed);
                }
            }
        } while(changed);
        return definitions;
    }

    public static boolean canPropagate(Expression valueExpr) {
        if(valueExpr instanceof ConstantExpression || valueExpr instanceof ParameterExpression
                || valueExpr instanceof VariableExpression) {
            return true;
        } else if(valueExpr instanceof BinaryExpression) {
            Expression operand1 = ((BinaryExpression) valueExpr).getOperand1();
            Expression operand2 = ((BinaryExpression) valueExpr).getOperand2();
            return canPropagate(operand1) && canPropagate(operand2);
        } else if(valueExpr instanceof UnaryExpression) {
            Expression operand = ((UnaryExpression) valueExpr).getOperand();
            return canPropagate(operand);
        } else {
            return false;
        }
    }
}
