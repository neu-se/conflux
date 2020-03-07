package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.AssignmentStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;
import edu.gmu.swe.phosphor.ignored.control.tac.ThreeAddressMethod;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;

import static edu.gmu.swe.phosphor.ignored.control.ControlAnalysisTestUtil.getMethodNode;
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
        for(Statement s : createStatementList(ssaMethod)) {
            if(s.definesVariable()) {
                allDefinitions.add(s.definedVariable());
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
        FlowGraph<SSABasicBlock> cfg = ssaMethod.getControlFlowGraph();
        Map<VariableExpression, SSABasicBlock> definitions = new HashMap<>();
        Map<VariableExpression, Set<SSABasicBlock>> uses = new HashMap<>();
        for(SSABasicBlock block : cfg.getVertices()) {
            for(Statement statement : block.getStatements()) {
                if(statement.definesVariable()) {
                    definitions.put(statement.definedVariable(), block);
                }
                if(!isPhiFunctionStatement(statement)) {
                    for(VariableExpression use : statement.usedVariables()) {
                        if(!uses.containsKey(use)) {
                            uses.put(use, new HashSet<>());
                        }
                        uses.get(use).add(block);
                    }
                }
            }
        }
        for(VariableExpression usedExpression : uses.keySet()) {
            SSABasicBlock definingBlock = definitions.get(usedExpression);
            for(SSABasicBlock usingBlock : uses.get(usedExpression)) {
                assertTrue(String.format("Use of %s in %s is not dominated by its definition in %s", usedExpression,
                        usingBlock, definingBlock), cfg.getDominatorSets().get(usingBlock).contains(definingBlock));
            }
        }
    }

    @Theory
    public void testPerformPropagation(Method method) throws Exception {
        SSAMethod ssaMethod = convertToSSA(method);
        PropagationTransformer transformer = new PropagationTransformer(ssaMethod.propagateVariables());
        List<Statement> statements = new LinkedList<>();
        for(Statement statement : createStatementList(ssaMethod)) {
            statements.add(statement.transform(transformer));
        }
        String original = format(createStatementList(ssaMethod));
        String propagated = format(statements);
    }

    private static String format(List<Statement> statements) {
        List<String> s = new LinkedList<>();
        for(Statement statement : statements) {
            s.add(statement.toString());
        }
        return String.join("\n", s);
    }

    @DataPoints
    public static Method[] methods() {
        List<Method> methods = new LinkedList<>();
        List<Class<?>> targetClasses = Arrays.asList(String.class, HashMap.class, LinkedList.class);
        for(Class<?> targetClass : targetClasses) {
            methods.addAll(Arrays.asList(targetClass.getMethods()));
        }
        return methods.toArray(new Method[0]);
    }

    private static SSAMethod convertToSSA(Method method) throws Exception {
        assumeTrue(method != null);
        MethodNode methodNode = getMethodNode(method.getDeclaringClass(), method.getName());
        String owner = Type.getInternalName(method.getDeclaringClass());
        return new SSAMethod(new ThreeAddressMethod(owner, methodNode));
    }

    private static List<Statement> createStatementList(SSAMethod ssaMethod) {
        List<Statement> list = new LinkedList<>();
        for(SSABasicBlock block : ssaMethod.getControlFlowGraph().getVertices()) {
            list.addAll(block.getStatements());
        }
        return list;
    }

    private static boolean isPhiFunctionStatement(Statement statement) {
        return statement instanceof AssignmentStatement
                && ((AssignmentStatement) statement).getRightHandSide() instanceof PhiFunction;
    }
}
