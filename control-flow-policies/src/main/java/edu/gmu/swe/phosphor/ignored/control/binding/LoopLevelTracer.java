package edu.gmu.swe.phosphor.ignored.control.binding;

import edu.columbia.cs.psl.phosphor.control.OpcodesUtil;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph.NaturalLoop;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.InvokeDynamicInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.gmu.swe.phosphor.ignored.control.FlowGraphUtil;
import edu.gmu.swe.phosphor.ignored.control.binding.ConstancyLevel.ConstantLevel;
import edu.gmu.swe.phosphor.ignored.control.binding.ConstancyLevel.LoopVariant;
import edu.gmu.swe.phosphor.ignored.control.binding.ConstancyLevel.ParameterDependent;
import edu.gmu.swe.phosphor.ignored.control.ssa.AnnotatedBasicBlock;
import edu.gmu.swe.phosphor.ignored.control.ssa.AnnotatedInstruction;
import edu.gmu.swe.phosphor.ignored.control.ssa.SSAMethod;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.*;

import java.util.Iterator;
import java.util.function.Predicate;

import static edu.gmu.swe.phosphor.ignored.control.binding.LoopLevelTracer.PossibleArrayDefinitionPredicate.ARRAY_DEFINITION_PREDICATE;
import static edu.gmu.swe.phosphor.ignored.control.binding.LoopLevelTracer.PossibleFieldDefinitionPredicate.FIELD_DEFINITION_PREDICATE;

public class LoopLevelTracer {

    private final MethodNode methodNode;
    private final SSAMethod ssaMethod;
    private final FlowGraph<AnnotatedBasicBlock> graph;
    private final Map<VariableExpression, ConstancyLevel> definitionConstancyLevels = new HashMap<>();
    private final Map<AnnotatedBasicBlock, Set<NaturalLoop<AnnotatedBasicBlock>>> containingLoops;
    private final Map<AbstractInsnNode, AnnotatedInstruction> insnMap = new HashMap<>();
    private final Map<AbstractInsnNode, AnnotatedBasicBlock> blockMap = new HashMap<>();
    private final Map<AbstractInsnNode, LoopLevel> loopLevelMap;
    private final Map<VariableExpression, Expression> rawDefinitionValues = new HashMap<>();
    private final Map<VariableExpression, AbstractInsnNode> rawDefinitionInsnMap = new HashMap<>();
    private final Map<VariableExpression, Set<Statement>> rawUsesMap = new HashMap<>();

    public LoopLevelTracer(MethodNode methodNode, SSAMethod ssaMethod) {
        this.methodNode = methodNode;
        this.ssaMethod = ssaMethod;
        graph = ssaMethod.getControlFlowGraph();
        containingLoops = FlowGraphUtil.calculateContainingLoops(graph);
        initializeMaps();
        initializeDefinitionConstancyLevels(graph.getEntryPoint());
        loopLevelMap = Collections.unmodifiableMap(createLoopLevelMap());
    }

