package edu.neu.ccs.conflux.internal.policy.conflux;

import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Arrays;

final class ControlFrame<E> {
    private static final int NOT_PUSHED = -1;
    private final int invocationLevel;
    private final int[] argumentStabilityLevels;
    private final LevelStacks<E> stacks;
    private final ControlFrame<E> next;
    private int[] branchLevels;

    ControlFrame(ControlFrame<E> other) {
        this.next = null;
        this.invocationLevel = other.invocationLevel;
        this.argumentStabilityLevels = other.argumentStabilityLevels;
        this.stacks = new LevelStacks<>(other.stacks);
    }

    ControlFrame(int invocationLevel, int[] argumentStabilityLevels, ControlFrame<E> next) {
        this.next = next;
        this.invocationLevel = invocationLevel;
        this.argumentStabilityLevels = argumentStabilityLevels;
        this.stacks = next == null ? new LevelStacks<>() : new LevelStacks<>(next.stacks);
    }

    ControlFrame<E> getNext() {
        return next;
    }

    int getLevel(int levelOffset) {
        return invocationLevel + levelOffset;
    }

    int getLevel(int[] dependencies) {
        if (argumentStabilityLevels == null) {
            return 0;
        } else {
            int max = 0;
            for (int dependency : dependencies) {
                int value = argumentStabilityLevels[dependency];
                if (value > max) {
                    max = value;
                }
            }
            return max;
        }
    }

    Taint<E> copyTag(int level) {
        return stacks.getTagUnderLevel(level);
    }

    void push(Taint<E> tag, int branchID, int branchesSize, int level) {
        if (tag != null && !tag.isEmpty()) {
            if (branchLevels == null) {
                branchLevels = new int[branchesSize];
                Arrays.fill(branchLevels, NOT_PUSHED);
            }
            if (branchLevels[branchID] == NOT_PUSHED) {
                branchLevels[branchID] = level;
                stacks.push(level, tag);
            } else {
                stacks.union(level, tag);
            }
        }
    }

    void pop(int branchID) {
        if (branchLevels != null && branchLevels[branchID] != NOT_PUSHED) {
            stacks.pop(branchLevels[branchID]);
            branchLevels[branchID] = NOT_PUSHED;
        }
    }

    void reset() {
        if (next != null) {
            next.reset();
        }
        branchLevels = null;
        stacks.clear();
    }

    void exitLoopLevel(int levelOffset) {
        if (branchLevels != null) {
            int level = getLevel(levelOffset);
            stacks.remove(level);
            for (int i = 0; i < branchLevels.length; i++) {
                if (branchLevels[i] == level) {
                    branchLevels[i] = NOT_PUSHED;
                }
            }
        }
    }
}
