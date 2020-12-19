package edu.neu.ccs.conflux.internal.policy.binding.tracer;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Arrays;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.LinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.neu.ccs.conflux.internal.policy.ssa.SSAMethod;
import edu.neu.ccs.conflux.internal.policy.binding.LoopLevel;
import edu.neu.ccs.conflux.internal.policy.binding.LoopLevel.DependentLoopLevel;
import edu.neu.ccs.conflux.internal.policy.binding.LoopLevel.VariantLoopLevel;
import org.junit.Test;

import java.util.Iterator;
import java.util.function.Predicate;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;
import static edu.neu.ccs.conflux.internal.policy.binding.LoopLevel.StableLoopLevel.STABLE_LOOP_LEVEL;
import static edu.neu.ccs.conflux.internal.policy.binding.tracer.LoopLevelTracerTestMethods.OWNER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoopLevelTracerTest {

    @Test
    public void testAllLocalAssignmentsConstant() throws Exception {
        checkAllStoresConstant(LoopLevelTracerTestMethods.allLocalAssignmentsConstant());
    }

    @Test
    public void testAllLocalAssignmentsConstant2() throws Exception {
        checkAllStoresConstant(LoopLevelTracerTestMethods.allLocalAssignmentsConstant2());
    }

    @Test
    public void testAllLocalAssignmentsConstant3() throws Exception {
        checkAllStoresConstant(LoopLevelTracerTestMethods.allLocalAssignmentsConstant3());
    }

    @Test
    public void testArgDependentAssignment() throws Exception {
        MethodNode mn = LoopLevelTracerTestMethods.argDependentAssignment();
        List<LoopLevel> expected = Arrays.asList(
                new DependentLoopLevel(new int[]{1}),
                new DependentLoopLevel(new int[]{0}),
                new DependentLoopLevel(new int[]{2}),
                new DependentLoopLevel(new int[]{0, 1})
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), mn));
    }

    @Test
    public void testArgDependentBranching() throws Exception {
        MethodNode mn = LoopLevelTracerTestMethods.argDependentBranching();
        List<LoopLevel> expected = Arrays.asList(
                STABLE_LOOP_LEVEL,
                STABLE_LOOP_LEVEL,
                STABLE_LOOP_LEVEL,
                STABLE_LOOP_LEVEL,
                new VariantLoopLevel(0),
                STABLE_LOOP_LEVEL,
                new VariantLoopLevel(0)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), mn));
    }

    @Test
    public void testLocalSelfComputation() throws Exception {
        MethodNode mn = LoopLevelTracerTestMethods.localSelfComputation();
        List<LoopLevel> expected = Arrays.asList(
                STABLE_LOOP_LEVEL,
                STABLE_LOOP_LEVEL
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), mn));
    }

    @Test
    public void testArraySelfComputation() throws Exception {
        MethodNode mn = LoopLevelTracerTestMethods.arraySelfComputation();
        List<LoopLevel> expected = Arrays.asList(
                STABLE_LOOP_LEVEL,
                new DependentLoopLevel(new int[]{0})
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), mn));
    }

    @Test
    public void testMultiArraySelfComputation() throws Exception {
        MethodNode mn = LoopLevelTracerTestMethods.multiArraySelfComputation();
        List<LoopLevel> expected = Arrays.asList(
                STABLE_LOOP_LEVEL,
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), mn));
    }

    @Test
    public void testFieldSelfComputation() throws Exception {
        MethodNode mn = LoopLevelTracerTestMethods.fieldSelfComputation();
        List<LoopLevel> expected = Arrays.asList(
                STABLE_LOOP_LEVEL,
                new DependentLoopLevel(new int[]{0})
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), mn));
    }

    @Test
    public void testArrayFieldSelfComputation() throws Exception {
        MethodNode mn = LoopLevelTracerTestMethods.arrayFieldSelfComputation();
        List<LoopLevel> expected = Arrays.asList(
                STABLE_LOOP_LEVEL,
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), mn));
    }

    @Test
    public void testLocalAssignedVariantValue() throws Exception {
        MethodNode mn = LoopLevelTracerTestMethods.localAssignedVariantValue();
        List<LoopLevel> expected = Arrays.asList(
                STABLE_LOOP_LEVEL,
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), mn));
    }

    @Test
    public void testArrayAssignedVariantValue() throws Exception {
        MethodNode mn = LoopLevelTracerTestMethods.arrayAssignedVariantValue();
        List<LoopLevel> expected = Arrays.asList(
                STABLE_LOOP_LEVEL,
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), mn));
    }

    @Test
    public void testMultiArrayAssignedVariantValue() throws Exception {
        MethodNode mn = LoopLevelTracerTestMethods.multiArrayAssignedVariantValue();
        List<LoopLevel> expected = Arrays.asList(
                STABLE_LOOP_LEVEL,
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), mn));
    }

    @Test
    public void testFieldAssignedVariantValue() throws Exception {
        MethodNode mn = LoopLevelTracerTestMethods.fieldAssignedVariantValue();
        List<LoopLevel> expected = Arrays.asList(
                STABLE_LOOP_LEVEL,
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), mn));
    }

    @Test
    public void testArrayFieldAssignedVariantValue() throws Exception {
        MethodNode mn = LoopLevelTracerTestMethods.arrayFieldAssignedVariantValue();
        List<LoopLevel> expected = Arrays.asList(
                STABLE_LOOP_LEVEL,
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), mn));
    }

    @Test
    public void testVariantArray() throws Exception {
        MethodNode mn = LoopLevelTracerTestMethods.variantArray();
        List<LoopLevel> expected = Arrays.asList(
                STABLE_LOOP_LEVEL,
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), mn));
    }

    @Test
    public void testVariantArray2() throws Exception {
        MethodNode mn = LoopLevelTracerTestMethods.variantArray2();
        List<LoopLevel> expected = Arrays.asList(
                new VariantLoopLevel(0),
                STABLE_LOOP_LEVEL,
                new VariantLoopLevel(1),
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), mn));
    }

    @Test
    public void testTwoArrays() throws Exception {
        MethodNode mn = LoopLevelTracerTestMethods.twoArrays();
        List<LoopLevel> expected = Arrays.asList(
                STABLE_LOOP_LEVEL,
                new DependentLoopLevel(new int[]{1}),
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), mn));
    }

    @Test
    public void testArrayAliasing() throws Exception {
        MethodNode mn = LoopLevelTracerTestMethods.arrayAliasing();
        List<LoopLevel> expected = Arrays.asList(
                new DependentLoopLevel(new int[]{0}),
                new DependentLoopLevel(new int[]{0})
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), mn));
    }

    @Test
    public void testArrayAliasingVariant() throws Exception {
        MethodNode mn = LoopLevelTracerTestMethods.arrayAliasingVariant();
        List<LoopLevel> expected = Arrays.asList(
                new DependentLoopLevel(new int[]{0}),
                new VariantLoopLevel(0)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), mn));
    }

    @Test
    public void testArrayElementRedefined() throws Exception {
        MethodNode mn = LoopLevelTracerTestMethods.arrayElementRedefined();
        List<LoopLevel> expected = Arrays.asList(
                new VariantLoopLevel(0),
                new DependentLoopLevel(new int[]{0}),
                new VariantLoopLevel(0)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), mn));
    }

    @Test
    public void testMethodCallBetweenUses() throws Exception {
        MethodNode mn = LoopLevelTracerTestMethods.methodCallBetweenUses();
        List<LoopLevel> expected = Arrays.asList(
                new VariantLoopLevel(0),
                new VariantLoopLevel(0)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), mn));
    }

    @Test
    public void testIndexOf() throws Exception {
        MethodNode mn = LoopLevelTracerTestMethods.indexOf();
        List<LoopLevel> expected = Arrays.asList(
                STABLE_LOOP_LEVEL,
                new VariantLoopLevel(0),
                STABLE_LOOP_LEVEL,
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), mn));
    }

    @Test
    public void testIndexOfBreak() throws Exception {
        MethodNode mn = LoopLevelTracerTestMethods.indexOfBreak();
        List<LoopLevel> expected = Arrays.asList(
                STABLE_LOOP_LEVEL,
                new VariantLoopLevel(0),
                STABLE_LOOP_LEVEL,
                new VariantLoopLevel(0)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), mn));
    }

    private static List<LoopLevel> getLoopLevels(List<AbstractInsnNode> instructions, MethodNode methodNode) throws AnalyzerException {
        LoopLevelTracer tracer = new LoopLevelTracer(new SSAMethod(OWNER, methodNode));
        List<LoopLevel> levels = new LinkedList<>();
        for(AbstractInsnNode insn : instructions) {
            levels.add(tracer.getLoopLevel(insn));
        }
        return levels;
    }

    private static List<AbstractInsnNode> getStoreInstructions(MethodNode mn) {
        Predicate<AbstractInsnNode> filter = (insn) -> {
            switch(insn.getOpcode()) {
                case ISTORE:
                case LSTORE:
                case FSTORE:
                case DSTORE:
                case ASTORE:
                case IASTORE:
                case LASTORE:
                case FASTORE:
                case DASTORE:
                case AASTORE:
                case BASTORE:
                case CASTORE:
                case SASTORE:
                case PUTSTATIC:
                case PUTFIELD:
                    return true;
                default:
                    return false;
            }
        };
        return filterInstructions(mn, filter);
    }

    private static List<AbstractInsnNode> filterInstructions(MethodNode mn, Predicate<AbstractInsnNode> filter) {
        LinkedList<AbstractInsnNode> stores = new LinkedList<>();
        Iterator<AbstractInsnNode> itr = mn.instructions.iterator();
        while(itr.hasNext()) {
            AbstractInsnNode insn = itr.next();
            if(filter.test(insn)) {
                stores.add(insn);
            }
        }
        return stores;
    }

    private static void checkAllStoresConstant(MethodNode mn) throws Exception {
        List<LoopLevel> loopLevels = getLoopLevels(getStoreInstructions(mn), mn);
        Iterator<LoopLevel> itr = loopLevels.iterator();
        for(AbstractInsnNode insn : getStoreInstructions(mn)) {
            assertTrue("Expected instruction to be at constant loop level:" + insn, itr.next() instanceof LoopLevel.StableLoopLevel);
        }
    }
}