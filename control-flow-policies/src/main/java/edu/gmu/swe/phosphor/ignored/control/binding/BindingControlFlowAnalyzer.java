package edu.gmu.swe.phosphor.ignored.control.binding;

import edu.columbia.cs.psl.phosphor.control.ControlFlowAnalyzer;
import edu.columbia.cs.psl.phosphor.control.OpcodesUtil;
import edu.columbia.cs.psl.phosphor.control.graph.*;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph.NaturalLoop;
import edu.columbia.cs.psl.phosphor.control.standard.BranchEnd;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.*;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.struct.SinglyLinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;

import java.util.Iterator;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;
import static edu.gmu.swe.phosphor.ignored.control.binding.LoopLevel.ConstantLoopLevel.CONSTANT_LOOP_LEVEL;

/**
 * Identifies and marks the scope of "binding" branch edges and indicates whether each marked edge is "revisable". Does
 * not consider edges due to exceptional control flow.
 *
 * <p>For a control flow graph G = (V, E):
 *
 * <p>An edge (u, v) in E is said to be a branch edge if and only if there exits some edge (u, w) in E such that v != w.
 * Branch edges are the result of conditional jump instructions (i.e., IF_ACMP<cond>, IF_ICMP<cond>, IF<cond>,
 * TABLESWITCH, LOOKUPSWITCH, IFNULL, and IFNONNULL).
 *
 * <p>A branch edge (u, v) is said to be binding if and only if one of the following conditions is true:
 * <ul>
 *     <li>The basic block u ends with an IFEQ or IFNE instruction.</li>
 *     <li>The basic block u ends with an IF_ICMPEQ or IF_ACMEQ instruction that has a jump target t and t = v</li>
 *     <li>The basic block u ends with an IF_ICMPNE or IF_ACMPNE instruction that has a jump target t and t != v</li>
 *     <li>The basic block u ends with a TABLESWITCH or LOOKUPSWITCH instruction that has a set of jump targets T and
 *     v is an element of T.</li>
 * </ul>
 *
 * <p>The scope of a binding branch edge is the range of instructions that are considered to have a binding control
 * dependency on the edge. The scope of a binding branch edge (u, v) starts after the end of the basic block u and
 * before the start of the basic block v. The scope of a binding branch edge (u, v) ends before each basic block w
 * in V such that the exists a path from the distinguished start vertex of the control flow graph to w that does not
 * contain the edge (u, v).
 *
 * <p> A branch edge (u, v) is said to be revisable if and only if all of the following conditions are true:
 * <ul>
 *     <li>The predicate of the conditional jump instruction that ends basic block u is not constant</li>
 *     <li>There exits some edge (u, w) in E such that v != w and there exists a path from w to v</li>
 * </ul>
 *
 * <p> An instruction is said to be revision-excluded if and only if one of the following conditions is true:
 * <ul>
 *     <li>It is an ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5, LCONST_0, LCONST_1, FCONST_0,
 *     FCONST_1, FCONST_2, DCONST_0, DCONST_1, BIPUSH, SIPUSH, or LDC instruction</li>
 *     <li>It is an IINC instruction</li>
 *     <li>It is an ISTORE, LSTORE, FSTORE, DSTORE, or ASTORE instruction that stores a value v into the local variable
 *     x where v can be expressed as an arithmetic expression where each operand is either a constant value or a single
 *     definition of x.
 * </ul>
 * A revision-excluded instruction is considered to be outside of the scope of all revisable branch edges.
 */
public class BindingControlFlowAnalyzer implements ControlFlowAnalyzer {

    /**
     * The instructions of the method being analyzed
     */
    private InsnList instructions;

    /**
     * A control flow graph representing the method
     */
    private FlowGraph<BasicBlock> controlFlowGraph;

    /**
     * Used to determine the constancy level of instructions in the method
     */
    private LoopLevelTracer tracer;

