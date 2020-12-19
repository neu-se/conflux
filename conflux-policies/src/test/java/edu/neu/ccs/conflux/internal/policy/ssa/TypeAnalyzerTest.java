package edu.neu.ccs.conflux.internal.policy.ssa;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.neu.ccs.conflux.internal.policy.ControlAnalysisTestUtil;
import org.junit.Test;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.IFEQ;
import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.IFNE;
import static junit.framework.TestCase.assertSame;

public class TypeAnalyzerTest {

    @Test
    public void testBooleanReturnValue() throws AnalyzerException {
        checkConditionalBranches(TypeAnalyzerTestMethods.booleanReturnValue(), true);
    }

    @Test
    public void testIntReturnValue() throws AnalyzerException {
        checkConditionalBranches(TypeAnalyzerTestMethods.intReturnValue(), false);

    }

    @Test
    public void testBooleanParam() throws AnalyzerException {
        checkConditionalBranches(TypeAnalyzerTestMethods.booleanParam(), true);
    }

    @Test
    public void testIntParam() throws AnalyzerException {
        checkConditionalBranches(TypeAnalyzerTestMethods.intParam(), false);

    }

    @Test
    public void testBooleanField() throws AnalyzerException {
        checkConditionalBranches(TypeAnalyzerTestMethods.booleanField(), true);
    }

    @Test
    public void testIntField() throws AnalyzerException {
        checkConditionalBranches(TypeAnalyzerTestMethods.intField(), false);

    }

    @Test
    public void testBooleanArray() throws AnalyzerException {
        checkConditionalBranches(TypeAnalyzerTestMethods.booleanArray(), true);
    }

    @Test
    public void testIntArray() throws AnalyzerException {
        checkConditionalBranches(TypeAnalyzerTestMethods.intArray(), false);

    }

    @Test
    public void testBooleanConstant() throws AnalyzerException {
        checkConditionalBranches(TypeAnalyzerTestMethods.booleanConstant(), true);
    }

    @Test
    public void testIntConstant() throws AnalyzerException {
        checkConditionalBranches(TypeAnalyzerTestMethods.intConstant(), false);

    }

    @Test
    public void testBooleanUnaryOperation() throws AnalyzerException {
        checkConditionalBranches(TypeAnalyzerTestMethods.booleanUnaryOperation(), true);
    }

    @Test
    public void testIntUnaryOperation() throws AnalyzerException {
        checkConditionalBranches(TypeAnalyzerTestMethods.intUnaryOperation(), false);

    }

    @Test
    public void testBooleanBinaryOperation() throws AnalyzerException {
        checkConditionalBranches(TypeAnalyzerTestMethods.booleanBinaryOperation(), true);
    }

    @Test
    public void testIntBinaryOperation() throws AnalyzerException {
        checkConditionalBranches(TypeAnalyzerTestMethods.intBinaryOperation(), false);
    }

    @Test
    public void testSingleBitCheck() throws AnalyzerException {
        checkConditionalBranches(TypeAnalyzerTestMethods.singleBitCheck(), true);
    }

    @Test
    public void testSingleBitCheckArray() throws AnalyzerException {
        checkConditionalBranches(TypeAnalyzerTestMethods.singleBitCheckArray(), true);
    }

    private static void checkConditionalBranches(MethodNode mn, boolean expected) throws AnalyzerException {
        TypeAnalyzer analyzer = new TypeAnalyzer(new SSAMethod(TypeAnalyzerTestMethods.OWNER, mn), true);
        List<AbstractInsnNode> conditionalBranches = ControlAnalysisTestUtil.filterInstructions(mn,
                (i) -> i.getOpcode() == IFEQ || i.getOpcode() == IFNE);
        for(AbstractInsnNode insn : conditionalBranches) {
            assertSame(expected, analyzer.isDoubleBindingBranch(insn));
        }
    }
}
