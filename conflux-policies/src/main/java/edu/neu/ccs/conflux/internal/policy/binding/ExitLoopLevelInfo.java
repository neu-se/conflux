package edu.neu.ccs.conflux.internal.policy.binding;

import edu.columbia.cs.psl.phosphor.PhosphorInstructionInfo;

public final class ExitLoopLevelInfo implements PhosphorInstructionInfo {

    private final int levelOffset;

    ExitLoopLevelInfo(int levelOffset) {
        this.levelOffset = levelOffset;
    }

    public int getLevelOffset() {
        return levelOffset;
    }
}