    private void initializeMaps() {
        for(AnnotatedBasicBlock block : graph.getVertices()) {
            for(AnnotatedInstruction insn : block.getInstructions()) {
                insnMap.put(insn.getOriginalInstruction(), insn);
                blockMap.put(insn.getOriginalInstruction(), block);
                for(Statement rawStatement : insn.getRawStatements()) {
                    if(rawStatement.definesVariable() && rawStatement instanceof AssignmentStatement) {
                        rawDefinitionValues.put(rawStatement.getDefinedVariable(),
                                ((AssignmentStatement) rawStatement).getRightHandSide());
                        rawDefinitionInsnMap.put(rawStatement.getDefinedVariable(), insn.getOriginalInstruction());
                    }
                    for(VariableExpression e : rawStatement.getUsedVariables()) {
                        if(!rawUsesMap.containsKey(e)) {
                            rawUsesMap.put(e, new HashSet<>());
                        }
                        rawUsesMap.get(e).add(rawStatement);
                    }
                }
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
     * If e' is a new expression, or new array expression then it is non-constant with respect to all loops that
     * contain it. If e' is an array access, field access, phi function, or invoke expression, then we conservatively
     * say that e is non-constant with respect to all loops that contain it.
     */
    private void initializeDefinitionConstancyLevels(AnnotatedBasicBlock block) {
        for(AnnotatedInstruction i : block.getInstructions()) {
            for(Statement s : i.getProcessedStatements()) {
                if(s instanceof AssignmentStatement && s.definesVariable()) {
                    VariableExpression key = s.getDefinedVariable();
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
            if(defConstancy == null) {
                return new LoopVariant(candidateLoops);
            } else if(defConstancy instanceof LoopVariant) {
                return new LoopVariant((LoopVariant) defConstancy, candidateLoops);
            } else {
                return defConstancy;
            }
        } else if(e instanceof BinaryExpression) {
            ConstancyLevel c1 = calculateConstancyLevel(((BinaryExpression) e).getOperand1(), candidateLoops);
            ConstancyLevel c2 = calculateConstancyLevel(((BinaryExpression) e).getOperand2(), candidateLoops);
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
            Set<NaturalLoop<AnnotatedBasicBlock>> candidateLoops = containingLoops.get(block);
            for(AnnotatedInstruction ai : block.getInstructions()) {
                if(ai.getOriginalInstruction() != null) {
                    ConstancyLevel cl = ConstantLevel.CONSTANT_LEVEL;
                    for(Statement processedStatement : ai.getProcessedStatements()) {
                        ConstancyLevel cl2 = ConstantLevel.CONSTANT_LEVEL;
                        if(processedStatement instanceof AssignmentStatement) {
                            cl2 = calculateConstancyLevel(ai, (AssignmentStatement) processedStatement, candidateLoops);
                        } else if(processedStatement instanceof InvokeStatement) {
                            cl2 = calculateConstancyLevel(((InvokeStatement) processedStatement).getExpression(), candidateLoops);
                        } else if(processedStatement instanceof MonitorStatement) {
                            cl2 = calculateConstancyLevel(((MonitorStatement) processedStatement).getOperand(), candidateLoops);
                        } else if(processedStatement instanceof SwitchStatement) {
                            cl2 = calculateConstancyLevel(((SwitchStatement) processedStatement).getValue(), candidateLoops);
                        } else if(processedStatement instanceof IfStatement) {
                            cl2 = calculateConstancyLevel(((IfStatement) processedStatement).getCondition(), candidateLoops);
                        } else if(processedStatement instanceof ThrowStatement) {
                            cl2 = calculateConstancyLevel(((ThrowStatement) processedStatement).getExpression(), candidateLoops);
                        } else if(processedStatement instanceof ReturnStatement) {
                            cl2 = calculateConstancyLevel(((ReturnStatement) processedStatement).getReturnValue(), candidateLoops);
                            // Add a dependency on the return value
                            cl2 = ConstancyLevel.merge(cl2, new ParameterDependent(ssaMethod.getParameterTypes().size()));
                        }
                        cl = ConstancyLevel.merge(cl, cl2);
                    }
                    map.put(ai.getOriginalInstruction(), cl.toLoopLevel());
                }
            }
        }
        Iterator<AbstractInsnNode> itr = methodNode.instructions.iterator();
        while(itr.hasNext()) {
            AbstractInsnNode insn = itr.next();
            if(!map.containsKey(insn)) {
                map.put(insn, LoopLevel.ConstantLoopLevel.CONSTANT_LOOP_LEVEL);
            }
        }
        return map;
    }

    private ConstancyLevel calculateConstancyLevel(AnnotatedInstruction ai, AssignmentStatement s,
                                                   Set<NaturalLoop<AnnotatedBasicBlock>> candidateLoops) {
        Expression lhs = s.getLeftHandSide();
        ConstancyLevel cl = ConstantLevel.CONSTANT_LEVEL;
        if(lhs instanceof FieldAccess) {
            Expression receiver = ((FieldAccess) lhs).getReceiver();
            cl = ConstancyLevel.merge(cl, calculateConstancyLevel(receiver, candidateLoops));
        } else if(lhs instanceof ArrayAccess) {
            Expression arrayRef = ((ArrayAccess) lhs).getArrayRef();
            cl = ConstancyLevel.merge(cl, calculateConstancyLevel(arrayRef, candidateLoops));
            Expression index = ((ArrayAccess) lhs).getIndex();
            cl = ConstancyLevel.merge(cl, calculateConstancyLevel(index, candidateLoops));
        } else if(lhs instanceof VariableExpression) {
            cl = calculateConstancyOfUses((VariableExpression) lhs,
                    candidateLoops, false, new HashSet<>());
        }
        if(lhs instanceof FieldAccess || lhs instanceof ArrayAccess || lhs instanceof LocalVariable) {
            for(Expression subExpression : gatherImpactingSubExpressions(ai, s)) {
                cl = ConstancyLevel.merge(cl, calculateConstancyLevel(subExpression, candidateLoops));
            }
        } else {
            cl = ConstancyLevel.merge(cl, calculateConstancyLevel(s.getRightHandSide(), candidateLoops));
        }
        return cl;
    }

    private Set<Expression> gatherImpactingSubExpressions(AnnotatedInstruction ai, AssignmentStatement s) {
        Set<Expression> subExpressions = new HashSet<>();
        AssignmentStatement rawStatement = (AssignmentStatement) ai.getRawStatement(s);
        Expression rhs = rawStatement.getRightHandSide();
        Expression excluded = rawStatement.getLeftHandSide();
        if(excluded instanceof LocalVariable) {
            VariableExpression baseExpression = ((VariableExpression) excluded).setVersion(-1);
            excluded = ssaMethod.getVersionStacks().get(baseExpression).getRedefines((VariableExpression) excluded);
            if(excluded == null) {
                return Collections.singleton(s.getRightHandSide());
            }
        }
        gatherImpactingSubExpressions(rhs, excluded, subExpressions, ai.getOriginalInstruction());
        return subExpressions;
    }

    private void gatherImpactingSubExpressions(Expression e, Expression excluded, Set<Expression> subExpressions,
                                               AbstractInsnNode target) {
        if(e.equals(excluded)) {
            return;
        }
        Expression processed = e.transform(ssaMethod.getTransformer());
        if(processed instanceof ConstantExpression) {
            subExpressions.add(processed);
        } else if(e instanceof ParameterExpression) {
            subExpressions.add(e);
        } else if(e instanceof BinaryExpression) {
            gatherImpactingSubExpressions(((BinaryExpression) e).getOperand1(), excluded, subExpressions, target);
            gatherImpactingSubExpressions(((BinaryExpression) e).getOperand2(), excluded, subExpressions, target);
        } else if(e instanceof UnaryExpression) {
            gatherImpactingSubExpressions(((UnaryExpression) e).getOperand(), excluded, subExpressions, target);
        } else if(e instanceof VariableExpression) {
            Expression value = rawDefinitionValues.get(e);
            AbstractInsnNode source = rawDefinitionInsnMap.get(e);
            if(!isExcludedFieldOrArrayAccess(value, excluded, source, target)) {
                gatherImpactingSubExpressions(value, excluded, subExpressions, target);
            }
        } else {
            subExpressions.add(processed);
        }
    }

    private boolean isExcludedFieldOrArrayAccess(Expression value, Expression excluded, AbstractInsnNode source,
                                                 AbstractInsnNode target) {
        Expression processedValue = value.transform(ssaMethod.getTransformer());
        Expression processedExcluded = excluded.transform(ssaMethod.getTransformer());
        if(processedValue.equals(processedExcluded)) {
            return (processedValue instanceof ArrayAccess && processedExcluded instanceof ArrayAccess
                    && !checkAllPaths(source, target, ARRAY_DEFINITION_PREDICATE))
                    || (processedValue instanceof FieldAccess && processedExcluded instanceof FieldAccess
                    && !checkAllPaths(source, target, FIELD_DEFINITION_PREDICATE));
        }
        return false;
    }

    public FrameConstancyInfo generateMethodConstancyInfo(AbstractInsnNode insn) {
        Set<NaturalLoop<AnnotatedBasicBlock>> candidateLoops = containingLoops.get(blockMap.get(insn));
        if(candidateLoops == null) {
            candidateLoops = Collections.emptySet();
        }
        int invocationLevel = candidateLoops.size();
        if(!insnMap.containsKey(insn)) {
            return null;
        }
        Statement statement = insnMap.get(insn).getProcessedStatements().get(0);
        InvokeExpression expr;
        if(statement instanceof AssignmentStatement) {
            expr = (InvokeExpression) ((AssignmentStatement) statement).getRightHandSide();
        } else if(statement instanceof InvokeStatement) {
            expr = ((InvokeStatement) statement).getExpression();
        } else {
            throw new IllegalArgumentException();
        }
        FrameConstancyInfo info = new FrameConstancyInfo(invocationLevel);
        if(expr.getReceiver() != null) {
            ConstancyLevel cl = calculateConstancyLevel(expr.getReceiver(), candidateLoops);
            info.pushArgumentLevel(cl.toLoopLevel());
        }
        for(Expression arg : expr.getArguments()) {
            ConstancyLevel cl = calculateConstancyLevel(arg, candidateLoops);
            info.pushArgumentLevel(cl.toLoopLevel());
        }
        if(statement.definesVariable()) {
            ConstancyLevel cl = calculateConstancyOfUses(statement.getDefinedVariable(),
                    candidateLoops, true, new HashSet<>());
            info.pushArgumentLevel(cl.toLoopLevel());
        }
        return info;
    }

    public Map<AbstractInsnNode, LoopLevel> getLoopLevelMap() {
        return loopLevelMap;
    }


    private ConstancyLevel calculateConstancyOfUses(VariableExpression expr, Set<NaturalLoop<AnnotatedBasicBlock>> candidateLoops,
                                                    boolean includeMethodUses, Set<VariableExpression> visited) {
        ConstancyLevel cl = ConstantLevel.CONSTANT_LEVEL;
        if(visited.add(expr) && rawUsesMap.containsKey(expr)) {
            for(Statement s : rawUsesMap.get(expr)) {
                if(s instanceof AssignmentStatement) {
                    AssignmentStatement assign = ((AssignmentStatement) s);
                    Expression lhs = assign.getLeftHandSide();
                    Expression rhs = assign.getRightHandSide();
                    if(rhs instanceof InvokeExpression) {
                        return new LoopVariant(candidateLoops);
                    } else if(includeMethodUses && assign.definesVariable() && (rhs instanceof PhiFunction || rhs.equals(expr)
                            || rhs instanceof UnaryExpression && ((UnaryExpression) rhs).getOperation() instanceof CastOperation)) {
                        ConstancyLevel level = calculateConstancyOfUses(assign.getDefinedVariable(),
                                candidateLoops, true, visited);
                        cl = ConstancyLevel.merge(cl, level);
                    }
                    if(lhs instanceof FieldAccess) {
                        Expression receiver = ((FieldAccess) lhs).getReceiver();
                        cl = ConstancyLevel.merge(cl, calculateConstancyLevel(receiver, candidateLoops));
                    } else if(lhs instanceof ArrayAccess) {
                        Expression arrayRef = ((ArrayAccess) lhs).getArrayRef();
                        cl = ConstancyLevel.merge(cl, calculateConstancyLevel(arrayRef, candidateLoops));
                        Expression index = ((ArrayAccess) lhs).getIndex();
                        cl = ConstancyLevel.merge(cl, calculateConstancyLevel(index, candidateLoops));
                    }
                } else if(s instanceof ReturnStatement) {
                    cl = ConstancyLevel.merge(cl, new ParameterDependent(ssaMethod.getParameterTypes().size()));
                } else if(s instanceof InvokeStatement && includeMethodUses) {
                    return new LoopVariant(candidateLoops);
                }
            }
        }
        return cl;
    }

    /**
     * Returns true if there exists some execution path from the specified source instruction to the specified target
     * insn that contains an instruction (excluding the source and target instructions) for which the specified
     * predicate returns true.
     */
    private boolean checkAllPaths(AbstractInsnNode source, AbstractInsnNode target, Predicate<AbstractInsnNode> predicate) {
        AnnotatedBasicBlock sourceBlock = blockMap.get(source);
        AnnotatedBasicBlock targetBlock = blockMap.get(target);
        if(graph.getVertices().size() > 10 && sourceBlock != targetBlock) {
            // Conservatively return true for large graphs when the source block is not the target block.
            return true;
        }
        Set<List<AnnotatedBasicBlock>> simplePaths = FlowGraphUtil.getAllSimplePaths(graph, sourceBlock, targetBlock);
        for(List<AnnotatedBasicBlock> simplePath : simplePaths) {
            for(AnnotatedBasicBlock block : simplePath) {
                AbstractInsnNode insn = block.getFirstInsn();
                if(block == sourceBlock) {
                    insn = source.getNext();
                }
                AbstractInsnNode end = block.getLastInsn();
                while(insn != null && insn != target) {
                    if(predicate.test(insn)) {
                        return true;
                    }
                    if(insn == end) {
                        break;
                    }
                    insn = insn.getNext();
                }
            }
        }
        return false;
    }

    public enum PossibleFieldDefinitionPredicate implements Predicate<AbstractInsnNode> {
        FIELD_DEFINITION_PREDICATE;

        @Override
        public boolean test(AbstractInsnNode insn) {
            return insn instanceof InvokeDynamicInsnNode
                    || insn instanceof MethodInsnNode || OpcodesUtil.isFieldStoreInsn(insn.getOpcode());
        }
    }

    public enum PossibleArrayDefinitionPredicate implements Predicate<AbstractInsnNode> {
        ARRAY_DEFINITION_PREDICATE;

        @Override
        public boolean test(AbstractInsnNode insn) {
            return insn instanceof InvokeDynamicInsnNode
                    || insn instanceof MethodInsnNode || OpcodesUtil.isArrayStore(insn.getOpcode());
        }
    }
}
