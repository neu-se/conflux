package edu.neu.ccs.conflux.internal.policy.binding.tracer;

import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph.NaturalLoop;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashMap;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import edu.neu.ccs.conflux.internal.policy.FlowGraphUtil;
import edu.neu.ccs.conflux.internal.policy.ssa.AnnotatedBasicBlock;
import edu.neu.ccs.conflux.internal.policy.ssa.AnnotatedInstruction;
import edu.neu.ccs.conflux.internal.policy.ssa.PropagatingVisitor;
import edu.neu.ccs.conflux.internal.policy.ssa.SSAMethod;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.*;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.AssignmentStatement;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.Statement;

public class ValueStabilities implements StatefulExpressionVisitor<LoopStability, Set<NaturalLoop<AnnotatedBasicBlock>>> {

    private final Map<VariableExpression, LoopStability> valueStabilityMap = new HashMap<>();
    private final Map<AnnotatedBasicBlock, Set<NaturalLoop<AnnotatedBasicBlock>>> containingLoops;
    private final PropagatingVisitor propagatingVisitor;

    public ValueStabilities(SSAMethod method, PropagatingVisitor propagatingVisitor) {
        this.propagatingVisitor = propagatingVisitor;
        FlowGraph<AnnotatedBasicBlock> cfg = method.getControlFlowGraph();
        containingLoops = FlowGraphUtil.calculateContainingLoops(cfg);
        calculateValueStabilityMap(cfg.getEntryPoint(), cfg);
    }

    private void calculateValueStabilityMap(AnnotatedBasicBlock block, FlowGraph<AnnotatedBasicBlock> cfg) {
        for(AnnotatedInstruction i : block.getInstructions()) {
            for(Statement s : i.getStatements()) {
                if(s instanceof AssignmentStatement && s.definesVariable()) {
                    VariableExpression variable = s.getDefinedVariable();
                    Expression expression = ((AssignmentStatement) s).getRightHandSide();
                    // Need to propagate to handle merged phi functions
                    expression = expression.accept(propagatingVisitor, s.getDefinedVariable());
                    Set<NaturalLoop<AnnotatedBasicBlock>> candidateLoops = containingLoops.get(block);
                    valueStabilityMap.put(variable, expression.accept(this, candidateLoops));
                }
            }
        }
        for(AnnotatedBasicBlock child : cfg.getDominatorTree().get(block)) {
            calculateValueStabilityMap(child, cfg);
        }
    }

    public LoopStability getStability(Expression expression, AnnotatedBasicBlock location) {
        if((expression instanceof LocalVariable || expression instanceof StackElement)
                && !valueStabilityMap.containsKey(expression)) {
            throw new IllegalArgumentException();
        }
        expression = expression.accept(propagatingVisitor, null);
        Set<NaturalLoop<AnnotatedBasicBlock>> candidates = containingLoops.get(location);
        if(candidates == null) {
            candidates = Collections.emptySet();
        }
        return expression.accept(this, candidates);
    }

    @Override
    public LoopStability visit(ArrayAccess expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return new LoopStability.Unstable(candidates);
    }

    @Override
    public LoopStability visit(CaughtExceptionExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return new LoopStability.Unstable(candidates);
    }

    @Override
    public LoopStability visit(FieldAccess expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return new LoopStability.Unstable(candidates);
    }

    @Override
    public LoopStability visit(InvokeExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return new LoopStability.Unstable(candidates);
    }

    @Override
    public LoopStability visit(NewArrayExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return new LoopStability.Unstable(candidates);
    }

    @Override
    public LoopStability visit(NewExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return new LoopStability.Unstable(candidates);
    }

    @Override
    public LoopStability visit(PhiFunction expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return new LoopStability.Unstable(candidates);
    }

    @Override
    public LoopStability visit(DoubleConstantExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return LoopStability.STABLE;
    }

    @Override
    public LoopStability visit(FloatConstantExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return LoopStability.STABLE;
    }

    @Override
    public LoopStability visit(IntegerConstantExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return LoopStability.STABLE;
    }

    @Override
    public LoopStability visit(LongConstantExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return LoopStability.STABLE;
    }

    @Override
    public LoopStability visit(ObjectConstantExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return LoopStability.STABLE;
    }

    @Override
    public LoopStability visit(ParameterExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return new LoopStability.ParameterDependent(expression);
    }

    @Override
    public LoopStability visit(UnaryExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return expression.getOperand().accept(this, candidates);
    }

    @Override
    public LoopStability visit(BinaryExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        LoopStability c1 = expression.getOperand1().accept(this, candidates);
        LoopStability c2 = expression.getOperand2().accept(this, candidates);
        return LoopStability.merge(c1, c2);
    }

    @Override
    public LoopStability visit(LocalVariable expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        if(!valueStabilityMap.containsKey(expression)) {
            throw new IllegalStateException();
        }
        return valueStabilityMap.get(expression).restrict(candidates);
    }

    @Override
    public LoopStability visit(StackElement expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        if(!valueStabilityMap.containsKey(expression)) {
            throw new IllegalStateException();
        }
        return valueStabilityMap.get(expression).restrict(candidates);
    }
}
