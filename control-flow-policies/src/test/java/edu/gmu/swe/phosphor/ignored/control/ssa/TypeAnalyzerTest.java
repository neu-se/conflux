package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ControlAnalysisTestUtil;
import org.junit.Test;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.IFEQ;
import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.IFNE;
import static edu.gmu.swe.phosphor.ignored.control.ssa.TypeAnalyzerTestMethods.*;
import static junit.framework.TestCase.assertSame;

public class TypeAnalyzerTest {

    @Test
    public void testBooleanReturnValue() throws AnalyzerException {
        checkConditionalBranches(booleanReturnValue(), true);
    }

    @Test
    public void testIntReturnValue() throws AnalyzerException {
        checkConditionalBranches(intReturnValue(), false);

    }

    @Test
    public void testBooleanParam() throws AnalyzerException {
        checkConditionalBranches(booleanParam(), true);
    }

    @Test
    public void testIntParam() throws AnalyzerException {
        checkConditionalBranches(intParam(), false);

    }

    @Test
    public void testBooleanField() throws AnalyzerException {
        checkConditionalBranches(booleanField(), true);
    }

    @Test
    public void testIntField() throws AnalyzerException {
        checkConditionalBranches(intField(), false);

    }

    @Test
    public void testBooleanArray() throws AnalyzerException {
        checkConditionalBranches(booleanArray(), true);
    }

    @Test
    public void testIntArray() throws AnalyzerException {
        checkConditionalBranches(intArray(), false);

    }

    @Test
    public void testBooleanConstant() throws AnalyzerException {
        checkConditionalBranches(booleanConstant(), true);
    }

    @Test
    public void testIntConstant() throws AnalyzerException {
        checkConditionalBranches(intConstant(), false);

    }

    @Test
    public void testBooleanUnaryOperation() throws AnalyzerException {
        checkConditionalBranches(booleanUnaryOperation(), true);
    }

    @Test
    public void testIntUnaryOperation() throws AnalyzerException {
        checkConditionalBranches(intUnaryOperation(), false);

    }

    @Test
    public void testBooleanBinaryOperation() throws AnalyzerException {
        checkConditionalBranches(booleanBinaryOperation(), true);
    }

    @Test
    public void testIntBinaryOperation() throws AnalyzerException {
        checkConditionalBranches(intBinaryOperation(), false);

    }

    private static void checkConditionalBranches(MethodNode mn, boolean expected) throws AnalyzerException {
        TypeAnalyzer analyzer = new TypeAnalyzer(new SSAMethod(OWNER, mn));
        List<AbstractInsnNode> conditionalBranches = ControlAnalysisTestUtil.filterInstructions(mn,
                (i) -> i.getOpcode() == IFEQ || i.getOpcode() == IFNE);
        for(AbstractInsnNode insn : conditionalBranches) {
            assertSame(expected, analyzer.isDoubleBindingBranch(insn));
        }
    }
}
