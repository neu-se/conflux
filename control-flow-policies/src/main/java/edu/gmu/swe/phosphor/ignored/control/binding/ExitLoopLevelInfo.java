package edu.gmu.swe.phosphor.ignored.control.binding;

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