    /**
     * A mapping between each instruction in the method and the natural loops that contain it
     */
    private Map<AbstractInsnNode, Set<NaturalLoop<BasicBlock>>> containingLoopMap;

    /**
     * The number of unique IDs assigned to branches in the method
     */
    private int numberOfUniqueBranchIDs = 0;

    public int getNumberOfUniqueBranchIDs() {
        return numberOfUniqueBranchIDs;
    }

    /**
     * @param methodNode the method to be analyzed and possibly modified
     */
    @Override
    public void annotate(String owner, MethodNode methodNode) {
        numberOfUniqueBranchIDs = 0;
        instructions = methodNode.instructions;
        try {
            if(instructions.size() > 0) {
                Set<BindingBranchEdge> bindingEdges = new HashSet<>();
                BindingControlFlowGraphCreator creator = new BindingControlFlowGraphCreator(bindingEdges);
                controlFlowGraph = creator.createControlFlowGraph(methodNode);
                containingLoopMap = calculateContainingLoops();
                tracer = new LoopLevelTracer(owner, methodNode);
                bindingEdges = processBindingEdges(bindingEdges);
                markBranchEnds(bindingEdges);
                markBranchStarts(bindingEdges);
                addCopyTagInfo();
                addConstancyInfoNodes();
                markLoopExits();
            }
        } catch(AnalyzerException e) {
            numberOfUniqueBranchIDs = 0;
        }

    }

    /**
     * @return a mapping between each instruction and the natural loops that contain it
     */
    private Map<AbstractInsnNode, Set<NaturalLoop<BasicBlock>>> calculateContainingLoops() {
        Set<NaturalLoop<BasicBlock>> loops = controlFlowGraph.getNaturalLoops();
        Map<AbstractInsnNode, Set<NaturalLoop<BasicBlock>>> loopMap = new HashMap<>();
        Iterator<AbstractInsnNode> itr = instructions.iterator();
        while(itr.hasNext()) {
            loopMap.put(itr.next(), new HashSet<>());
        }
        for(NaturalLoop<BasicBlock> loop : loops) {
            for(BasicBlock basicBlock : loop.getVertices()) {
                if(basicBlock instanceof SimpleBasicBlock) {
                    AbstractInsnNode start = basicBlock.getFirstInsn();
                    while(start != null) {
                        loopMap.get(start).add(loop);
                        if(start == basicBlock.getLastInsn()) {
                            break;
                        }
                        start = start.getNext();
                    }
                }
            }
        }
        return loopMap;
    }

    /**
     * Adds CopyTagInfo nodes before IINC, constant storing, array store, field store, and local variable store
     * instructions.
     */
    private void addCopyTagInfo() {
        Iterator<AbstractInsnNode> itr = instructions.iterator();
        while(itr.hasNext()) {
            AbstractInsnNode insn = itr.next();
            if(OpcodesUtil.isPushConstantOpcode(insn.getOpcode()) || insn.getOpcode() == IINC) {
                instructions.insertBefore(insn, new LdcInsnNode(new CopyTagInfo(CONSTANT_LOOP_LEVEL)));
            } else if(OpcodesUtil.isArrayStore(insn.getOpcode()) || OpcodesUtil.isFieldStoreInsn(insn.getOpcode())
                    || OpcodesUtil.isLocalVariableStoreInsn(insn.getOpcode())) {
                instructions.insertBefore(insn, new LdcInsnNode(new CopyTagInfo(tracer.getLoopLevelMap().get(insn))));
            }
        }
    }

    /**
     * Adds FrameConstancyInfo nodes before MethodInsnNodes and InvokeDynamicInsnNodes.
     */
    private void addConstancyInfoNodes() {
        SinglyLinkedList<AbstractInsnNode> methodCalls = new SinglyLinkedList<>();
        Iterator<AbstractInsnNode> itr = instructions.iterator();
        while(itr.hasNext()) {
            AbstractInsnNode insn = itr.next();
            if(insn instanceof MethodInsnNode || insn instanceof InvokeDynamicInsnNode) {
                methodCalls.addLast(insn);
            }
        }
        for(AbstractInsnNode insn : methodCalls) {
            FrameConstancyInfo constancyInfo = tracer.generateMethodConstancyInfo(insn);
            if(constancyInfo != null) {
                instructions.insertBefore(insn, new LdcInsnNode(constancyInfo));
            }
        }
    }

