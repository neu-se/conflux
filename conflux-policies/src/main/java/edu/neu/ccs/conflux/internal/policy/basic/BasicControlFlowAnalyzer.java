package edu.neu.ccs.conflux.internal.policy.basic;

import edu.columbia.cs.psl.phosphor.control.ControlFlowAnalyzer;
import edu.columbia.cs.psl.phosphor.control.graph.BaseControlFlowGraphCreator;
import edu.columbia.cs.psl.phosphor.control.graph.BasicBlock;
import edu.columbia.cs.psl.phosphor.control.graph.DummyBasicBlock;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.control.standard.BranchEnd;
import edu.columbia.cs.psl.phosphor.control.standard.BranchStart;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.InsnList;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LdcInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;

public class BasicControlFlowAnalyzer implements ControlFlowAnalyzer {

    private int numberOfUniqueBranchIDs = 0;

    public int getNumberOfUniqueBranchIDs() {
        return numberOfUniqueBranchIDs;
    }

    @Override
    public void annotate(String owner, MethodNode methodNode) {
        numberOfUniqueBranchIDs = 0;
        if(methodNode.instructions.size() > 0) {
            Set<Branch> branches = gatherBranches(methodNode);
            markBranchEnds(branches, methodNode.instructions);
            markBranchStarts(branches, methodNode.instructions);
            numberOfUniqueBranchIDs = branches.size();
        }
    }

    private void markBranchEnds(Set<Branch> branches, InsnList instructions) {
        for(Branch branch : branches) {
            BasicBlock scopeEnd = branch.immediatePostDominator;
            if(!(scopeEnd instanceof DummyBasicBlock)) {
                AbstractInsnNode insn = findNextPrecedableInstruction(scopeEnd.getFirstInsn());
                instructions.insertBefore(insn, new LdcInsnNode(new BranchEnd(branch.branchID)));
            }
        }
    }

    private void markBranchStarts(Set<Branch> branches, InsnList instructions) {
        for(Branch branch : branches) {
            AbstractInsnNode insn = branch.source.getLastInsn();
            instructions.insertBefore(insn, new LdcInsnNode(new BranchStart(branch.branchID)));
        }
    }

    private static AbstractInsnNode findNextPrecedableInstruction(AbstractInsnNode insn) {
        while(insn.getType() == AbstractInsnNode.FRAME || insn.getType() == AbstractInsnNode.LINE
                || insn.getType() == AbstractInsnNode.LABEL || insn.getOpcode() > 200) {
            insn = insn.getNext();
        }
        return insn;
    }

    private static Set<Branch> gatherBranches(MethodNode methodNode) {
        BranchGatheringGraphCreator graphCreator = new BranchGatheringGraphCreator();
        FlowGraph<BasicBlock> cfg = graphCreator.createControlFlowGraph(methodNode);
        Set<Branch> branches = new HashSet<>();
        int nextId = 0;
        Map<BasicBlock, BasicBlock> immediatePostDominators = cfg.getImmediatePostDominators();
        for(BasicBlock branchSource : graphCreator.getBranchSources()) {
            if(immediatePostDominators.containsKey(branchSource)) {
                branches.add(new Branch(nextId++, branchSource, immediatePostDominators.get(branchSource)));
            }
        }
        return branches;
    }

    private static final class Branch {
        private final int branchID;
        private final BasicBlock source;
        private final BasicBlock immediatePostDominator;

        private Branch(int branchID, BasicBlock source, BasicBlock immediatePostDominator) {
            this.source = source;
            this.immediatePostDominator = immediatePostDominator;
            this.branchID = branchID;
        }
    }

    private static final class BranchGatheringGraphCreator extends BaseControlFlowGraphCreator {
        private final Set<BasicBlock> branchSources = new HashSet<>();

        @Override
        protected void addBranchTakenEdge(BasicBlock source, BasicBlock target) {
            super.addBranchTakenEdge(source, target);
            branchSources.add(source);
        }

        @Override
        protected void addBranchNotTakenEdge(BasicBlock source, BasicBlock target) {
            super.addBranchNotTakenEdge(source, target);
            branchSources.add(source);
        }

        @Override
        protected void addNonDefaultCaseSwitchEdge(BasicBlock source, BasicBlock target) {
            super.addNonDefaultCaseSwitchEdge(source, target);
            branchSources.add(source);
        }

        @Override
        protected void addDefaultCaseSwitchEdge(BasicBlock source, BasicBlock target) {
            super.addDefaultCaseSwitchEdge(source, target);
            branchSources.add(source);
        }

        public Set<BasicBlock> getBranchSources() {
            return branchSources;
        }
    }
}