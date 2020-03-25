package edu.gmu.swe.phosphor.ignored.control.binding;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Arrays;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.LinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.binding.LoopLevel.DependentLoopLevel;
import edu.gmu.swe.phosphor.ignored.control.binding.LoopLevel.VariantLoopLevel;
import org.junit.Test;

import java.util.Iterator;
import java.util.function.Predicate;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;
import static edu.gmu.swe.phosphor.ignored.control.binding.LoopLevel.ConstantLoopLevel.CONSTANT_LOOP_LEVEL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoopLevelTracerConstancyTest {

    @Test
    public void testAllLocalAssignmentsConstant() throws Exception {
        checkAllStoresConstant(LoopLevelTracerConstancyTestMethods.allLocalAssignmentsConstant());
    }

    @Test
    public void testAllLocalAssignmentsConstant2() throws Exception {
        checkAllStoresConstant(LoopLevelTracerConstancyTestMethods.allLocalAssignmentsConstant2());
    }

    @Test
    public void testAllLocalAssignmentsConstant3() throws Exception {
        checkAllStoresConstant(LoopLevelTracerConstancyTestMethods.allLocalAssignmentsConstant3());
    }

    @Test
    public void testArgDependentAssignment() throws Exception {
        MethodNode mn = LoopLevelTracerConstancyTestMethods.argDependentAssignment();
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        List<LoopLevel> expected = Arrays.asList(
                new DependentLoopLevel(new int[]{1}),
                new DependentLoopLevel(new int[]{0}),
                new DependentLoopLevel(new int[]{2}),
                new DependentLoopLevel(new int[]{0, 1})
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), loopLevelMap));
    }

    @Test
    public void testArgDependentBranching() throws Exception {
        MethodNode mn = LoopLevelTracerConstancyTestMethods.argDependentBranching();
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        List<LoopLevel> expected = Arrays.asList(
                CONSTANT_LOOP_LEVEL,
                CONSTANT_LOOP_LEVEL,
                CONSTANT_LOOP_LEVEL,
                CONSTANT_LOOP_LEVEL,
                new VariantLoopLevel(0),
                CONSTANT_LOOP_LEVEL,
                new VariantLoopLevel(0)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), loopLevelMap));
    }

    @Test
    public void testLocalSelfComputation() throws Exception {
        MethodNode mn = LoopLevelTracerConstancyTestMethods.localSelfComputation();
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        List<LoopLevel> expected = Arrays.asList(
                CONSTANT_LOOP_LEVEL,
                CONSTANT_LOOP_LEVEL
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), loopLevelMap));
    }

    @Test
    public void testArraySelfComputation() throws Exception {
        MethodNode mn = LoopLevelTracerConstancyTestMethods.arraySelfComputation();
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        List<LoopLevel> expected = Arrays.asList(
                CONSTANT_LOOP_LEVEL,
                new DependentLoopLevel(new int[]{0})
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), loopLevelMap));
    }

    @Test
    public void testMultiArraySelfComputation() throws Exception {
        MethodNode mn = LoopLevelTracerConstancyTestMethods.multiArraySelfComputation();
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        List<LoopLevel> expected = Arrays.asList(
                CONSTANT_LOOP_LEVEL,
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), loopLevelMap));
    }

    @Test
    public void testFieldSelfComputation() throws Exception {
        MethodNode mn = LoopLevelTracerConstancyTestMethods.fieldSelfComputation();
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        List<LoopLevel> expected = Arrays.asList(
                CONSTANT_LOOP_LEVEL,
                new DependentLoopLevel(new int[]{0})
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), loopLevelMap));
    }

    @Test
    public void testArrayFieldSelfComputation() throws Exception {
        MethodNode mn = LoopLevelTracerConstancyTestMethods.arrayFieldSelfComputation();
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        List<LoopLevel> expected = Arrays.asList(
                CONSTANT_LOOP_LEVEL,
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), loopLevelMap));
    }

    @Test
    public void testLocalAssignedVariantValue() throws Exception {
        MethodNode mn = LoopLevelTracerConstancyTestMethods.localAssignedVariantValue();
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        List<LoopLevel> expected = Arrays.asList(
                CONSTANT_LOOP_LEVEL,
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), loopLevelMap));
    }

    @Test
    public void testArrayAssignedVariantValue() throws Exception {
        MethodNode mn = LoopLevelTracerConstancyTestMethods.arrayAssignedVariantValue();
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        List<LoopLevel> expected = Arrays.asList(
                CONSTANT_LOOP_LEVEL,
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), loopLevelMap));
    }

    @Test
    public void testMultiArrayAssignedVariantValue() throws Exception {
        MethodNode mn = LoopLevelTracerConstancyTestMethods.multiArrayAssignedVariantValue();
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        List<LoopLevel> expected = Arrays.asList(
                CONSTANT_LOOP_LEVEL,
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), loopLevelMap));
    }

    @Test
    public void testFieldAssignedVariantValue() throws Exception {
        MethodNode mn = LoopLevelTracerConstancyTestMethods.fieldAssignedVariantValue();
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        List<LoopLevel> expected = Arrays.asList(
                CONSTANT_LOOP_LEVEL,
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), loopLevelMap));
    }

    @Test
    public void testArrayFieldAssignedVariantValue() throws Exception {
        MethodNode mn = LoopLevelTracerConstancyTestMethods.arrayFieldAssignedVariantValue();
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        List<LoopLevel> expected = Arrays.asList(
                CONSTANT_LOOP_LEVEL,
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), loopLevelMap));
    }

    @Test
    public void testVariantArray() throws Exception {
        MethodNode mn = LoopLevelTracerConstancyTestMethods.variantArray();
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        List<LoopLevel> expected = Arrays.asList(
                CONSTANT_LOOP_LEVEL,
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), loopLevelMap));
    }

    @Test
    public void testVariantArray2() throws Exception {
        MethodNode mn = LoopLevelTracerConstancyTestMethods.variantArray2();
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        List<LoopLevel> expected = Arrays.asList(
                new VariantLoopLevel(0),
                CONSTANT_LOOP_LEVEL,
                new VariantLoopLevel(1),
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), loopLevelMap));
    }

    @Test
    public void testTwoArrays() throws Exception {
        MethodNode mn = LoopLevelTracerConstancyTestMethods.twoArrays();
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        List<LoopLevel> expected = Arrays.asList(
                CONSTANT_LOOP_LEVEL,
                new DependentLoopLevel(new int[]{1}),
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), loopLevelMap));
    }

    @Test
    public void testArrayAliasing() throws Exception {
        MethodNode mn = LoopLevelTracerConstancyTestMethods.arrayAliasing();
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        List<LoopLevel> expected = Arrays.asList(
                new DependentLoopLevel(new int[]{0}),
                new DependentLoopLevel(new int[]{0})
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), loopLevelMap));
    }

    @Test
    public void testArrayAliasingVariant() throws Exception {
        MethodNode mn = LoopLevelTracerConstancyTestMethods.arrayAliasingVariant();
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        List<LoopLevel> expected = Arrays.asList(
                new DependentLoopLevel(new int[]{0}),
                new VariantLoopLevel(0)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), loopLevelMap));
    }

    @Test
    public void testArrayElementRedefined() throws Exception {
        MethodNode mn = LoopLevelTracerConstancyTestMethods.arrayElementRedefined();
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        List<LoopLevel> expected = Arrays.asList(
                new VariantLoopLevel(0),
                new DependentLoopLevel(new int[]{0}),
                new VariantLoopLevel(0)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), loopLevelMap));
    }

    @Test
    public void testMethodCallBetweenUses() throws Exception {
        MethodNode mn = LoopLevelTracerConstancyTestMethods.methodCallBetweenUses();
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        List<LoopLevel> expected = Arrays.asList(
                new VariantLoopLevel(0),
                new VariantLoopLevel(0)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), loopLevelMap));
    }

    @Test
    public void testIndexOf() throws Exception {
        MethodNode mn = LoopLevelTracerConstancyTestMethods.indexOf();
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        List<LoopLevel> expected = Arrays.asList(
                CONSTANT_LOOP_LEVEL,
                new VariantLoopLevel(0),
                CONSTANT_LOOP_LEVEL,
                new VariantLoopLevel(1)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), loopLevelMap));
    }

    @Test
    public void testIndexOfBreak() throws Exception {
        MethodNode mn = LoopLevelTracerConstancyTestMethods.indexOfBreak();
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        List<LoopLevel> expected = Arrays.asList(
                CONSTANT_LOOP_LEVEL,
                new VariantLoopLevel(0),
                CONSTANT_LOOP_LEVEL,
                new VariantLoopLevel(0)
        );
        assertEquals(expected, getLoopLevels(getStoreInstructions(mn), loopLevelMap));
    }

    private static List<LoopLevel> getLoopLevels(List<AbstractInsnNode> instructions, Map<AbstractInsnNode, LoopLevel> loopLevelMap) {
        List<LoopLevel> levels = new LinkedList<>();
        for(AbstractInsnNode insn : instructions) {
            levels.add(loopLevelMap.get(insn));
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
        Map<AbstractInsnNode, LoopLevel> loopLevelMap = calculateLoopLevelMap(mn);
        for(AbstractInsnNode insn : getStoreInstructions(mn)) {
            assertTrue("Expected instruction to be at constant loop level:" + insn, loopLevelMap.get(insn) instanceof LoopLevel.ConstantLoopLevel);
        }
    }

    public static Map<AbstractInsnNode, LoopLevel> calculateLoopLevelMap(MethodNode methodNode) throws AnalyzerException {
        return new LoopLevelTracer(LoopLevelTracerConstancyTestMethods.OWNER, methodNode).getLoopLevelMap();
    }
}