package edu.gmu.swe.phosphor.ignored.control.binding.tracer;

import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph.NaturalLoop;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashMap;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import edu.gmu.swe.phosphor.ignored.control.FlowGraphUtil;
import edu.gmu.swe.phosphor.ignored.control.ssa.AnnotatedBasicBlock;
import edu.gmu.swe.phosphor.ignored.control.ssa.AnnotatedInstruction;
import edu.gmu.swe.phosphor.ignored.control.ssa.PropagatingVisitor;
import edu.gmu.swe.phosphor.ignored.control.ssa.SSAMethod;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.AssignmentStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

/**
 * Calculates the loop-relative stability of a variable's (local variable or stack element) value.
 * <p>
 * Let m be a method in static single assignment form, s be an assignment statement in m which stores the value
 * of an expression e into the variable x_v (i.e., x_v := e), L1 be the set of natural loops in M, and L2 be the theoretical
 * set of loops which contain a particular call to m at runtime.
 * <p>
 * The value of x_v is considered to be stable relative to a some natural loop, l in L1 or l in L2, s is not contained in
 * l or along all paths consisting only of vertices contained in L starting from the header of L, x_v is either undefined
 * or its definition is always equal to the same value.
 * <p>
 * An expression e is considered to be stable with respect to a loop L if a one of the following conditions is met:
 * <ul>
 *     <li>e is a constant/literal</li>
 *     <li>e is of the form <i>a op b</i>, where op is a binary operator and the subexpressions a and b are both
 *     stable with respect to L </li>
 *     <li>e is of the form <i>op a</i>, where op is a unary operator and the subexpression a is stable with respect
 *     to L </li>
 *     <li>e is a variable whose value is stable with respect to L</li>
 * </ul>
 * If e is a memory allocation instruction (e.g., a new expression, or new array expression) then it is unstable
 * with respect to all loops that contain it.
 * If e is an array access, field access, phi function, or invoke expression, then we conservatively
 * say that e is unstable with respect to all loops that contain it.
 */
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
