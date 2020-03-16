package edu.gmu.swe.phosphor.ignored.control.binding;

import edu.columbia.cs.psl.phosphor.control.OpcodesUtil;
import edu.columbia.cs.psl.phosphor.control.graph.BasicBlock;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph.NaturalLoop;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.InvokeDynamicInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.gmu.swe.phosphor.ignored.control.FlowGraphUtil;
import edu.gmu.swe.phosphor.ignored.control.binding.ConstancyLevel.ConstantLevel;
import edu.gmu.swe.phosphor.ignored.control.binding.ConstancyLevel.LoopVariant;
import edu.gmu.swe.phosphor.ignored.control.binding.ConstancyLevel.ParameterDependent;
import edu.gmu.swe.phosphor.ignored.control.ssa.AnnotatedBasicBlock;
import edu.gmu.swe.phosphor.ignored.control.ssa.AnnotatedInstruction;
import edu.gmu.swe.phosphor.ignored.control.ssa.SSAMethod;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.AssignmentStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.InvokeStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

import java.util.function.Predicate;

public class LoopLevelTracer {

    private final SSAMethod ssaMethod;
    private final FlowGraph<AnnotatedBasicBlock> graph;
    private final Map<VariableExpression, ConstancyLevel> definitionConstancyLevels = new HashMap<>();
    private final Map<AnnotatedBasicBlock, Set<NaturalLoop<AnnotatedBasicBlock>>> containingLoops;
    private final Map<AbstractInsnNode, AnnotatedInstruction> insnMap = new HashMap<>();
    private final Map<AbstractInsnNode, AnnotatedBasicBlock> blockMap = new HashMap<>();
    private final Map<AbstractInsnNode, LoopLevel> loopLevelMap;
    private final Map<VariableExpression, AbstractInsnNode> definitions = new HashMap<>();
    private final Map<VariableExpression, Expression> rawDefinitions = new HashMap<>();

    public LoopLevelTracer(String owner, MethodNode methodNode) throws AnalyzerException {
        ssaMethod = new SSAMethod(owner, methodNode);
        graph = ssaMethod.getControlFlowGraph();
        containingLoops = FlowGraphUtil.calculateContainingLoops(graph);
        initializeInsnAndBlockMaps();
        initializeDefinitionConstancyLevels(graph.getEntryPoint());
        for(AnnotatedBasicBlock block : graph.getVertices()) {
            for(AnnotatedInstruction insn : block.getInstructions()) {
                for(Statement rawStatement : insn.getRawStatements()) {
                    if(rawStatement.definesVariable() && rawStatement instanceof AssignmentStatement) {
                        rawDefinitions.put(rawStatement.getDefinedVariable(),
                                ((AssignmentStatement) rawStatement).getRightHandSide());
                    }
                }
            }
        }
        loopLevelMap = Collections.unmodifiableMap(createLoopLevelMap());
    }

    private void initializeInsnAndBlockMaps() {
        for(AnnotatedBasicBlock block : graph.getVertices()) {
            for(AnnotatedInstruction ai : block.getInstructions()) {
                insnMap.put(ai.getOriginalInstruction(), ai);
                blockMap.put(ai.getOriginalInstruction(), block);
            }
        }
    }

    /**
     * Calculates the loop-relative constancy of each variable (local variable or stack element) definition.
     * <p>
     * Let s be an assignment statement for some representation in static single assignment form which assigns a
     * variable, x_v, the value of an expression, e.
     * The value of x_v is constant relative to a natural loop, L, that contains the statement s, if along all
     * paths from the header of L consisting only of vertices contained in L, x_v is either undefined or
     * its definition is always equal to the same value.
     * <p>
     * The expression e is comprised of a combination of subexpressions and operations. The expression is said to be
     * non-constant with respect to L if at least one of its subexpressions is non-constant with respect to L.
     * A subexpression e' is constant with respect to L if one of the following conditions is met:
     * <ul>
     *     <li>e' is a constant</li>
     *     <li>e' is a parameter expression whose definition is constant with respect to L</li>
     *     <li>e' is a variable expression whose definition is constant with respect to L</li>
     * </ul>
     * If e' is a new expression, new array expression, or a phi function, then it can vary with respect to all loops that
     * contain it. If e' is an array access, field access, or invoke expression, then we conservatively say that e is
     * non-constant with respect to all loops that contain it.
     */
    private void initializeDefinitionConstancyLevels(AnnotatedBasicBlock block) {
        for(AnnotatedInstruction i : block.getInstructions()) {
            for(Statement s : i.getProcessedStatements()) {
                if(s instanceof AssignmentStatement && s.definesVariable()) {
                    VariableExpression key = s.getDefinedVariable();
                    definitions.put(key, i.getOriginalInstruction());
                    Expression e = ((AssignmentStatement) s).getRightHandSide();
                    Set<NaturalLoop<AnnotatedBasicBlock>> candidateLoops = containingLoops.get(block);
                    definitionConstancyLevels.put(key, calculateConstancyLevel(e, candidateLoops));
                }
            }
        }
        for(AnnotatedBasicBlock child : graph.getDominatorTree().get(block)) {
            initializeDefinitionConstancyLevels(child);
        }
    }

