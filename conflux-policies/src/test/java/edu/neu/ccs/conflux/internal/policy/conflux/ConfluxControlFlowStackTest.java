package edu.neu.ccs.conflux.internal.policy.conflux;

import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Arrays;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConfluxControlFlowStackTest {

    @Test
    public void testCopyTagEmptyStack() {
        ConfluxControlFlowStack<Object> ctrl = new ConfluxControlFlowStack<>();
        assertNullOrEmpty(ctrl.copyTagStable());
        assertNullOrEmpty(ctrl.copyTagUnstable(3));
    }

    @Test
    public void testCopyTagDifferentLevels() {
        ConfluxControlFlowStack<Object> ctrl = new ConfluxControlFlowStack<>();
        ctrl.setNextBranchTag(Taint.withLabel(0));
        ctrl.pushStable(0, 3);
        ctrl.setNextBranchTag(Taint.withLabel(1));
        ctrl.pushUnstable(1, 3, 1);
        ctrl.setNextBranchTag(Taint.withLabel(2));
        ctrl.pushStable(2, 3);
        assertContainsLabels(ctrl.copyTagStable(), 0, 2);
        assertContainsLabels(ctrl.copyTagUnstable(1), 0, 1, 2);
    }

    @Test
    public void testExitLoop() {
        ConfluxControlFlowStack<Object> ctrl = new ConfluxControlFlowStack<>();
        ctrl.setNextBranchTag(Taint.withLabel(0));
        ctrl.pushStable(0, 3);
        ctrl.setNextBranchTag(Taint.withLabel(1));
        ctrl.pushUnstable(1, 3, 1);
        ctrl.exitLoopLevel(1);
        assertContainsLabels(ctrl.copyTagUnstable(1), 0);
    }

    @Test
    public void testPushFramePushTag() {
        ConfluxControlFlowStack<Object> ctrl = new ConfluxControlFlowStack<>();
        ctrl.setNextBranchTag(Taint.withLabel(0));
        ctrl.pushStable(0, 1);
        ctrl.startFrame(0, 0).pushFrame();
        ctrl.setNextBranchTag(Taint.withLabel(1));
        ctrl.pushStable(0, 1);
        assertContainsLabels(ctrl.copyTagStable(), 0, 1);
    }

    @Test
    public void testPushFramePushPopTag() {
        ConfluxControlFlowStack<Object> ctrl = new ConfluxControlFlowStack<>();
        ctrl.setNextBranchTag(Taint.withLabel(0));
        ctrl.pushStable(0, 1);
        ctrl.startFrame(0, 0).pushFrame();
        ctrl.setNextBranchTag(Taint.withLabel(1));
        ctrl.pushStable(0, 1);
        ctrl.pop(0);
        assertContainsLabels(ctrl.copyTagStable(), 0);
    }

    @Test
    public void testPushFrameOffset() {
        ConfluxControlFlowStack<Object> ctrl = new ConfluxControlFlowStack<>();
        ctrl.setNextBranchTag(Taint.withLabel(0));
        ctrl.pushStable(0, 1);
        ctrl.startFrame(1, 0).pushFrame();
        ctrl.setNextBranchTag(Taint.withLabel(1));
        ctrl.pushUnstable(0, 1, 0);
        assertContainsLabels(ctrl.copyTagStable(), 0);
        assertContainsLabels(ctrl.copyTagUnstable(1), 0, 1);
    }

    @Test
    public void testPopFrame() {
        ConfluxControlFlowStack<Object> ctrl = new ConfluxControlFlowStack<>();
        ctrl.setNextBranchTag(Taint.withLabel(0));
        ctrl.pushStable(0, 1);
        ctrl.startFrame(1, 0).pushFrame();
        ctrl.setNextBranchTag(Taint.withLabel(1));
        ctrl.pushStable(0, 1);
        ctrl.popFrame();
        assertContainsLabels(ctrl.copyTagStable(), 0);
    }

    @Test
    public void testReset() {
        ConfluxControlFlowStack<Object> ctrl = new ConfluxControlFlowStack<>();
        ctrl.setNextBranchTag(Taint.withLabel(0));
        ctrl.pushStable(0, 1);
        ctrl.startFrame(1, 0).pushFrame();
        ctrl.setNextBranchTag(Taint.withLabel(1));
        ctrl.pushStable(0, 1);
        ctrl.startFrame(0, 0).pushFrame();
        ctrl.setNextBranchTag(Taint.withLabel(2));
        ctrl.pushStable(0, 1);
        ctrl.reset();
        assertNullOrEmpty(ctrl.copyTagStable());
        ctrl.popFrame();
        assertNullOrEmpty(ctrl.copyTagStable());
        ctrl.popFrame();
        assertNullOrEmpty(ctrl.copyTagStable());
    }

    @Test
    public void testDependentCopy() {
        ConfluxControlFlowStack<Object> ctrl = new ConfluxControlFlowStack<>();
        ctrl.startFrame(2, 2).setNextFrameArgStable().setNextFrameArgUnstable(1).pushFrame();
    }

    @Test
    public void testCopyDependent() {
        ConfluxControlFlowStack<Object> ctrl = new ConfluxControlFlowStack<>();
        // Prepare frame for a call to method with 3 arguments that is within one loop
        ctrl.startFrame(1, 3)
                .setNextFrameArgDependent(new int[]{0})
                .setNextFrameArgStable()
                .setNextFrameArgUnstable(1);
        ctrl.pushFrame();
        ctrl.setNextBranchTag(Taint.withLabel(0));
        ctrl.pushUnstable(1, 3, 0);
        assertNullOrEmpty(ctrl.copyTagDependent(new int[]{0}));
        assertNullOrEmpty(ctrl.copyTagDependent(new int[]{1}));
        assertContainsLabels(ctrl.copyTagDependent(new int[]{2}), 0);
    }

    @Test
    public void testPushDependent() {
        ConfluxControlFlowStack<Object> ctrl = new ConfluxControlFlowStack<>();
        // Prepare frame for a call to method with 1 argument that is within one loop
        ctrl.startFrame(1, 3)
                .setNextFrameArgUnstable(1);//  unstable with respect to all containing loops
        ctrl.pushFrame();
        ctrl.setNextBranchTag(Taint.withLabel(0));
        ctrl.pushDependent(0, 2, new int[]{0});
        assertNullOrEmpty(ctrl.copyTagStable());
        assertContainsLabels(ctrl.copyTagUnstable(0), 0);
    }

    public static void assertContainsLabels(Taint<Object> tag, Object... labels) {
        assertNotNull(tag);
        Set<Object> expected = new HashSet<>(Arrays.asList(labels));
        Set<Object> actual = new HashSet<>(Arrays.asList(tag.getLabels()));
        assertEquals(expected, actual);
    }

    public static void assertNullOrEmpty(Taint<?> taint) {
        if (taint != null && !taint.isEmpty()) {
            fail("Expected null taint. Got: " + taint);
        }
    }
}
