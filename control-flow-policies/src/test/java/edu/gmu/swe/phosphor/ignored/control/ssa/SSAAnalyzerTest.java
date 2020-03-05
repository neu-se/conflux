package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VersionedExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.AssignmentStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.*;

import static edu.gmu.swe.phosphor.ignored.control.ControlAnalysisTestUtil.getMethodNode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class SSAAnalyzerTest {

    /**
     * Checks that each assignment to a LocalVariable or StackElement has a unique version
     */
    @Theory
    public void assignmentsUniquelyNamed(Method method) throws Exception {
        SSAAnalyzer analyzer = makeAnalyzer(method);
        List<VersionedExpression> allDefinitions = new LinkedList<>();
        for(VersionedExpression expr : analyzer.getParameterDefinitions()) {
            allDefinitions.add(expr);
        }
        for(Statement s : analyzer.createProcessedStatementList()) {
            if(s instanceof AssignmentStatement) {
                AssignmentStatement assignment = (AssignmentStatement) s;
                if(assignment.getLeftHandSide() instanceof VersionedExpression) {
                    allDefinitions.add((VersionedExpression) assignment.getLeftHandSide());
                }
            }
        }
        String s = analyzer.toString();
        assertEquals(new LinkedList<>(new LinkedHashSet<>(allDefinitions)), allDefinitions);
    }

    /**
     * Checks that along any control flow path any use of V_i in the transformed program and the corresponding use of V
     * in the original program V and V_i have the same value
     */
    @Theory
    public void valuesPreserved(Method method) throws Exception {
        SSAAnalyzer analyzer = makeAnalyzer(method);
    }

    /**
     * Checks that if a CFG vertex Z is the first vertex common to two nonnull paths X -> Z and Y -> Z
     * that start at vertices X and Y contain at least one assignments to V, then a phi-function for V has been
     * inserted at the entrance to Z.
     */
    @Theory
    public void phiFunctionAtMerge(Method method) throws Exception {
        SSAAnalyzer analyzer = makeAnalyzer(method);
    }

    private static SSAAnalyzer makeAnalyzer(Method method) throws Exception {
        assumeTrue(method != null);
        MethodNode methodNode = getMethodNode(method.getDeclaringClass(), method.getName());
        String owner = Type.getInternalName(method.getDeclaringClass());
        return new SSAAnalyzer(owner, methodNode);
    }

    @DataPoints
    public static List<Method> methods() {
        List<Method> methods = new LinkedList<>();
        List<Class<?>> targetClasses = Arrays.asList(String.class, HashMap.class, LinkedList.class);
        for(Class<?> targetClass : targetClasses) {
            methods.addAll(Arrays.asList(targetClass.getMethods()));
        }
        return methods;
    }
}
