package edu.gmu.swe.phosphor.ignored.control.binding;

import edu.columbia.cs.psl.phosphor.PhosphorInstructionInfo;
import edu.columbia.cs.psl.phosphor.struct.SinglyLinkedList;

import java.util.Iterator;

public class FrameConstancyInfo implements PhosphorInstructionInfo {

    private final int invocationLevel;
    private final SinglyLinkedList<LoopLevel> argumentLevels = new SinglyLinkedList<>();

    public FrameConstancyInfo(int invocationLevel) {
        this.invocationLevel = invocationLevel;
    }

    public int getInvocationLevel() {
        return invocationLevel;
    }

    public int getNumArguments() {
        return argumentLevels.size();
    }

    public void pushArgumentLevel(LoopLevel level) {
        argumentLevels.push(level);
    }

    public Iterator<LoopLevel> getLevelIterator() {
        return argumentLevels.iterator();
    }
}
