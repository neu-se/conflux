package edu.neu.ccs.conflux.internal.policy.ssa;

import com.sun.beans.decoder.DocumentHandler;
import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.neu.ccs.conflux.internal.policy.conflux.tracer.UseGatheringVisitor;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.*;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.AssignmentStatement;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.Statement;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.GregorianCalendar;

import static edu.neu.ccs.conflux.internal.policy.ControlAnalysisTestUtil.getMethodNode;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class SSAMethodTest {

    /**
     * Checks that each assignment to a LocalVariable or StackElement has a unique version
     */
    @Theory
    public void assignmentsUniquelyNamed(Method method) throws Exception {
        SSAMethod ssaMethod = convertToSSA(method);
        List<VariableExpression> allDefinitions = new LinkedList<>();
        for(AnnotatedBasicBlock block : ssaMethod.getControlFlowGraph().getVertices()) {
            for(AnnotatedInstruction i : block.getInstructions()) {
                for(Statement s : i.getStatements()) {
                    if(s.definesVariable()) {
                        allDefinitions.add(s.getDefinedVariable());
                    }
                }
            }
        }
        // Maps local variable indices to sets of versions
        Map<Integer, Set<Integer>> localVariableVersions = new HashMap<>();
        // Maps stack element indices to sets of versions
        Map<Integer, Set<Integer>> stackElementVersions = new HashMap<>();
        for(VariableExpression expr : allDefinitions) {
            if(expr instanceof StackElement) {
                int index = ((StackElement) expr).getIndex();
                if(!stackElementVersions.containsKey(index)) {
                    stackElementVersions.put(index, new HashSet<>());
                }
                Set<Integer> versions = stackElementVersions.get(index);
                int version = expr.getVersion();
                assertTrue("Multiple assignments made to:" + expr, versions.add(version));
            } else if(expr instanceof LocalVariable) {
                int index = ((LocalVariable) expr).getIndex();
                if(!localVariableVersions.containsKey(index)) {
                    localVariableVersions.put(index, new HashSet<>());
                }
                Set<Integer> versions = localVariableVersions.get(index);
                int version = expr.getVersion();
                assertTrue("Multiple assignments made to:" + expr, versions.add(version));
            }
        }
    }

    /**
     * Checks that each use of a LocalVariable or StackElement not in a PhiFunction is dominated by the definition of
     * the LocalVariable or StackElement
     */
    @Theory
    public void definitionDominatesUse(Method method) throws Exception {
        SSAMethod ssaMethod = convertToSSA(method);
        FlowGraph<AnnotatedBasicBlock> cfg = ssaMethod.getControlFlowGraph();
        Map<VariableExpression, AnnotatedBasicBlock> definitions = new HashMap<>();
        Map<VariableExpression, Set<AnnotatedBasicBlock>> uses = new HashMap<>();
        UseGatheringVisitor useGatherer = UseGatheringVisitor.INSTANCE;
        for(AnnotatedBasicBlock block : cfg.getVertices()) {
            for(AnnotatedInstruction i : block.getInstructions()) {
                for(Statement statement : i.getStatements()) {
                    if(statement.definesVariable()) {
                        definitions.put(statement.getDefinedVariable(), block);
                    }
                    if(!(statement instanceof AssignmentStatement
                            && ((AssignmentStatement) statement).getRightHandSide() instanceof PhiFunction)) {
                        for(VariableExpression use : statement.accept(useGatherer)) {
                            if(!uses.containsKey(use)) {
                                uses.put(use, new HashSet<>());
                            }
                            uses.get(use).add(block);
                        }
                    }
                }
            }
        }
        for(VariableExpression usedExpression : uses.keySet()) {
            AnnotatedBasicBlock definingBlock = definitions.get(usedExpression);
            for(AnnotatedBasicBlock usingBlock : uses.get(usedExpression)) {
                assertTrue(String.format("Use of %s in %s is not dominated by its definition in %s", usedExpression,
                        usingBlock, definingBlock), cfg.getDominatorSets().get(usingBlock).contains(definingBlock));
            }
        }
    }

    /**
     * Checks that each PhiFunction has at least two values.
     */
    @Theory
    public void phiFunctionsHaveAtLeastTwoValues(Method method) throws Exception {
        SSAMethod ssaMethod = convertToSSA(method);
        FlowGraph<AnnotatedBasicBlock> cfg = ssaMethod.getControlFlowGraph();
        for(AnnotatedBasicBlock block : cfg.getVertices()) {
            for(AnnotatedInstruction i : block.getInstructions()) {
                for(Statement statement : i.getStatements()) {
                    if(statement instanceof AssignmentStatement) {
                        Expression rhs = ((AssignmentStatement) statement).getRightHandSide();
                        if(rhs instanceof PhiFunction) {
                            assertTrue(((PhiFunction) rhs).getValues().size() >= 2);
                        }
                    }
                }
            }
        }
    }

    @DataPoints
    public static Method[] methods() {
        List<Method> methods = new LinkedList<>();
        List<Class<?>> targetClasses = Arrays.asList(String.class, HashMap.class, LinkedList.class, Lister.class,
                DocumentHandler.class, GregorianCalendar.class);
        for(Class<?> targetClass : targetClasses) {
            methods.addAll(Arrays.asList(targetClass.getMethods()));
        }
        return methods.toArray(new Method[0]);
    }

    private static SSAMethod convertToSSA(Method method) throws Exception {
        assumeTrue(method != null);
        MethodNode methodNode = getMethodNode(method.getDeclaringClass(), method.getName());
        String owner = Type.getInternalName(method.getDeclaringClass());
        return new SSAMethod(owner, methodNode);
    }
}
