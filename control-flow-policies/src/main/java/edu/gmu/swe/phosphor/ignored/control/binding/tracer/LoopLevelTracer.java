package edu.gmu.swe.phosphor.ignored.control.binding.tracer;

import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph.NaturalLoop;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
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

import java.util.function.Predicate;

import static edu.gmu.swe.phosphor.ignored.control.binding.LoopLevel.ConstantLoopLevel.CONSTANT_LOOP_LEVEL;
import static edu.gmu.swe.phosphor.ignored.control.binding.tracer.PossibleArrayDefinitionPredicate.ARRAY_DEFINITION_PREDICATE;
import static edu.gmu.swe.phosphor.ignored.control.binding.tracer.PossibleFieldDefinitionPredicate.FIELD_DEFINITION_PREDICATE;

/**
 * Assignment Statements:
 * Let m be a method in static single assignment form, s be an assignment statement in m which stores the value
 * of an expression e into the variable x_v (i.e., x_v := e), L1 be the set of natural loops in M, and L2 be the
 * theoretical set of loops which contain a particular call to m at runtime.
 * TODO
 * <p>
 * Return Statements:
 * Non-void return statements can be seen as assignment statements that span a method call. The constancy of the value
 * of the statement is determined by the callee and the constancy of the memory location is determined by the caller.
 * TODO
 * <p>
 * Branches:
 * A branching statement that is conditional on some expression e is considered to be constant with respect to a loop L
 * if the value of e is considered to be constant with respect to L.
 * <p>
 * Method Calls:
 * TODO
 */
public class LoopLevelTracer {

    private final SSAMethod method;
    private final FlowGraph<AnnotatedBasicBlock> graph;
    private final Map<AnnotatedBasicBlock, Set<NaturalLoop<AnnotatedBasicBlock>>> containingLoops;
    private final Map<AbstractInsnNode, AnnotatedInstruction> insnMap = new HashMap<>();
    private final Map<AbstractInsnNode, AnnotatedBasicBlock> blockMap = new HashMap<>();
    private final Map<VariableExpression, Expression> definitionValues = new HashMap<>();
    private final Map<VariableExpression, AnnotatedInstruction> definitionInsnMap = new HashMap<>();
    private final Map<VariableExpression, Set<Statement>> usesMap = new HashMap<>();
    private final Constancy returnDependentConstancy;
    private final ValueConstancies valueConstancies;
    private final PropagatingVisitor propagatingVisitor;

    public LoopLevelTracer(SSAMethod method) {
        this.method = method;
        propagatingVisitor = new PropagatingVisitor(method.getControlFlowGraph());
        valueConstancies = new ValueConstancies(method, propagatingVisitor);
        returnDependentConstancy = new Constancy.ParameterDependent(method.getParameterTypes().size());
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

    private Constancy calculateConstancy(AnnotatedInstruction source, AssignmentStatement statement) {
        statement = (AssignmentStatement) statement.accept(propagatingVisitor);
        Expression lhs = statement.getLeftHandSide();
        Set<NaturalLoop<AnnotatedBasicBlock>> candidateLoops = containingLoops.get(source.getBasicBlock());
        Constancy c = getMemoryLocationConstancy(statement.getLeftHandSide(), source, candidateLoops);
        if(lhs instanceof FieldAccess || lhs instanceof ArrayAccess || lhs instanceof VariableExpression) {
            for(Expression subExpression : gatherImpactingSubExpressions(source, statement)) {
                c = Constancy.merge(c, subExpression.accept(valueConstancies, candidateLoops));
            }
        } else {
            throw new IllegalArgumentException();
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
            result = calculateConstancyOfUses((VariableExpression) expression, candidateLoops, new HashSet<>());
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
                    && !checkAllPaths(source, target, ARRAY_DEFINITION_PREDICATE))
                    || (processedValue instanceof FieldAccess && processedExcluded instanceof FieldAccess
                    && !checkAllPaths(source, target, FIELD_DEFINITION_PREDICATE));
        }
        return false;
    }

    private Constancy calculateConstancyOfUses(VariableExpression expr, Set<NaturalLoop<AnnotatedBasicBlock>> candidateLoops,
                                               Set<VariableExpression> visited) {
        Constancy result = Constancy.CONSTANT;
        if(visited.add(expr) && usesMap.containsKey(expr)) {
            for(Statement s : usesMap.get(expr)) {
                if(s instanceof AssignmentStatement) {
                    AssignmentStatement assign = ((AssignmentStatement) s);
                    Expression lhs = assign.getLeftHandSide();
                    Expression rhs = assign.getRightHandSide();
                    if(rhs instanceof InvokeExpression) {
                        return new Constancy.Nonconstant(candidateLoops);
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
                    candidateLoops, new HashSet<>());
            info.pushArgumentLevel(c.toLoopLevel());
        }
        return info;
    }

    public LoopLevel getLoopLevel(AbstractInsnNode insn) {
        if(insnMap.containsKey(insn)) {
            AnnotatedInstruction ai = insnMap.get(insn);
            Constancy c = Constancy.CONSTANT;
            for(Statement statement : ai.getStatements()) {
                Constancy c2 = Constancy.CONSTANT;
                if(statement instanceof AssignmentStatement) {
                    c2 = calculateConstancy(ai, (AssignmentStatement) statement);
                } else if(statement instanceof SwitchStatement) {
                    Expression e = ((SwitchStatement) statement).getExpression();
                    c2 = valueConstancies.getConstancy(e, ai.getBasicBlock());
                } else if(statement instanceof IfStatement) {
                    Expression e = ((IfStatement) statement).getExpression();
                    c2 = valueConstancies.getConstancy(e, ai.getBasicBlock());
                } else if(statement instanceof ReturnStatement && !((ReturnStatement) statement).isVoid()) {
                    Expression e = ((ReturnStatement) statement).getExpression();
                    c2 = valueConstancies.getConstancy(e, ai.getBasicBlock());
                    // Add a dependency on the return value memory location
                    c = Constancy.merge(c, returnDependentConstancy);
                }
                c = Constancy.merge(c, c2);
            }
            return c.toLoopLevel();
        }
        return CONSTANT_LOOP_LEVEL;
    }
}
