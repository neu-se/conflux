package edu.neu.ccs.conflux.policy.conflux;

import edu.columbia.cs.psl.phosphor.runtime.MultiTainter;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.neu.ccs.conflux.policy.BasePolicyTest;
import org.junit.Test;

import java.util.LinkedList;

public class LoopConfluxITCase extends BasePolicyTest {

    @Test
    public void testLoopingVarConditionallyUpdated() {
        char[] c = GeneralConfluxITCase.createDigitArray();
        LinkedList<Character> digits = new LinkedList<>();
        for(int i = 0; i < c.length; i++) {
            digits.add(c[i]);
            if(c[i] == '0') {
                // Skip value after zero
                i++;
            }
        }
        for(char digit : digits) {
            Taint<?> tag = MultiTainter.getTaint(digit);
            assertNonNullTaint(tag);
            assertTaintHasOnlyLabels(tag, Integer.parseInt("" + digit));
        }
    }

    @Test
    public void testCountZeros() {
        int z = 0;
        int[] a = new int[5];
        a[4] = 1;
        BasePolicyTest.taintWithIndices(a);
        for(int i : a) {
            if(i == 0) {
                z++;
            }
        }
        Taint<?> tag = MultiTainter.getTaint(z);
        assertNullOrEmpty(tag);
    }

    @Test
    public void testIndexOfNoBreak() {
        int z = 0;
        int[] a = new int[5];
        BasePolicyTest.taintWithIndices(a);
        for(int i = 0; i < a.length; i++) {
            if(a[i] == 0) {
                z = i;
            }
        }
        Taint<?> tag = MultiTainter.getTaint(z);
        assertNonNullTaint(tag);
        assertTaintHasOnlyLabels(tag, 4);
    }

    @Test
    public void testContainsNoBreak() {
        boolean contains = false;
        int[] a = new int[5];
        BasePolicyTest.taintWithIndices(a);
        for(int value : a) {
            if(value == 0) {
                contains = true;
            }
        }
        Taint<?> tag = MultiTainter.getTaint(contains);
        assertNullOrEmpty(tag);
    }

    @Test
    public void testContainsBreak() {
        boolean contains = false;
        int[] a = new int[5];
        BasePolicyTest.taintWithIndices(a);
        for(int value : a) {
            if(value == 0) {
                contains = true;
                break;
            }
        }
        Taint<?> tag = MultiTainter.getTaint(contains);
        assertNullOrEmpty(tag);
    }

    private int partialControlExclusion(boolean condition, int[] a) {
        int result = 0;
        if(condition) {
            for(int value : a) {
                if(value == 0) {
                    result = 7;
                    break;
                }
            }
        }
        return result;
    }

    @Test
    public void testPartialControlExclusionSameMethod() {
        boolean outerCondition = MultiTainter.taintedBoolean(true, "outerCondition");
        int[] a = new int[5];
        BasePolicyTest.taintWithIndices(a);
        int result = partialControlExclusion(outerCondition, a);
        Taint<?> tag = MultiTainter.getTaint(result);
        assertNonNullTaint(tag);
        assertTaintHasOnlyLabels(tag, "outerCondition");
    }

    @Test
    public void testPartialControlExclusionDifferentMethod() {
        boolean outerCondition = MultiTainter.taintedBoolean(true, "outerCondition");
        int[] a = new int[5];
        BasePolicyTest.taintWithIndices(a);
        int result = 5;
        if(outerCondition) {
            result = partialControlExclusion(true, a);
        }
        Taint<?> tag = MultiTainter.getTaint(result);
        assertNonNullTaint(tag);
        assertTaintHasOnlyLabels(tag, "outerCondition");
    }
}
