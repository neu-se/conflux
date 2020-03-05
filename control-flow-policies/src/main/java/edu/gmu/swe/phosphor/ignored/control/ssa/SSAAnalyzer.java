package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.control.graph.BasicBlock;
import edu.columbia.cs.psl.phosphor.control.graph.EntryPoint;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
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
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VersionedExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.AssignmentStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

import java.util.Iterator;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * Uses algorithms from the following for placing phi functions and renaming variables:
 * <p>Ron Cytron, Jeanne Ferrante, Barry K. Rosen, Mark N. Wegman, and F. Kenneth Zadeck.
 * 1991. Efficiently computing static single assignment form and the control dependence graph.
 * ACM Trans. Program. Lang. Syst. 13, 4 (October 1991), 451–490. DOI: https://doi.org/10.1145/115372.115320
 */
public class SSAAnalyzer {

    private final FlowGraph<BasicBlock> cfg;
    private final LinkedHashMap<AbstractInsnNode, AnnotatedInstruction> annotationMap;
    private final Map<VersionedExpression, Set<AnnotatedBasicBlock>> persistentVariableDefinitions = new HashMap<>();
    private final List<LocalVariable> parameterDefinitions = new LinkedList<>();

    public SSAAnalyzer(String owner, MethodNode methodNode) throws AnalyzerException {
        this.annotationMap = annotateInstructions(owner, methodNode);
        cfg = new SSAControlFlowGraphCreator(methodNode, annotationMap).createControlFlowGraph();
        locatePersistentVariableDefinitions();
        placePhiFunctions();
        renameVariables(computeOriginalParameterDefinitions(methodNode));
    }

    public LinkedHashMap<AbstractInsnNode, AnnotatedInstruction> getAnnotationMap() {
        return annotationMap;
    }

    public List<LocalVariable> getParameterDefinitions() {
        return parameterDefinitions;
    }

    List<Statement> createStatementList() {
        List<Statement> flattenedList = new LinkedList<>();
        for(AnnotatedInstruction annotation : annotationMap.values()) {
            for(Statement s : annotation.getStatements()) {
                flattenedList.add(s);
            }
        }
        return flattenedList;
    }

    List<Statement> createProcessedStatementList() {
        List<Statement> flattenedList = new LinkedList<>();
        for(AnnotatedInstruction annotation : annotationMap.values()) {
            for(Statement s : annotation.getProcessedStatements()) {
                flattenedList.add(s);
            }
        }
        return flattenedList;
    }

    private void placePhiFunctions() {
        Map<BasicBlock, Set<BasicBlock>> dominanceFrontiers = cfg.getDominanceFrontiers();
        for(VersionedExpression expr : persistentVariableDefinitions.keySet()) {
            Set<AnnotatedBasicBlock> definingBlocks = persistentVariableDefinitions.get(expr);
            placePhiFunctions(definingBlocks, expr, dominanceFrontiers);
        }
    }

    private void renameVariables(List<LocalVariable> originalParamDefinitions) {
        Map<BasicBlock, Set<BasicBlock>> dominatorTree = cfg.getDominatorTree();
        Map<VersionedExpression, VersionStack> versionStacks = new HashMap<>();
        Set<VersionedExpression> definedVariables = collectDefinedVariables(annotationMap.values());
        definedVariables.addAll(originalParamDefinitions);
        for(VersionedExpression expression : definedVariables) {
            versionStacks.put(expression, new VersionStack(expression));
        }
        search(cfg.getEntryPoint(), versionStacks, dominatorTree, originalParamDefinitions);
    }

    private void search(BasicBlock block, Map<VersionedExpression, VersionStack> versionStacks,
                        Map<BasicBlock, Set<BasicBlock>> dominatorTree, List<LocalVariable> originalParamDefinitions) {
        for(VersionStack stack : versionStacks.values()) {
            stack.processingBlock();
        }
        if(block instanceof AnnotatedBasicBlock) {
            ((AnnotatedBasicBlock) block).processStatements(versionStacks);
        } else if(block instanceof EntryPoint) {
            for(LocalVariable originalParameterDefinition : originalParamDefinitions) {
                parameterDefinitions.add((LocalVariable) versionStacks.get(originalParameterDefinition).createNewVersion());
            }
        }
        for(BasicBlock successor : cfg.getSuccessors(block)) {
            if(successor instanceof AnnotatedBasicBlock) {
                ((AnnotatedBasicBlock) successor).addPossiblePhiFunctionValues(versionStacks);
            }
        }
        for(BasicBlock child : dominatorTree.get(block)) {
            search(child, versionStacks, dominatorTree, originalParamDefinitions);
        }
        for(VersionStack stack : versionStacks.values()) {
            stack.finishedProcessingBlock();
        }
    }

