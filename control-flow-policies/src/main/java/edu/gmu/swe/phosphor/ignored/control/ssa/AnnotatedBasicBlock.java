package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;

public class AnnotatedBasicBlock {

    private final List<AnnotatedInstruction> instructions;
    private final int index;

    public AnnotatedBasicBlock(int index, List<AnnotatedInstruction> instructions) {
        this.index = index;
        this.instructions = Collections.unmodifiableList(new ArrayList<>(instructions));
    }

    public int getIndex() {
        return index;
    }

    public List<AnnotatedInstruction> getInstructions() {
        return instructions;
    }

    public String getRawStatementsString() {
        List<String> l = new LinkedList<>();
        for(AnnotatedInstruction i : instructions) {
            l.add(i.getRawStatementString());
        }
        return String.join("\n", l);
    }

    public String getProcessedStatementsString() {
        List<String> l = new LinkedList<>();
        for(AnnotatedInstruction i : instructions) {
            l.add(i.getProcessedStatementString());
        }
        return String.join("\n", l);
    }

    @Override
    public String toString() {
        return getRawStatementsString();
    }

    public static Collection<AnnotatedBasicBlock> sort(Collection<AnnotatedBasicBlock> blocks) {
        Comparator<AnnotatedBasicBlock> c = (b1, b2) -> Integer.compare(b1.getIndex(), b2.getIndex());
        List<AnnotatedBasicBlock> result = new LinkedList<>(blocks);
        Collections.sort(result, c);
        return result;
    }
}