    /**
     * Adds ExitLoopLevelInfo nodes at the beginning of each basic block v for each edge (u,v) in the
     * control flow graph such that u is contained in some loop l and v is not contained in l.
     */
    private void markLoopExits() {
        Set<NaturalLoop<BasicBlock>> loops = controlFlowGraph.getNaturalLoops();
        for(NaturalLoop<BasicBlock> loop : loops) {
            BasicBlock header = loop.getHeader();
            Set<SimpleBasicBlock> exits = new HashSet<>();
            for(BasicBlock vertex : loop.getVertices()) {
                for(BasicBlock target : controlFlowGraph.getSuccessors(vertex)) {
                    if(target instanceof BindingBranchEdge) {
                        target = ((BindingBranchEdge) target).target;
                    }
                    if(target instanceof SimpleBasicBlock && !loop.contains(target)) {
                        exits.add((SimpleBasicBlock) target);
                    }
                }
            }
            int numContainingLoops = containingLoopMap.get(header.getFirstInsn()).size();
            ExitLoopLevelInfo exitLoopLevelInfo = new ExitLoopLevelInfo(numContainingLoops);
            for(BasicBlock exit : exits) {
                AbstractInsnNode nextInsn = findNextPrecedableInstruction(exit.getFirstInsn());
                instructions.insertBefore(nextInsn, new LdcInsnNode(exitLoopLevelInfo));
            }
        }
    }

    /**
     * Marks the ends of the scopes of the specified binding edges by inserting BranchEnd nodes.
     */
    private void markBranchEnds(Set<BindingBranchEdge> bindingEdges) {
        for(BindingBranchEdge bindingEdge : bindingEdges) {
            Set<BasicBlock> scopeEnds = bindingEdge.getScopeEnds(controlFlowGraph);
            for(BasicBlock scopeEnd : scopeEnds) {
                AbstractInsnNode insn = findNextPrecedableInstruction(scopeEnd.getFirstInsn());
                instructions.insertBefore(insn, new LdcInsnNode(new BranchEnd(bindingEdge.id)));
            }
        }
    }

    /**
     * Marks the starts of the scopes of the specified binding edges by inserting BindingBranchStart nodes.
     */
    private void markBranchStarts(Set<BindingBranchEdge> bindingEdges) {
        for(BindingBranchEdge bindingEdge : bindingEdges) {
            BindingBranchStart scopeStart = new BindingBranchStart(bindingEdge.level, bindingEdge.id);
            AbstractInsnNode scopeStartNode = new LdcInsnNode(scopeStart);
            AbstractInsnNode targetInsn = bindingEdge.target.getFirstInsn();
            if(bindingEdge.branchTaken) {
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
                swapLabel(bindingEdge.source.getLastInsn(), ((LabelNode) targetInsn), newTarget);
            } else {
                instructions.insertBefore(findNextPrecedableInstruction(targetInsn), scopeStartNode);
            }
        }
    }

