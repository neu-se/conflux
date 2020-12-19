package edu.neu.ccs.conflux.internal.policy.binding.tracer;

import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph.NaturalLoop;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.neu.ccs.conflux.internal.policy.FlowGraphUtil;
import edu.neu.ccs.conflux.internal.policy.ssa.AnnotatedBasicBlock;
import edu.neu.ccs.conflux.internal.policy.ssa.AnnotatedInstruction;
import edu.neu.ccs.conflux.internal.policy.ssa.PropagatingVisitor;
import edu.neu.ccs.conflux.internal.policy.ssa.SSAMethod;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.*;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.*;
import edu.neu.ccs.conflux.internal.policy.binding.FrameLoopStabilityInfo;
import edu.neu.ccs.conflux.internal.policy.binding.LoopLevel;
import java.util.function.Predicate;

import static edu.neu.ccs.conflux.internal.policy.binding.LoopLevel.StableLoopLevel.STABLE_LOOP_LEVEL;

public class LoopLevelTracer {

    private final SSAMethod method;
    private final FlowGraph<AnnotatedBasicBlock> graph;
    private final Map<AnnotatedBasicBlock, Set<NaturalLoop<AnnotatedBasicBlock>>> containingLoops;
    private final Map<AbstractInsnNode, AnnotatedInstruction> insnMap = new HashMap<>();
    private final Map<AbstractInsnNode, AnnotatedBasicBlock> blockMap = new HashMap<>();
    private final Map<VariableExpression, Expression> definitionValues = new HashMap<>();
    private final Map<VariableExpression, AnnotatedInstruction> definitionInsnMap = new HashMap<>();
    private final Map<VariableExpression, Set<Statement>> usesMap = new HashMap<>();
    private final LoopStability returnDependentLoopStability;
    private final ValueStabilities valueStabilities;
    private final PropagatingVisitor propagatingVisitor;

