package edu.gmu.swe.phosphor.ignored.control.binding.tracer;

import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph.NaturalLoop;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.gmu.swe.phosphor.ignored.control.FlowGraphUtil;
import edu.gmu.swe.phosphor.ignored.control.binding.FrameConstancyInfo;
import edu.gmu.swe.phosphor.ignored.control.binding.LoopLevel;
import edu.gmu.swe.phosphor.ignored.control.ssa.AnnotatedBasicBlock;
import edu.gmu.swe.phosphor.ignored.control.ssa.AnnotatedInstruction;
import edu.gmu.swe.phosphor.ignored.control.ssa.PropagatingVisitor;
import edu.gmu.swe.phosphor.ignored.control.ssa.SSAMethod;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.*;

import java.util.Iterator;
import java.util.function.Predicate;

import static edu.gmu.swe.phosphor.ignored.control.binding.tracer.PossibleArrayDefinitionPredicate.ARRAY_DEFINITION_PREDICATE;
import static edu.gmu.swe.phosphor.ignored.control.binding.tracer.PossibleFieldDefinitionPredicate.FIELD_DEFINITION_PREDICATE;

public class LoopLevelTracer {

    private final MethodNode methodNode;
    private final SSAMethod method;
    private final FlowGraph<AnnotatedBasicBlock> graph;
    private final Map<AnnotatedBasicBlock, Set<NaturalLoop<AnnotatedBasicBlock>>> containingLoops;
    private final Map<AbstractInsnNode, AnnotatedInstruction> insnMap = new HashMap<>();
    private final Map<AbstractInsnNode, AnnotatedBasicBlock> blockMap = new HashMap<>();
    private final Map<AbstractInsnNode, LoopLevel> loopLevelMap;
    private final Map<VariableExpression, Expression> definitionValues = new HashMap<>();
    private final Map<VariableExpression, AbstractInsnNode> definitionInsnMap = new HashMap<>();
    private final Map<VariableExpression, Set<Statement>> usesMap = new HashMap<>();
    private final Constancy returnDependentConstancy;
    private final ValueConstancies valueConstancies;
    private final PropagatingVisitor propagatingVisitor;


    public LoopLevelTracer(MethodNode methodNode, SSAMethod method) {
        this.methodNode = methodNode;
        this.method = method;
        propagatingVisitor = new PropagatingVisitor(method.getControlFlowGraph());
        valueConstancies = new ValueConstancies(method, propagatingVisitor);
        returnDependentConstancy = new Constancy.ParameterDependent(method.getParameterTypes().size());
        graph = method.getControlFlowGraph();
        containingLoops = FlowGraphUtil.calculateContainingLoops(graph);
        initializeMaps();
        loopLevelMap = Collections.unmodifiableMap(createLoopLevelMap());
    }

    private void initializeMaps() {
        UseGatheringVisitor useGatherer = new UseGatheringVisitor();
        for(AnnotatedBasicBlock block : graph.getVertices()) {
            for(AnnotatedInstruction insn : block.getInstructions()) {
                insnMap.put(insn.getInstruction(), insn);
                blockMap.put(insn.getInstruction(), block);
                for(Statement statement : insn.getStatements()) {
                    if(statement.definesVariable()) {
                        definitionValues.put(statement.getDefinedVariable(),
                                ((AssignmentStatement) statement).getRightHandSide());
                        definitionInsnMap.put(statement.getDefinedVariable(), insn.getInstruction());
                    }
                    for(VariableExpression e : statement.accept(useGatherer)) {
                        if(!usesMap.containsKey(e)) {
                            usesMap.put(e, new HashSet<>());
                        }
                        usesMap.get(e).add(statement);
                    }
                }
            }
        }
    }

