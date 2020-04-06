package edu.gmu.swe.phosphor.ignored.control.strict;

import edu.columbia.cs.psl.phosphor.control.ControlFlowAnalyzer;
import edu.columbia.cs.psl.phosphor.control.graph.BaseControlFlowGraphCreator;
import edu.columbia.cs.psl.phosphor.control.graph.BasicBlock;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import edu.gmu.swe.phosphor.ignored.control.BranchEdge;
import edu.gmu.swe.phosphor.ignored.control.ssa.SSAMethod;
import edu.gmu.swe.phosphor.ignored.control.ssa.TypeAnalyzer;

import java.util.Iterator;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public class StrictControlFlowAnalyzer implements ControlFlowAnalyzer {

    /**
     * The number of unique IDs assigned to branches in the method
     */
    private int numberOfUniqueBranchIDs = 0;

    public int getNumberOfUniqueBranchIDs() {
        return numberOfUniqueBranchIDs;
    }

    @Override
    public void annotate(String owner, MethodNode methodNode) {
        numberOfUniqueBranchIDs = 0;
        if(methodNode.instructions.size() > 0) {
            try {
                SSAMethod ssaMethod = new SSAMethod(owner, methodNode);
                Set<BranchEdge> strictEdges = new HashSet<>();
                StrictBranchGatheringGraphCreator creator = new StrictBranchGatheringGraphCreator(strictEdges, new TypeAnalyzer(ssaMethod));
                FlowGraph<BasicBlock> cfg = creator.createControlFlowGraph(methodNode);
                numberOfUniqueBranchIDs = processEdges(strictEdges, cfg);
                for(BranchEdge edge : strictEdges) {
                    edge.markBranchStart(methodNode.instructions);
                    edge.markBranchEnds(methodNode.instructions);
                }
            } catch(AnalyzerException e) {
                //
            }
        }
    }

    private int processEdges(Set<? extends BranchEdge> bindingEdges, FlowGraph<BasicBlock> cfg) {
        Map<BasicBlock, BasicBlock> immediatePostDominators = cfg.getImmediatePostDominators();
        int nextBranchIDAssigned = 0;
        Iterator<? extends BranchEdge> itr = bindingEdges.iterator();
        while(itr.hasNext()) {
            BranchEdge edge = itr.next();
            if(immediatePostDominators.containsKey(edge.getSource())) {
                edge.setBranchID(nextBranchIDAssigned++);
                edge.addScopeEnd(immediatePostDominators.get(edge.getSource()));
            } else {
                // Unreachable source block
                itr.remove();
            }
        }
        return nextBranchIDAssigned;
    }

    private static final class StrictBranchGatheringGraphCreator extends BaseControlFlowGraphCreator {

        private final Set<? super BranchEdge> branchEdges;
        private final TypeAnalyzer typeAnalyzer;

        StrictBranchGatheringGraphCreator(Set<? super BranchEdge> branchEdges, TypeAnalyzer typeAnalyzer) {
            this.branchEdges = branchEdges;
            this.typeAnalyzer = typeAnalyzer;
        }

        @Override
        protected void addBranchTakenEdge(BasicBlock source, BasicBlock target) {
            super.addBranchTakenEdge(source, target);
            AbstractInsnNode insn = source.getLastInsn();
            switch(insn.getOpcode()) {
                case IF_ICMPEQ:
                case IF_ACMPEQ:
                case IFEQ:
                    branchEdges.add(new BranchEdge(source, target, true));
                    break;
                case IFNE:
                    if(typeAnalyzer.isDoubleBindingBranch(insn)) {
                        branchEdges.add(new BranchEdge(source, target, true));
                        break;
                    }
            }
        }

        @Override
        protected void addBranchNotTakenEdge(BasicBlock source, BasicBlock target) {
            super.addBranchNotTakenEdge(source, target);
            AbstractInsnNode insn = source.getLastInsn();
            switch(insn.getOpcode()) {
                case IFNE:
                case IF_ICMPNE:
                case IF_ACMPNE:
                    branchEdges.add(new BranchEdge(source, target, false));
                    break;
                case IFEQ:
                    if(typeAnalyzer.isDoubleBindingBranch(insn)) {
                        branchEdges.add(new BranchEdge(source, target, false));
                        break;
                    }
            }
        }

        @Override
        protected void addNonDefaultCaseSwitchEdge(BasicBlock source, BasicBlock target) {
            super.addNonDefaultCaseSwitchEdge(source, target);
            branchEdges.add(new BranchEdge(source, target, true));
        }

        @Override
        protected void addDefaultCaseSwitchEdge(BasicBlock source, BasicBlock target) {
            super.addDefaultCaseSwitchEdge(source, target);
            branchEdges.add(new BranchEdge(source, target, true));
        }
    }
}