    public LoopLevelTracer(SSAMethod method) {
        this.method = method;
        propagatingVisitor = new PropagatingVisitor(method.getControlFlowGraph(), false);
        valueStabilities = new ValueStabilities(method, propagatingVisitor);
        returnDependentLoopStability = new LoopStability.ParameterDependent(method.getParameterTypes().size());
        graph = method.getControlFlowGraph();
        containingLoops = FlowGraphUtil.calculateContainingLoops(graph);
        initializeMaps();
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
                        definitionInsnMap.put(statement.getDefinedVariable(), insn);
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

    private LoopStability calculateStability(AnnotatedInstruction source, AssignmentStatement statement) {
        statement = (AssignmentStatement) statement.accept(propagatingVisitor);
        Expression lhs = statement.getLeftHandSide();
        Set<NaturalLoop<AnnotatedBasicBlock>> candidateLoops = containingLoops.get(source.getBasicBlock());
        LoopStability c = getMemoryLocationStability(statement.getLeftHandSide(), source, candidateLoops);
        if(lhs instanceof FieldAccess || lhs instanceof ArrayAccess || lhs instanceof VariableExpression) {
            for(Expression subExpression : gatherImpactingSubExpressions(source, statement)) {
                c = LoopStability.merge(c, subExpression.accept(valueStabilities, candidateLoops));
            }
        } else {
            throw new IllegalArgumentException();
        }
        return c;
    }

    private LoopStability getMemoryLocationStability(Expression expression, AnnotatedInstruction source,
                                                     Set<NaturalLoop<AnnotatedBasicBlock>> candidateLoops) {
        LoopStability result = LoopStability.STABLE;
        if(expression instanceof FieldAccess) {
            Expression receiver = ((FieldAccess) expression).getReceiver();
            if(receiver != null) {
                result = LoopStability.merge(result, valueStabilities.getStability(receiver, source.getBasicBlock()));
            }
        } else if(expression instanceof ArrayAccess) {
            Expression arrayRef = ((ArrayAccess) expression).getArrayRef();
            result = LoopStability.merge(result, valueStabilities.getStability(arrayRef, source.getBasicBlock()));
            Expression index = ((ArrayAccess) expression).getIndex();
            result = LoopStability.merge(result, valueStabilities.getStability(index, source.getBasicBlock()));
        } else if(expression instanceof VariableExpression) {
            result = calculateStabilityOfUses((VariableExpression) expression, candidateLoops, new HashSet<>());
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
        gatherImpactingSubExpressions(rhs, excluded, subExpressions, insn);
        return subExpressions;
    }

    private void gatherImpactingSubExpressions(Expression e, Expression excluded, Set<Expression> subExpressions,
                                               AnnotatedInstruction target) {
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
            AnnotatedInstruction source = definitionInsnMap.get(e);
            if(!isExcludedFieldOrArrayAccess(value, excluded, source, target)) {
                gatherImpactingSubExpressions(value, excluded, subExpressions, target);
            }
        } else {
            subExpressions.add(processed);
        }
    }

    private boolean isExcludedFieldOrArrayAccess(Expression value, Expression excluded, AnnotatedInstruction source,
                                                 AnnotatedInstruction target) {
        Expression processedValue = value.accept(propagatingVisitor, null);
        Expression processedExcluded = excluded.accept(propagatingVisitor, null);
        if(processedValue.equals(processedExcluded)) {
            return (processedValue instanceof ArrayAccess && processedExcluded instanceof ArrayAccess
                    && !checkAllPaths(source, target, PossibleArrayDefinitionPredicate.ARRAY_DEFINITION_PREDICATE))
                    || (processedValue instanceof FieldAccess && processedExcluded instanceof FieldAccess
                    && !checkAllPaths(source, target, PossibleFieldDefinitionPredicate.FIELD_DEFINITION_PREDICATE));
        }
        return false;
    }

    private LoopStability calculateStabilityOfUses(VariableExpression expr, Set<NaturalLoop<AnnotatedBasicBlock>> candidateLoops,
                                                   Set<VariableExpression> visited) {
        LoopStability result = LoopStability.STABLE;
        if(visited.add(expr) && usesMap.containsKey(expr)) {
            for(Statement s : usesMap.get(expr)) {
                if(s instanceof AssignmentStatement) {
                    AssignmentStatement assign = ((AssignmentStatement) s);
                    Expression lhs = assign.getLeftHandSide();
                    Expression rhs = assign.getRightHandSide();
                    if(rhs instanceof InvokeExpression) {
                        return new LoopStability.Unstable(candidateLoops);
                    }
                    if(lhs instanceof FieldAccess) {
                        Expression receiver = ((FieldAccess) lhs).getReceiver();
                        if(receiver != null) {
                            result = LoopStability.merge(result, receiver.accept(valueStabilities, candidateLoops));
                        }
                    } else if(lhs instanceof ArrayAccess) {
                        Expression arrayRef = ((ArrayAccess) lhs).getArrayRef();
                        result = LoopStability.merge(result, arrayRef.accept(valueStabilities, candidateLoops));
                        Expression index = ((ArrayAccess) lhs).getIndex();
                        result = LoopStability.merge(result, index.accept(valueStabilities, candidateLoops));
                    }
                } else if(s instanceof ReturnStatement) {
                    result = LoopStability.merge(result, new LoopStability.ParameterDependent(method.getParameterTypes().size()));
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
    private boolean checkAllPaths(AnnotatedInstruction source, AnnotatedInstruction target, Predicate<AbstractInsnNode> predicate) {
        AnnotatedBasicBlock sourceBlock = source.getBasicBlock();
        AnnotatedBasicBlock targetBlock = target.getBasicBlock();
        if(graph.getVertices().size() > 10 && sourceBlock != targetBlock) {
            // Conservatively return true for large graphs when the source block is not the target block.
            return true;
        }
        Set<List<AnnotatedBasicBlock>> simplePaths = FlowGraphUtil.getAllSimplePaths(graph, sourceBlock, targetBlock);
        for(List<AnnotatedBasicBlock> simplePath : simplePaths) {
            for(AnnotatedBasicBlock block : simplePath) {
                AbstractInsnNode insn = block.getFirstInsn();
                if(block == sourceBlock) {
                    insn = source.getInstruction().getNext();
                }
                AbstractInsnNode end = block.getLastInsn();
                while(insn != null && insn != target.getInstruction()) {
                    if(predicate.test(insn)) {
                        return true;
                    } else if(insn == end) {
                        break;
                    }
                    insn = insn.getNext();
                }
            }
        }
        return false;
    }

    public FrameLoopStabilityInfo generateMethodLoopStabilityInfo(AbstractInsnNode insn) {
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
        FrameLoopStabilityInfo info = new FrameLoopStabilityInfo(invocationLevel);
        if(expr.getReceiver() != null) {
            LoopStability c = expr.getReceiver().accept(valueStabilities, candidateLoops);
            info.addLastArgumentLevel(c.toLoopLevel());
        }
        for(Expression arg : expr.getArguments()) {
            LoopStability c = arg.accept(valueStabilities, candidateLoops);
            info.addLastArgumentLevel(c.toLoopLevel());
        }
        if(statement.definesVariable()) {
            LoopStability c = calculateStabilityOfUses(statement.getDefinedVariable(),
                    candidateLoops, new HashSet<>());
            info.addLastArgumentLevel(c.toLoopLevel());
        }
        return info;
    }

    public LoopLevel getLoopLevel(AbstractInsnNode insn) {
        if(insnMap.containsKey(insn)) {
            AnnotatedInstruction ai = insnMap.get(insn);
            LoopStability c = LoopStability.STABLE;
            for(Statement statement : ai.getStatements()) {
                LoopStability c2 = LoopStability.STABLE;
                if(statement instanceof AssignmentStatement) {
                    c2 = calculateStability(ai, (AssignmentStatement) statement);
                } else if(statement instanceof SwitchStatement) {
                    Expression e = ((SwitchStatement) statement).getExpression();
                    c2 = valueStabilities.getStability(e, ai.getBasicBlock());
                } else if(statement instanceof IfStatement) {
                    Expression e = ((IfStatement) statement).getExpression();
                    c2 = valueStabilities.getStability(e, ai.getBasicBlock());
                } else if(statement instanceof ReturnStatement && !((ReturnStatement) statement).isVoid()) {
                    Expression e = ((ReturnStatement) statement).getExpression();
                    c2 = valueStabilities.getStability(e, ai.getBasicBlock());
                    // Add a dependency on the return value memory location
                    c = LoopStability.merge(c, returnDependentLoopStability);
                }
                c = LoopStability.merge(c, c2);
            }
            return c.toLoopLevel();
        }
        return STABLE_LOOP_LEVEL;
    }
}