    private void swapLabel(AbstractInsnNode insn, LabelNode originalTarget, LabelNode newTarget) {
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

    private FrameNode findNextFrame(AbstractInsnNode insn) {
        while(insn != null && !(insn instanceof FrameNode)) {
            insn = insn.getNext();
        }
        return (FrameNode) insn;
    }

    private Set<BindingBranchEdge> processBindingEdges(Set<BindingBranchEdge> bindingEdges) {
        bindingEdges = filterBindingEdges(bindingEdges);
        numberOfUniqueBranchIDs = assignBranchIDs(bindingEdges);
        calculateLoopLevels(bindingEdges);
        return bindingEdges;
    }

    private Set<BindingBranchEdge> filterBindingEdges(Collection<BindingBranchEdge> bindingEdges) {
        Set<BindingBranchEdge> filtered = new HashSet<>();
        for(BindingBranchEdge edge : bindingEdges) {
            if(edge.hasNonEmptyScope(controlFlowGraph)) {
                filtered.add(edge);
            }
        }
        return filtered;
    }

    private int assignBranchIDs(Set<BindingBranchEdge> edges) {
        int nextBranchIDAssigned = 0;
        for(BindingBranchEdge edge : edges) {
            edge.id = nextBranchIDAssigned++;
        }
        return nextBranchIDAssigned;
    }

    private void calculateLoopLevels(Set<BindingBranchEdge> bindingEdges) {
        for(BindingBranchEdge bindingEdge : bindingEdges) {
            LoopLevel level = tracer.getLoopLevelMap().get(bindingEdge.source.getLastInsn());
            if(level instanceof LoopLevel.VariantLoopLevel && ((LoopLevel.VariantLoopLevel) level).getLevelOffset() != 0) {
                int revisableLoops = calculateRevisableContainingLoops(bindingEdge);
                if(((LoopLevel.VariantLoopLevel) level).getLevelOffset() > revisableLoops) {
                    level = new LoopLevel.VariantLoopLevel(revisableLoops);
                }
            }
            bindingEdge.level = level;
        }
    }

    private int calculateRevisableContainingLoops(BindingBranchEdge bindingEdge) {
        Set<NaturalLoop<BasicBlock>> containingLoops = containingLoopMap.get(bindingEdge.source.getLastInsn());
        int revisableLoops = 0;
        for(NaturalLoop<BasicBlock> containingLoop : containingLoops) {
            for(BasicBlock successor : controlFlowGraph.getSuccessors(bindingEdge.source)) {
                if(!successor.equals(bindingEdge) && controlFlowGraph.containsPath(successor, containingLoop.getHeader())) {
                    revisableLoops++;
                    break;
                }
            }
        }
        return revisableLoops;
    }

    private static AbstractInsnNode findNextPrecedableInstruction(AbstractInsnNode insn) {
        while(insn.getType() == AbstractInsnNode.FRAME || insn.getType() == AbstractInsnNode.LINE
                || insn.getType() == AbstractInsnNode.LABEL || insn.getOpcode() > 200) {
            insn = insn.getNext();
        }
        return insn;
    }

    /**
     * A BindingBranchEdge w is used in conjunction with the edges (u, w) and (w, v) to represent some edge
     * (u, v).
     */
    private static class BindingBranchEdge extends DummyBasicBlock {

        /**
         * The source vertex of this edge.
         */
        private final BasicBlock source;

        /**
         * The target vertex of this edge.
         */
        private final BasicBlock target;

        /**
         * True if this edge represents a jump occurring
         */
        private final boolean branchTaken;

        private LoopLevel level = null;

        private int id = -1;

        BindingBranchEdge(BasicBlock sourceBlock, BasicBlock target, boolean branchTaken) {
            this.source = sourceBlock;
            this.target = target;
            this.branchTaken = branchTaken;
        }

        /**
         * @param controlFlowGraph the control flow graph containing this edge
         * @return true if there is at least one basic block in the scope of this edge
         * @throws IllegalArgumentException if the specified control flow graph does not contain this edge
         */
        boolean hasNonEmptyScope(FlowGraph<BasicBlock> controlFlowGraph) {
            if(!controlFlowGraph.getVertices().contains(this)) {
                throw new IllegalArgumentException("Supplied control flow graph does contain this edge");
            }
            if(!controlFlowGraph.getDominanceFrontiers().containsKey(this)) {
                // This edge is unreachable in the specified graph
                return false;
            }
            return !controlFlowGraph.getDominanceFrontiers().get(this).contains(this.target);
        }

        /**
         * @param controlFlowGraph a control flow graph
         * @return the set of non-dummy basic blocks before which the scope of this edge ends or the empty set
         * if this edge is unreachable in the specified graph
         * @throws IllegalArgumentException if the specified control flow graph does not contain this edge or this edge
         *                                  is unreachable in the specified control flow graph
         */
        Set<BasicBlock> getScopeEnds(FlowGraph<BasicBlock> controlFlowGraph) {
            if(!controlFlowGraph.getVertices().contains(this)) {
                throw new IllegalArgumentException("Supplied control flow graph does contain this edge");
            }
            if(!controlFlowGraph.getDominanceFrontiers().containsKey(this)) {
                throw new IllegalArgumentException("Edge is unreachable in supplied control flow graph");
            }
            Set<BasicBlock> scopeEnds = new HashSet<>();
            for(BasicBlock block : controlFlowGraph.getDominanceFrontiers().get(this)) {
                if(!(block instanceof DummyBasicBlock)) {
                    scopeEnds.add(block);
                }
            }
            return scopeEnds;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) {
                return true;
            } else if(!(o instanceof BindingBranchEdge)) {
                return false;
            }
            BindingBranchEdge that = (BindingBranchEdge) o;
            if(!source.equals(that.source)) {
                return false;
            }
            return target.equals(that.target);
        }

        @Override
        public int hashCode() {
            int result = source.hashCode();
            result = 31 * result + target.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return String.format("<DirectedEdge: %s -> %s>", source, target);
        }
    }