    private ConstancyLevel calculateConstancyLevel(Expression e, Set<NaturalLoop<AnnotatedBasicBlock>> candidateLoops) {
        if(e instanceof ConstantExpression) {
            return ConstantLevel.CONSTANT_LEVEL;
        } else if(e instanceof ParameterExpression) {
            return new ParameterDependent((ParameterExpression) e);
        } else if(e instanceof VariableExpression) {
            ConstancyLevel defConstancy = definitionConstancyLevels.get(e);
            if(defConstancy instanceof LoopVariant) {
                return new LoopVariant((LoopVariant) defConstancy, candidateLoops);
            } else {
                return defConstancy;
            }
        } else if(e instanceof BinaryExpression) {
            ConstancyLevel c1 = calculateConstancyLevel(((BinaryExpression) e).getOperand1(), candidateLoops);
            ConstancyLevel c2 = calculateConstancyLevel(((BinaryExpression) e).getOperand1(), candidateLoops);
            return ConstancyLevel.merge(c1, c2);
        } else if(e instanceof UnaryExpression) {
            return calculateConstancyLevel(((UnaryExpression) e).getOperand(), candidateLoops);
        } else {
            return new LoopVariant(candidateLoops);
        }
    }

    private Map<AbstractInsnNode, LoopLevel> createLoopLevelMap() {
        Map<AbstractInsnNode, LoopLevel> map = new HashMap<>();
        for(AnnotatedBasicBlock block : graph.getVertices()) {
            for(AnnotatedInstruction ai : block.getInstructions()) {
                if(ai.getOriginalInstruction() != null) {
                    ConstancyLevel cl = ConstantLevel.CONSTANT_LEVEL;
                    for(Statement processedStatement : ai.getProcessedStatements()) {
                        if(processedStatement instanceof AssignmentStatement) {
                            ConstancyLevel cl2 = calculateConstancyLevel(ai, (AssignmentStatement) processedStatement);
                            cl = ConstancyLevel.merge(cl, cl2);
                        }
                    }
                    map.put(ai.getOriginalInstruction(), cl.toLoopLevel());
                }

            }
        }
        return map;
    }

    private ConstancyLevel calculateConstancyLevel(AnnotatedInstruction ai, AssignmentStatement s) {
        Expression lhs = s.getLeftHandSide();
        ConstancyLevel cl = ConstantLevel.CONSTANT_LEVEL;
        AnnotatedBasicBlock block = blockMap.get(ai.getOriginalInstruction());
        Set<NaturalLoop<AnnotatedBasicBlock>> candidateLoops = containingLoops.get(block);
        if(lhs instanceof FieldAccess) {
            Expression receiver = ((FieldAccess) lhs).getReceiver();
            cl = ConstancyLevel.merge(cl, calculateConstancyLevel(receiver, candidateLoops));
        } else if(lhs instanceof ArrayAccess) {
            Expression arrayRef = ((ArrayAccess) lhs).getArrayRef();
            cl = ConstancyLevel.merge(cl, calculateConstancyLevel(arrayRef, candidateLoops));
            Expression index = ((ArrayAccess) lhs).getIndex();
            cl = ConstancyLevel.merge(cl, calculateConstancyLevel(index, candidateLoops));
        }
        // TODO Check VariableExpressions lhs usages
        for(Expression subExpression : gatherImpactingSubExpressions(ai, s)) {
            // TODO here candidate loops should be defined wrt to the rhs
            cl = ConstancyLevel.merge(cl, calculateConstancyLevel(subExpression, candidateLoops));
        }
        return cl;
    }

    private Set<Expression> gatherImpactingSubExpressions(AnnotatedInstruction ai, AssignmentStatement s) {
        Set<Expression> subExpressions = new HashSet<>();
        AssignmentStatement rawStatement = (AssignmentStatement) ai.getRawStatement(s);
        gatherImpactingSubExpressions(rawStatement.getRightHandSide(), rawStatement.getLeftHandSide(), subExpressions);
        return subExpressions;
    }

    private void gatherImpactingSubExpressions(Expression e, Expression excluded, Set<Expression> subExpressions) {
        if(directMatch(e, excluded)) {
            return;
        }
        Expression processed = e.transform(ssaMethod.getTransformer());
        if(processed instanceof ConstantExpression) {
            subExpressions.add(processed);
        } else if(e instanceof ParameterExpression) {
            subExpressions.add(e);
        } else if(e instanceof BinaryExpression) {
            gatherImpactingSubExpressions(((BinaryExpression) e).getOperand1(), excluded, subExpressions);
            gatherImpactingSubExpressions(((BinaryExpression) e).getOperand2(), excluded, subExpressions);
        } else if(e instanceof UnaryExpression) {
            gatherImpactingSubExpressions(((UnaryExpression) e).getOperand(), excluded, subExpressions);
        } else if(e instanceof ArrayAccess && excluded instanceof ArrayAccess &&
                e.transform(ssaMethod.getTransformer()).equals(excluded.transform(ssaMethod.getTransformer()))) {
            // TODO CHECK for possible intervening redefinition
            return;
        } else if(e instanceof FieldAccess && excluded instanceof FieldAccess &&
                e.transform(ssaMethod.getTransformer()).equals(excluded.transform(ssaMethod.getTransformer()))) {
            // TODO CHECK for possible intervening redefinition
            return;
        } else if(e instanceof VariableExpression) {
            gatherImpactingSubExpressions(rawDefinitions.get(e), excluded, subExpressions);
        } else {
            subExpressions.add(processed);
        }
    }

