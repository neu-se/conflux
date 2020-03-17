package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.InsnNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;

public class AnnotatedBasicBlock {

    private static final InsnNode NOP_INSN = new InsnNode(Opcodes.NOP);

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
        StringBuilder builder = new StringBuilder();
        int x = 0;
        for(AnnotatedInstruction i : instructions) {
            String s = i.getRawStatementString();
            if(s.length() > 0) {
                builder.append(s);
                if(x != instructions.size() - 1) {
                    builder.append("\n");
                }
            }
            x++;
        }
        return builder.toString();
    }

    public String getProcessedStatementsString() {
        StringBuilder builder = new StringBuilder();
        int x = 0;
        for(AnnotatedInstruction i : instructions) {
            String s = i.getProcessedStatementString();
            if(s.length() > 0) {
                builder.append(s);
                if(x != instructions.size() - 1) {
                    builder.append("\n");
                }
            }
            x++;
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return getRawStatementsString();
    }

    public AbstractInsnNode getFirstInsn() {
        for(AnnotatedInstruction insn : instructions) {
            if(insn.getOriginalInstruction() != null) {
                return insn.getOriginalInstruction();
            }
        }
        return NOP_INSN;
    }

    public AbstractInsnNode getLastInsn() {
        for(int i = instructions.size() - 1; i >= 0; i--) {
            AnnotatedInstruction insn = instructions.get(i);
            if(insn.getOriginalInstruction() != null) {
                return insn.getOriginalInstruction();
            }
        }
        return NOP_INSN;
    }

    public static Collection<AnnotatedBasicBlock> sort(Collection<AnnotatedBasicBlock> blocks) {
        Comparator<AnnotatedBasicBlock> c = (b1, b2) -> Integer.compare(b1.getIndex(), b2.getIndex());
        List<AnnotatedBasicBlock> result = new LinkedList<>(blocks);
        Collections.sort(result, c);
        return result;
    }
}