    private Map<AbstractInsnNode, LoopLevel> createLoopLevelMap() {
        Map<AbstractInsnNode, LoopLevel> map = new HashMap<>();
        for(AnnotatedBasicBlock block : graph.getVertices()) {
            Set<NaturalLoop<AnnotatedBasicBlock>> candidateLoops = containingLoops.get(block);
            for(AnnotatedInstruction insn : block.getInstructions()) {
                if(insn.getInstruction() != null) {
                    Constancy c = Constancy.CONSTANT;
                    for(Statement statement : insn.getStatements()) {
                        statement = statement.accept(propagatingVisitor);
                        Constancy c2 = Constancy.CONSTANT;
                        if(statement instanceof AssignmentStatement) {
                            c2 = calculateConstancy(insn, (AssignmentStatement) statement, candidateLoops);
                        } else if(statement instanceof InvokeStatement) {
                            c2 = ((InvokeStatement) statement).getExpression().accept(valueConstancies, candidateLoops);
                        } else if(statement instanceof MonitorStatement) {
                            c2 = ((MonitorStatement) statement).getExpression().accept(valueConstancies, candidateLoops);
                        } else if(statement instanceof SwitchStatement) {
                            c2 = ((SwitchStatement) statement).getExpression().accept(valueConstancies, candidateLoops);
                        } else if(statement instanceof IfStatement) {
                            c2 = ((IfStatement) statement).getExpression().accept(valueConstancies, candidateLoops);
                        } else if(statement instanceof ThrowStatement) {
                            c2 = ((ThrowStatement) statement).getExpression().accept(valueConstancies, candidateLoops);
                        } else if(statement instanceof ReturnStatement) {
                            if(!((ReturnStatement) statement).isVoid()) {
                                c2 = ((ReturnStatement) statement).getExpression().accept(valueConstancies, candidateLoops);
                            }
                            // Add a dependency on the return value
                            c = Constancy.merge(c, returnDependentConstancy);
                        }
                        c = Constancy.merge(c, c2);
                    }
                    map.put(insn.getInstruction(), c.toLoopLevel());
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

    private Constancy calculateConstancy(AnnotatedInstruction source, AssignmentStatement statement,
                                         Set<NaturalLoop<AnnotatedBasicBlock>> candidateLoops) {
        Expression lhs = statement.getLeftHandSide();
        Constancy c = getMemoryLocationConstancy(statement.getLeftHandSide(), source, candidateLoops);
        if(lhs instanceof FieldAccess || lhs instanceof ArrayAccess || lhs instanceof LocalVariable) {
            for(Expression subExpression : gatherImpactingSubExpressions(source, statement)) {
                c = Constancy.merge(c, subExpression.accept(valueConstancies, candidateLoops));
            }
        } else {
            c = Constancy.merge(c, statement.getRightHandSide().accept(valueConstancies, candidateLoops));
        }
        return c;
    }

    private Constancy getMemoryLocationConstancy(Expression expression, AnnotatedInstruction source,
                                                 Set<NaturalLoop<AnnotatedBasicBlock>> candidateLoops) {
        Constancy result = Constancy.CONSTANT;
        if(expression instanceof FieldAccess) {
            Expression receiver = ((FieldAccess) expression).getReceiver();
            if(receiver != null) {
                result = Constancy.merge(result, valueConstancies.getConstancy(receiver, source.getBasicBlock()));
            }
        } else if(expression instanceof ArrayAccess) {
            Expression arrayRef = ((ArrayAccess) expression).getArrayRef();
            result = Constancy.merge(result, valueConstancies.getConstancy(arrayRef, source.getBasicBlock()));
            Expression index = ((ArrayAccess) expression).getIndex();
            result = Constancy.merge(result, valueConstancies.getConstancy(index, source.getBasicBlock()));
        } else if(expression instanceof VariableExpression) {
            result = calculateConstancyOfUses((VariableExpression) expression, candidateLoops, false, new HashSet<>());
        } else {
            throw new IllegalArgumentException();
        }
        return result;
    }

    private Set<Expression> gatherImpactingSubExpressions(AnnotatedInstruction insn, AssignmentStatement statement) {
        Set<Expression> subExpressions = new HashSet<>();
        Expression rhs = statement.getRightHandSide();
        Expression excluded = statement.getLeftHandSide();
        if(excluded instanceof LocalVariable) {
            VariableExpression baseExpression = ((VariableExpression) excluded).setVersion(-1);
            excluded = method.getVersionStacks().get(baseExpression).getRedefines((VariableExpression) excluded);
            if(excluded == null) {
                return Collections.singleton(statement.getRightHandSide());
            }
        }
        gatherImpactingSubExpressions(rhs, excluded, subExpressions, insn.getInstruction());
        return subExpressions;
    }

    private void gatherImpactingSubExpressions(Expression e, Expression excluded, Set<Expression> subExpressions,
                                               AbstractInsnNode target) {
        if(e.equals(excluded)) {
            return;
        }
        Expression processed = e.accept(propagatingVisitor, null);
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
            Expression value = definitionValues.get(e);
            AbstractInsnNode source = definitionInsnMap.get(e);
            if(!isExcludedFieldOrArrayAccess(value, excluded, source, target)) {
                gatherImpactingSubExpressions(value, excluded, subExpressions, target);
            }
        } else {
            subExpressions.add(processed);
        }
    }

    private boolean isExcludedFieldOrArrayAccess(Expression value, Expression excluded, AbstractInsnNode source,
                                                 AbstractInsnNode target) {
        Expression processedValue = value.accept(propagatingVisitor, null);
        Expression processedExcluded = excluded.accept(propagatingVisitor, null);
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
        Statement statement = insnMap.get(insn).getStatements().get(0).accept(propagatingVisitor);
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
            Constancy c = expr.getReceiver().accept(valueConstancies, candidateLoops);
            info.pushArgumentLevel(c.toLoopLevel());
        }
        for(Expression arg : expr.getArguments()) {
            Constancy c = arg.accept(valueConstancies, candidateLoops);
            info.pushArgumentLevel(c.toLoopLevel());
        }
        if(statement.definesVariable()) {
            Constancy c = calculateConstancyOfUses(statement.getDefinedVariable(),
                    candidateLoops, true, new HashSet<>());
            info.pushArgumentLevel(c.toLoopLevel());
        }
        return info;
    }

    public Map<AbstractInsnNode, LoopLevel> getLoopLevelMap() {
        return loopLevelMap;
    }


    private Constancy calculateConstancyOfUses(VariableExpression expr, Set<NaturalLoop<AnnotatedBasicBlock>> candidateLoops,
                                               boolean includeMethodUses, Set<VariableExpression> visited) {
        Constancy result = Constancy.CONSTANT;
        if(visited.add(expr) && usesMap.containsKey(expr)) {
            for(Statement s : usesMap.get(expr)) {
                if(s instanceof AssignmentStatement) {
                    AssignmentStatement assign = ((AssignmentStatement) s);
                    Expression lhs = assign.getLeftHandSide();
                    Expression rhs = assign.getRightHandSide();
                    if(rhs instanceof InvokeExpression) {
                        return new Constancy.Nonconstant(candidateLoops);
                    } else if(includeMethodUses && assign.definesVariable() && (rhs instanceof PhiFunction || rhs.equals(expr)
                            || rhs instanceof UnaryExpression && ((UnaryExpression) rhs).getOperation() instanceof CastOperation)) {
                        Constancy useConstancy = calculateConstancyOfUses(assign.getDefinedVariable(),
                                candidateLoops, true, visited);
                        result = Constancy.merge(result, useConstancy);
                    }
                    if(lhs instanceof FieldAccess) {
                        Expression receiver = ((FieldAccess) lhs).getReceiver();
                        if(receiver != null) {
                            result = Constancy.merge(result, receiver.accept(valueConstancies, candidateLoops));
                        }
                    } else if(lhs instanceof ArrayAccess) {
                        Expression arrayRef = ((ArrayAccess) lhs).getArrayRef();
                        result = Constancy.merge(result, arrayRef.accept(valueConstancies, candidateLoops));
                        Expression index = ((ArrayAccess) lhs).getIndex();
                        result = Constancy.merge(result, index.accept(valueConstancies, candidateLoops));
                    }
                } else if(s instanceof ReturnStatement) {
                    result = Constancy.merge(result, new Constancy.ParameterDependent(method.getParameterTypes().size()));
                } else if(s instanceof InvokeStatement && includeMethodUses) {
                    return new Constancy.Nonconstant(candidateLoops);
                }
            }
        }
        return result;
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
}
