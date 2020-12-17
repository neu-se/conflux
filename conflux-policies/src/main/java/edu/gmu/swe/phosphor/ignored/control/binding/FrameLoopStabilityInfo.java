package edu.gmu.swe.phosphor.ignored.control.binding;

import edu.columbia.cs.psl.phosphor.PhosphorInstructionInfo;
import edu.columbia.cs.psl.phosphor.struct.SinglyLinkedList;

import java.util.Iterator;

public class FrameLoopStabilityInfo implements PhosphorInstructionInfo {

    private final int invocationLevel;
    private final SinglyLinkedList<LoopLevel> argumentLevels = new SinglyLinkedList<>();

    public FrameLoopStabilityInfo(int invocationLevel) {
        this.invocationLevel = invocationLevel;
    }

    public int getInvocationLevel() {
        return invocationLevel;
    }

    public int getNumArguments() {
        return argumentLevels.size();
    }

    public void addLastArgumentLevel(LoopLevel level) {
        argumentLevels.addLast(level);
    }

    public Iterator<LoopLevel> getLevelIterator() {
        return argumentLevels.iterator();
    }
}
