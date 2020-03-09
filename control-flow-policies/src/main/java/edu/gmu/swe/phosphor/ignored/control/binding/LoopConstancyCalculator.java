package edu.gmu.swe.phosphor.ignored.control.binding;

import edu.columbia.cs.psl.phosphor.control.graph.BasicBlock;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashMap;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.PropagationTransformer;
import edu.gmu.swe.phosphor.ignored.control.ssa.SSABasicBlock;
import edu.gmu.swe.phosphor.ignored.control.ssa.SSAMethod;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.AssignmentStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

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
public class LoopConstancyCalculator {

    public LoopConstancyCalculator(String owner, MethodNode methodNode) throws AnalyzerException {
        SSAMethod ssaMethod = new SSAMethod(owner, methodNode);
        PropagationTransformer transformer = new PropagationTransformer(propagateVariables(ssaMethod.getSsaControlFlowGraph()));
    }

    /**
     * Returns true if there exists some execution path from the specified source instruction to the specified target
     * insn that contains one of the following (excluding the source and target instructions):
     * <ul>
     *     <li>an InvokeStatement</li>
     *     <li>an AssignmentStatement that assigns a value to a FieldAccess that could represent the same memory
     *     location as the specified expression (the name of the field must be the same and the field must be static
     *     if and only if the specified field is static.</li>
     * </ul>
     */
    public static <T extends BasicBlock> boolean interveningFieldRedefinitionPossible(AbstractInsnNode source,
                                                                                      AbstractInsnNode target, FlowGraph<T> cfg,
                                                                                      Map<AbstractInsnNode, T> insnBlockMap, FieldAccess expression) {
        return false;
    }

    /**
     * Returns true if there exists some execution path from the specified source instruction to the specified target
     * insn that contains one of the following (excluding the source and target instructions):
     * <ul>
     *     <li>an InvokeStatement</li>
     *     <li>an AssignmentStatement that assigns a value to an ArrayAccess</li>
     * </ul>
     */
    public static <T extends BasicBlock> boolean interveningArrayRedefinitionPossible(AbstractInsnNode source, AbstractInsnNode target,
                                                                                      FlowGraph<T> cfg,
                                                                                      Map<AbstractInsnNode, T> insnBlockMap) {
        return false;
    }

    public static Map<VariableExpression, Expression> propagateVariables(FlowGraph<SSABasicBlock> graph) {
        Map<VariableExpression, Expression> definitions = new HashMap<>();
        for(SSABasicBlock block : graph.getVertices()) {
            for(Statement statement : block.getStatements()) {
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
