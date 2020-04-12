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
 * Calculates the loop-relative constancy of a variable's (local variable or stack element) value.
 * <p>
 * Let m be a method in static single assignment form, s be an assignment statement in m which stores the value
 * of an expression e into the variable x_v (i.e., x_v := e), L1 be the set of natural loops in M, and L2 be a theoretical
 * set of loops which contain a particular call to m at runtime.
 * <p>
 * The value of x_v is considered to be constant relative to a some natural loop, l in L1 or l in L2, s is not contained in
 * l or along all paths consisting only of vertices contained in L starting from the header of L, x_v is either undefined
 * or its definition is always equal to the same value.
 * <p>
 * An expression e is considered to be constant with respect to a loop L if a one of the following conditions is met:
 * <ul>
 *     <li>e is a constant/literal</li>
 *     <li>e is of the form <i>a op b</i>, where op is a binary operator and the subexpressions a and b are both
 *     constant with respect to L </li>
 *     <li>e is of the form <i>op a</i>, where op is a unary operator and the subexpression a is constant with respect
 *     to L </li>
 *     <li>e is a variable whose value is constant with respect to L</li>
 * </ul>
 * If e is a memory allocation instruction (e.g., a new expression, or new array expression) then it is non-constant
 * with respect to all loops that contain it.
 * If e is an array access, field access, phi function, or invoke expression, then we conservatively
 * say that e is non-constant with respect to all loops that contain it.
 */
public class ValueConstancies implements StatefulExpressionVisitor<Constancy, Set<NaturalLoop<AnnotatedBasicBlock>>> {

    private final Map<VariableExpression, Constancy> valueConstancyMap = new HashMap<>();
    private final Map<AnnotatedBasicBlock, Set<NaturalLoop<AnnotatedBasicBlock>>> containingLoops;
    private final PropagatingVisitor propagatingVisitor;

    public ValueConstancies(SSAMethod method, PropagatingVisitor propagatingVisitor) {
        this.propagatingVisitor = propagatingVisitor;
        FlowGraph<AnnotatedBasicBlock> cfg = method.getControlFlowGraph();
        containingLoops = FlowGraphUtil.calculateContainingLoops(cfg);
        calculateValueConstancyMap(cfg.getEntryPoint(), cfg);
    }

    private void calculateValueConstancyMap(AnnotatedBasicBlock block, FlowGraph<AnnotatedBasicBlock> cfg) {
        for(AnnotatedInstruction i : block.getInstructions()) {
            for(Statement s : i.getStatements()) {
                if(s instanceof AssignmentStatement && s.definesVariable()) {
                    VariableExpression variable = s.getDefinedVariable();
                    Expression expression = ((AssignmentStatement) s).getRightHandSide();
                    // Need to propagate to handle merged phi functions
                    expression = expression.accept(propagatingVisitor, s.getDefinedVariable());
                    Set<NaturalLoop<AnnotatedBasicBlock>> candidateLoops = containingLoops.get(block);
                    valueConstancyMap.put(variable, expression.accept(this, candidateLoops));
                }
            }
        }
        for(AnnotatedBasicBlock child : cfg.getDominatorTree().get(block)) {
            calculateValueConstancyMap(child, cfg);
        }
    }

    public Constancy getConstancy(Expression expression, AnnotatedBasicBlock location) {
        if((expression instanceof LocalVariable || expression instanceof StackElement)
                && !valueConstancyMap.containsKey(expression)) {
            throw new IllegalArgumentException();
        }
        Set<NaturalLoop<AnnotatedBasicBlock>> candidates = containingLoops.get(location);
        if(candidates == null) {
            candidates = Collections.emptySet();
        }
        return expression.accept(this, candidates);
    }

    @Override
    public Constancy visit(ArrayAccess expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return new Constancy.Nonconstant(candidates);
    }

    @Override
    public Constancy visit(CaughtExceptionExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return new Constancy.Nonconstant(candidates);
    }

    @Override
    public Constancy visit(FieldAccess expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return new Constancy.Nonconstant(candidates);
    }

    @Override
    public Constancy visit(InvokeExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return new Constancy.Nonconstant(candidates);
    }

    @Override
    public Constancy visit(NewArrayExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return new Constancy.Nonconstant(candidates);
    }

    @Override
    public Constancy visit(NewExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return new Constancy.Nonconstant(candidates);
    }

    @Override
    public Constancy visit(PhiFunction expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return new Constancy.Nonconstant(candidates);
    }

    @Override
    public Constancy visit(DoubleConstantExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return Constancy.CONSTANT;
    }

    @Override
    public Constancy visit(FloatConstantExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return Constancy.CONSTANT;
    }

    @Override
    public Constancy visit(IntegerConstantExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return Constancy.CONSTANT;
    }

    @Override
    public Constancy visit(LongConstantExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return Constancy.CONSTANT;
    }

    @Override
    public Constancy visit(ObjectConstantExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return Constancy.CONSTANT;
    }

    @Override
    public Constancy visit(ParameterExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return new Constancy.ParameterDependent(expression);
    }

    @Override
    public Constancy visit(UnaryExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        return expression.getOperand().accept(this, candidates);
    }

    @Override
    public Constancy visit(BinaryExpression expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        Constancy c1 = expression.getOperand1().accept(this, candidates);
        Constancy c2 = expression.getOperand2().accept(this, candidates);
        return Constancy.merge(c1, c2);
    }

    @Override
    public Constancy visit(LocalVariable expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        if(!valueConstancyMap.containsKey(expression)) {
            throw new IllegalStateException();
        }
        return valueConstancyMap.get(expression).restrict(candidates);
    }

    @Override
    public Constancy visit(StackElement expression, Set<NaturalLoop<AnnotatedBasicBlock>> candidates) {
        if(!valueConstancyMap.containsKey(expression)) {
            throw new IllegalStateException();
        }
        return valueConstancyMap.get(expression).restrict(candidates);
    }
}