    private void locatePersistentVariableDefinitions() {
        for(AnnotatedInstruction annotatedInstruction : annotationMap.values()) {
            AnnotatedBasicBlock block = annotatedInstruction.getBasicBlock();
            for(Statement s : annotatedInstruction.getStatements()) {
                if(s instanceof AssignmentStatement) {
                    Expression expr = ((AssignmentStatement) s).getLeftHandSide();
                    if(isPersistentDefinition(block, expr)) {
                        if(!persistentVariableDefinitions.containsKey(expr)) {
                            persistentVariableDefinitions.put((VersionedExpression) expr, new HashSet<>());
                        }
                        persistentVariableDefinitions.get(expr).add(block);
                    }
                }
            }
        }
    }

    private boolean isPersistentDefinition(AnnotatedBasicBlock block, Expression expr) {
        if(expr instanceof LocalVariable || expr instanceof StackElement) {
            for(BasicBlock successor : cfg.getSuccessors(block)) {
                if(isDefinedAtStartOfBlock(successor, (VersionedExpression) expr)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isDefinedAtStartOfBlock(BasicBlock block, VersionedExpression expr) {
        if(!(block instanceof AnnotatedBasicBlock)) {
            return false;
        }
        int index = expr instanceof LocalVariable ? ((LocalVariable) expr).getIndex() : ((StackElement) expr).getIndex();
        Frame<TypeValue> succeedingFrame = ((AnnotatedBasicBlock) block).getFirstFrame();
        if(succeedingFrame != null) {
            if(expr instanceof LocalVariable) {
                return index < succeedingFrame.getLocals()
                        && succeedingFrame.getLocal(index) != TypeValue.UNINITIALIZED_VALUE;
            } else {
                return index < succeedingFrame.getStackSize()
                        && succeedingFrame.getStack(index) != TypeValue.UNINITIALIZED_VALUE;
            }
        }
        return false;
    }

    private void placePhiFunctions(Set<AnnotatedBasicBlock> definingBlocks, VersionedExpression expr,
                                   Map<BasicBlock, Set<BasicBlock>> dominanceFrontiers) {
        LinkedList<AnnotatedBasicBlock> workingSet = new LinkedList<>(definingBlocks);
        Set<AnnotatedBasicBlock> visited = new HashSet<>(workingSet);
        while(!workingSet.isEmpty()) {
            AnnotatedBasicBlock x = workingSet.poll();
            for(BasicBlock y : dominanceFrontiers.get(x)) {
                if(y instanceof AnnotatedBasicBlock) {
                    AnnotatedBasicBlock a = (AnnotatedBasicBlock) y;
                    if(isDefinedAtStartOfBlock(a, expr)) {
                        a.addPhiFunctionForExpression(expr);
                    }
                    if(visited.add(a)) {
                        workingSet.add(a);
                    }
                }
            }
        }
    }

    private static List<LocalVariable> computeOriginalParameterDefinitions(MethodNode method) {
        List<LocalVariable> initiallyDefinedLocals = new LinkedList<>();
        int currentLocal = 0;
        boolean isInstanceMethod = (method.access & ACC_STATIC) == 0;
        if(isInstanceMethod) {
            initiallyDefinedLocals.add(new LocalVariable(currentLocal));
            currentLocal++;
        }
        Type[] argumentTypes = Type.getArgumentTypes(method.desc);
        for(Type argumentType : argumentTypes) {
            initiallyDefinedLocals.add(new LocalVariable(currentLocal));
            currentLocal += argumentType.getSize();
        }
        return initiallyDefinedLocals;
    }

    /**
     * @param annotatedInstructions instructions to be checked for local variable and stack element definitions
     * @return a set containing the local variables and stack elements that are assigned a value in at least one
     * statement in the specified array.
     */
    private static Set<VersionedExpression> collectDefinedVariables(Iterable<AnnotatedInstruction> annotatedInstructions) {
        Set<VersionedExpression> definedVariables = new HashSet<>();
        for(AnnotatedInstruction annotatedInstruction : annotatedInstructions) {
            for(Statement statement : annotatedInstruction.getStatements()) {
                if(statement instanceof AssignmentStatement) {
                    Expression expr = ((AssignmentStatement) statement).getLeftHandSide();
                    if(expr instanceof VersionedExpression) {
                        definedVariables.add((VersionedExpression) expr);
                    }
                }
            }
        }
        return definedVariables;
    }

    private static LinkedHashMap<AbstractInsnNode, AnnotatedInstruction> annotateInstructions(String owner, MethodNode methodNode)
            throws AnalyzerException {
        LinkedHashMap<AbstractInsnNode, AnnotatedInstruction> annotationMap = new LinkedHashMap<>();
        TypeInterpreter interpreter = new TypeInterpreter(owner, methodNode);
        Frame<TypeValue>[] frames = new PhosphorOpcodeIgnoringAnalyzer<>(interpreter).analyze(owner, methodNode);
        Iterator<AbstractInsnNode> itr = methodNode.instructions.iterator();
        for(int i = 0; itr.hasNext(); i++) {
            AbstractInsnNode instruction = itr.next();
            AnnotatedInstruction annotatedInstruction = new AnnotatedInstruction(instruction, frames[i]);
            annotationMap.put(instruction, annotatedInstruction);
        }
        return annotationMap;
    }
}
