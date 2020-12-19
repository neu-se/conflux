package edu.neu.ccs.conflux.internal.policy;

import edu.columbia.cs.psl.phosphor.control.graph.BasicBlock;
import edu.columbia.cs.psl.phosphor.control.graph.DummyBasicBlock;
import edu.columbia.cs.psl.phosphor.control.standard.BranchEnd;
import edu.columbia.cs.psl.phosphor.control.standard.BranchStart;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.*;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.GOTO;

public class BranchEdge extends DummyBasicBlock {

    /**
     * The source vertex of this edge.
     */
    private final BasicBlock source;

    /**
     * The target vertex of this edge.
     */
    private final BasicBlock target;

    /**
     * True if this edge is traversed as a result of a jump occurring
     */
    private final boolean branchTaken;

    /**
     * The basic block at whose start this branch edge's scope ends
     */
    private final Set<BasicBlock> scopeEnds = new HashSet<>();

    /**
     * Identifier assigned to this branch
     */
    private int branchId = -1;

    public BranchEdge(BasicBlock source, BasicBlock target, boolean branchTaken) {
        if(source == null || target == null) {
            throw new NullPointerException();
        }
        this.source = source;
        this.target = target;
        this.branchTaken = branchTaken;
    }

    public BasicBlock getSource() {
        return source;
    }

    public BasicBlock getTarget() {
        return target;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public void addScopeEnd(BasicBlock scopeEnd) {
        scopeEnds.add(scopeEnd);
    }

    public AbstractInsnNode createScopeStartNode() {
        return new LdcInsnNode(new BranchStart(branchId));
    }

    /**
     * Marks the end/s of the scope of this branch edge's scope by inserting BranchEnd nodes into the specified
     * instruction list.
     */
    public void markBranchEnds(InsnList instructions) {
        for(BasicBlock scopeEnd : scopeEnds) {
            if(!(scopeEnd instanceof DummyBasicBlock)) {
                AbstractInsnNode insn = FlowGraphUtil.findNextPrecedableInstruction(scopeEnd.getFirstInsn());
                instructions.insertBefore(insn, new LdcInsnNode(new BranchEnd(branchId)));
            }
        }
    }

    /**
     * Marks the start of the scope of this branch edge's scope by inserting scope start nodes into the specified
     * instruction list.
     */
    public void markBranchStart(InsnList instructions) {
        AbstractInsnNode scopeStartNode = createScopeStartNode();
        AbstractInsnNode targetInsn = target.getFirstInsn();
        if(branchTaken) {
            // Adjust the control flow
            FrameNode targetFrame = findNextFrame(targetInsn);
            LabelNode newTarget = new LabelNode(new Label());
            instructions.add(newTarget);
            FrameNode targetFrameCopy = new FrameNode(targetFrame.type, targetFrame.local.size(), targetFrame.local.toArray(),
                    targetFrame.stack.size(), targetFrame.stack.toArray());
            instructions.add(targetFrameCopy);
            instructions.add(scopeStartNode);
            instructions.add(new JumpInsnNode(GOTO, ((LabelNode) targetInsn)));
            // Swap the target label of the jump/switch instruction at the source
            swapLabel(source.getLastInsn(), ((LabelNode) targetInsn), newTarget);
        } else {
            instructions.insertBefore(FlowGraphUtil.findNextPrecedableInstruction(targetInsn), scopeStartNode);
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof BranchEdge)) {
            return false;
        }
        BranchEdge that = (BranchEdge) o;
        return branchTaken == that.branchTaken && source.equals(that.source) && target.equals(that.target);
    }

    @Override
    public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + target.hashCode();
        result = 31 * result + (branchTaken ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("<BranchEdge: %s -> %s>", source, target);
    }

    private static void swapLabel(AbstractInsnNode insn, LabelNode originalTarget, LabelNode newTarget) {
        if(insn instanceof JumpInsnNode) {
            JumpInsnNode jump = (JumpInsnNode) insn;
            if(jump.label == originalTarget) {
                jump.label = newTarget;
            } else {
                throw new IllegalArgumentException();
            }
        } else if(insn instanceof LookupSwitchInsnNode) {
            LookupSwitchInsnNode switchInsn = (LookupSwitchInsnNode) insn;
            boolean found = false;
            if(switchInsn.dflt == originalTarget) {
                switchInsn.dflt = newTarget;
                found = true;
            }
            java.util.List<LabelNode> newLabels = new java.util.LinkedList<>();
            for(LabelNode label : switchInsn.labels) {
                if(label == originalTarget) {
                    found = true;
                    newLabels.add(newTarget);
                } else {
                    newLabels.add(label);
                }
            }
            switchInsn.labels = newLabels;
            if(!found) {
                throw new IllegalArgumentException();
            }
        } else if(insn instanceof TableSwitchInsnNode) {
            TableSwitchInsnNode switchInsn = (TableSwitchInsnNode) insn;
            boolean found = false;
            if(switchInsn.dflt == originalTarget) {
                switchInsn.dflt = newTarget;
                found = true;
            }
            java.util.List<LabelNode> newLabels = new java.util.LinkedList<>();
            for(LabelNode label : switchInsn.labels) {
                if(label == originalTarget) {
                    found = true;
                    newLabels.add(newTarget);
                } else {
                    newLabels.add(label);
                }
            }
            switchInsn.labels = newLabels;
            if(!found) {
                throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static FrameNode findNextFrame(AbstractInsnNode insn) {
        while(insn != null && !(insn instanceof FrameNode)) {
            insn = insn.getNext();
        }
        return (FrameNode) insn;
    }
}
