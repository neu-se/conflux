package edu.gmu.swe.phosphor.ignored.control.binding;

import edu.columbia.cs.psl.phosphor.control.graph.BasicBlock;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.InsnList;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.struct.SinglyLinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.PropagationTransformer;
import edu.gmu.swe.phosphor.ignored.control.ssa.SSABasicBlock;
import edu.gmu.swe.phosphor.ignored.control.ssa.SSAMethod;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.AssignmentStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;
import edu.gmu.swe.phosphor.ignored.control.tac.ThreeAddressMethod;

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Calculates the loop-relative constancy of each variable (local variable or stack element) definition.
 * <p>
 * Let s be an assignment statement for some representation in static single assignment form which assigns a
 * variable, x_v, the value of an expression, e.
 * The definition of x_v is constant relative to a natural loop, L, that contains the statement s, if along all
 * paths from the header of L consisting only of vertices contained in L, x_v is either undefined or
 * its definition is always equal to the same value.
 * <p>
 * E -> T | (unary_op T) | (T binary_op T)
 * T -> array_access | constant | field_access | invoke_expression | new_expression | new_array_expression
 * | parameter_expression | phi_function | variable
 */
public class LoopConstancyCalculator {

    private final Map<AbstractInsnNode, StatementInfo> insnStatementMap;
    private final StatementInfo parameterInfo;

    public LoopConstancyCalculator(String owner, MethodNode methodNode) throws AnalyzerException {
        ThreeAddressMethod threeAddressMethod = new ThreeAddressMethod(owner, methodNode);
        SSAMethod ssaMethod = new SSAMethod(threeAddressMethod);
        PropagationTransformer transformer = new PropagationTransformer(propagateVariables(ssaMethod));
        InsnList instructions = threeAddressMethod.getOriginalMethod().instructions;
        insnStatementMap = new LinkedHashMap<>();
        Iterator<AbstractInsnNode> itr = instructions.iterator();
        while(itr.hasNext()) {
            AbstractInsnNode insn = itr.next();
            Statement[] threeAddressStatements = threeAddressMethod.getStatements(insn);
            Statement[] ssaStatements = ssaMethod.getStatements(insn);
            Statement[] propagatedStatements = new Statement[ssaStatements.length];
            for(int i = 0; i < ssaStatements.length; i++) {
                propagatedStatements[i] = ssaStatements[i].transform(transformer);
            }
            StatementInfo info = new StatementInfo(insn, threeAddressStatements, ssaStatements, propagatedStatements);
            insnStatementMap.put(insn, info);
        }
        Statement[] threeAddressStatements = threeAddressMethod.getParameterDefinitions().toArray(new Statement[0]);
        Statement[] ssaStatements = ssaMethod.getParameterDefinitions().toArray(new Statement[0]);
        Statement[] propagatedStatements = new Statement[ssaStatements.length];
        for(int i = 0; i < ssaStatements.length; i++) {
            propagatedStatements[i] = ssaStatements[i].transform(transformer);
        }
        parameterInfo = new StatementInfo(null, threeAddressStatements, ssaStatements, propagatedStatements);
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
        return checkAllPaths(source, target, cfg, insnBlockMap, abstractInsnNode -> false);
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
        return checkAllPaths(source, target, cfg, insnBlockMap, abstractInsnNode -> false);
    }

    public static <T extends BasicBlock> boolean checkAllPaths(AbstractInsnNode source, AbstractInsnNode target,
                                                               FlowGraph<T> cfg, Map<AbstractInsnNode, T> insnBlockMap,
                                                               Predicate<AbstractInsnNode> predicate) {
        T sourceBlock = insnBlockMap.get(source);
        T targetBlock = insnBlockMap.get(target);
        Set<T> visited = new HashSet<>();
        SinglyLinkedList<T> queue = new SinglyLinkedList<>();
        if(checkInstructions(source.getNext(), sourceBlock.getLastInsn(), predicate)) {
            return true;
        }
        queue.enqueue(sourceBlock);
        visited.add(sourceBlock);
        while(!queue.isEmpty()) {
            for(T successor : cfg.getSuccessors(queue.removeFirst())) {
                if(successor.equals(targetBlock)) {
                    if(successor.getFirstInsn() == target || target.getPrevious() == null) {
                        return false;
                    } else {
                        return checkInstructions(successor.getFirstInsn(), target.getPrevious(), predicate);
                    }
                } else if(visited.add(successor)) {
                    if(checkInstructions(successor.getFirstInsn(), successor.getLastInsn(), predicate)) {
                        return true;
                    }
                    queue.enqueue(successor);
                }
            }
        }
        return false;
    }

    public static boolean checkInstructions(AbstractInsnNode start, AbstractInsnNode end, Predicate<AbstractInsnNode> predicate) {
        while(start != null) {
            if(predicate.test(start)) {
                return true;
            } else if(start == end) {
                break;
            }
            start = start.getNext();
        }
        return false;
    }

    public static Map<VariableExpression, Expression> propagateVariables(SSAMethod method) {
        Map<VariableExpression, Expression> definitions = new HashMap<>();
        for(SSABasicBlock block : method.getControlFlowGraph().getVertices()) {
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

    private static class StatementInfo {
        private final AbstractInsnNode insn;
        private final Statement[] threeAddressStatements;
        private final Statement[] ssaStatements;
        private final Statement[] propagatedStatements;

        public StatementInfo(AbstractInsnNode insn, Statement[] threeAddressStatements, Statement[] ssaStatements,
                             Statement[] propagatedStatements) {
            this.insn = insn;
            this.threeAddressStatements = threeAddressStatements;
            this.ssaStatements = ssaStatements;
            this.propagatedStatements = propagatedStatements;
        }
    }
}