    /**
     * Builds a control flow graph where each binding branch edge (u, v) is replaced with a vertex w, an edge (u, w)
     * and an edge (w, v).
     */
    private static class BindingControlFlowGraphCreator extends BaseControlFlowGraphCreator {

        /**
         * A set containing the vertices used to represent binding branch edges in the control flow graph
         */
        private final Set<? super BindingBranchEdge> bindingBranchEdges;

        BindingControlFlowGraphCreator(Set<? super BindingBranchEdge> bindingBranchEdges) {
            this.bindingBranchEdges = bindingBranchEdges;
        }

        @Override
        protected void addBranchTakenEdge(BasicBlock source, BasicBlock target) {
            switch(source.getLastInsn().getOpcode()) {
                case IFEQ:
                case IFNE:
                case IF_ICMPEQ:
                case IF_ACMPEQ:
                    addBindingBranchEdge(source, target, true);
                    break;
                default:
                    super.addBranchTakenEdge(source, target);
            }
        }

        @Override
        protected void addBranchNotTakenEdge(BasicBlock source, BasicBlock target) {
            switch(source.getLastInsn().getOpcode()) {
                case IFEQ:
                case IFNE:
                case IF_ICMPNE:
                case IF_ACMPNE:
                    addBindingBranchEdge(source, target, false);
                    break;
                default:
                    super.addBranchNotTakenEdge(source, target);
            }
        }

        @Override
        protected void addNonDefaultCaseSwitchEdge(BasicBlock source, BasicBlock target) {
            addBindingBranchEdge(source, target, true);
        }

        @Override
        protected void addDefaultCaseSwitchEdge(BasicBlock source, BasicBlock target) {
            addBindingBranchEdge(source, target, true);
        }

        /**
         * If the specified (source, target) edge is not already represented in the graph adds a new vertex w to the graph
         * and the edges (source, w) and (w, target).
         *
         * @param source      the source vertex of the binding branch edge to be represented in the graph
         * @param target      the target vertex of the binding branch edge to be represented in the graph
         * @param branchTaken true if the edge being added represents a jump occurring
         */
        private void addBindingBranchEdge(BasicBlock source, BasicBlock target, boolean branchTaken) {
            BindingBranchEdge bindingBranchEdge = new BindingBranchEdge(source, target, branchTaken);
            if(bindingBranchEdges.add(bindingBranchEdge)) {
                builder.addVertex(bindingBranchEdge);
                builder.addEdge(source, bindingBranchEdge);
                builder.addEdge(bindingBranchEdge, target);
            }
        }
    }
}