    private boolean directMatch(Expression e, Expression target) {
        if(target instanceof LocalVariable) {
            int index = ((LocalVariable) target).getIndex();
            if(e instanceof LocalVariable && ((LocalVariable) e).getIndex() == index) {
                return true;
            }
            Expression processed = e.transform(ssaMethod.getTransformer());
            if(processed instanceof LocalVariable && ((LocalVariable) processed).getIndex() == index) {
                return true;
            }
        }
        return e.equals(target);
    }

    public FrameConstancyInfo generateMethodConstancyInfo(AbstractInsnNode insn) {
        int invocationLevel = containingLoops.get(insn).size();
        Statement statement = insnMap.get(insn).getProcessedStatements().get(0);
        InvokeExpression expr;
        if(statement instanceof AssignmentStatement) {
            expr = (InvokeExpression) ((AssignmentStatement) statement).getRightHandSide();
            // TODO check uses of assigned expression determine the return value's constancy level
        } else if(statement instanceof InvokeStatement) {
            expr = ((InvokeStatement) statement).getExpression();
        } else {
            throw new IllegalArgumentException();
        }
        FrameConstancyInfo info = new FrameConstancyInfo(invocationLevel);
        if(expr.getReceiver() != null) {
            ConstancyLevel cl = calculateConstancyLevel(expr.getReceiver(), containingLoops.get(insn));
            info.pushArgumentLevel(cl.toLoopLevel());
        }
        for(Expression arg : expr.getArguments()) {
            ConstancyLevel cl = calculateConstancyLevel(arg, containingLoops.get(insn));
            info.pushArgumentLevel(cl.toLoopLevel());
        }
        return info;
    }

    public Map<AbstractInsnNode, LoopLevel> getLoopLevelMap() {
        return loopLevelMap;
    }

    /**
     * Returns true if there exists some execution path from the specified source instruction to the specified target
     * insn that contains one of the following (excluding the source and target instructions):
     * <ul>
     *     <li>an InvokeDynamicInsnNode</li>
     *     <li>a MethodInsnNode</li>
     *     <li>an instruction that stores a value to a field</li>
     * </ul>
     */
    public static <T extends BasicBlock> boolean interveningFieldRedefinitionPossible(AbstractInsnNode source,
                                                                                      AbstractInsnNode target, FlowGraph<T> graph,
                                                                                      Map<AbstractInsnNode, T> insnBlockMap) {
        return checkAllPaths(source, target, graph, insnBlockMap, insn ->
                insn instanceof InvokeDynamicInsnNode || insn instanceof MethodInsnNode || OpcodesUtil.isFieldStoreInsn(insn.getOpcode()));
    }

    /**
     * Returns true if there exists some execution path from the specified source instruction to the specified target
     * insn that contains one of the following (excluding the source and target instructions):
     * <ul>
     *     <li>an InvokeDynamicInsnNode</li>
     *     <li>a MethodInsnNode</li>
     *     <li>an instruction that stores a value to an array</li>
     * </ul>
     */
    public static <T extends BasicBlock> boolean interveningArrayRedefinitionPossible(AbstractInsnNode source, AbstractInsnNode target,
                                                                                      FlowGraph<T> graph,
                                                                                      Map<AbstractInsnNode, T> insnBlockMap) {
        return checkAllPaths(source, target, graph, insnBlockMap, insn ->
                insn instanceof InvokeDynamicInsnNode || insn instanceof MethodInsnNode || OpcodesUtil.isArrayStore(insn.getOpcode()));
    }

    private static <T extends BasicBlock> boolean checkAllPaths(AbstractInsnNode source, AbstractInsnNode target,
                                                                FlowGraph<T> graph,
                                                                Map<AbstractInsnNode, T> insnBlockMap, Predicate<AbstractInsnNode> predicate) {
        T sourceBlock = insnBlockMap.get(source);
        T targetBlock = insnBlockMap.get(target);
        Set<List<T>> simplePaths = FlowGraphUtil.getAllSimplePaths(graph, sourceBlock, targetBlock);
        for(List<T> simplePath : simplePaths) {
            for(T block : simplePath) {
                AbstractInsnNode insn = block.getFirstInsn();
                if(block == sourceBlock) {
                    insn = source.getNext();
                }
                AbstractInsnNode end = block.getLastInsn();
                if(block == targetBlock) {
                    end = target;
                }
                while(insn != null && insn != end) {
                    if(predicate.test(insn)) {
                        return true;
                    }
                    insn = insn.getNext();
                }
            }
        }
        return false;
    }
}
