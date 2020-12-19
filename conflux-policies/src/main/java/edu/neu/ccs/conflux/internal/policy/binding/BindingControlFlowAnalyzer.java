package edu.neu.ccs.conflux.internal.policy.binding;

import edu.columbia.cs.psl.phosphor.control.ControlFlowAnalyzer;
import edu.columbia.cs.psl.phosphor.control.OpcodesUtil;
import edu.columbia.cs.psl.phosphor.control.graph.*;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph.NaturalLoop;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.*;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.struct.SinglyLinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.neu.ccs.conflux.internal.policy.BranchEdge;
import edu.neu.ccs.conflux.internal.policy.FlowGraphUtil;
import edu.neu.ccs.conflux.internal.policy.binding.tracer.LoopLevelTracer;
import edu.neu.ccs.conflux.internal.policy.ssa.SSAMethod;
import edu.neu.ccs.conflux.internal.policy.ssa.TypeAnalyzer;

import java.util.Iterator;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public class BindingControlFlowAnalyzer implements ControlFlowAnalyzer {

    /**
     * The instructions of the method being analyzed
     */
    private InsnList instructions;

    /**
     * A control flow graph representing the method
     */
    private FlowGraph<BasicBlock> cfg;

    /**
     * Used to determine the loop stability level of instructions in the method
     */
    private LoopLevelTracer tracer;

    /**
     * The number of unique IDs assigned to branches in the method
     */
    private int numberOfUniqueBranchIDs = 0;

    public int getNumberOfUniqueBranchIDs() {
        return numberOfUniqueBranchIDs;
    }

    /**
     * @param owner      the internal name of the declaring class of the method to be analyzed
     * @param methodNode the method to be analyzed and possibly modified
     */
    @Override
    public void annotate(String owner, MethodNode methodNode) {
        numberOfUniqueBranchIDs = 0;
        instructions = methodNode.instructions;
        if(instructions.size() > 0) {
            try {
                SSAMethod ssaMethod = new SSAMethod(owner, methodNode);
                tracer = new LoopLevelTracer(ssaMethod);
                Set<BindingBranchEdge> bindingEdges = new HashSet<>();
                TypeAnalyzer typeAnalyzer = new TypeAnalyzer(ssaMethod, false);
                BindingControlFlowGraphCreator creator = new BindingControlFlowGraphCreator(bindingEdges, typeAnalyzer);
                cfg = creator.createControlFlowGraph(methodNode);
                numberOfUniqueBranchIDs = processEdges(bindingEdges);
                for(BranchEdge edge : bindingEdges) {
                    edge.markBranchStart(instructions);
                    edge.markBranchEnds(instructions);
                }
                addCopyTagInfo();
                addLoopStabilityInfoNodes();
                markLoopExits();
            } catch(AnalyzerException e) {
                //
            }
        }
    }

    /**
     * Adds CopyTagInfo nodes before IINC, constant pushing instructions, array store instructions,
     * field store instructions, local variable store instructions, and non-void return instructions.
     */
    private void addCopyTagInfo() {
        Iterator<AbstractInsnNode> itr = instructions.iterator();
        while(itr.hasNext()) {
            AbstractInsnNode insn = itr.next();
            if(OpcodesUtil.isArrayStore(insn.getOpcode())
                    || OpcodesUtil.isFieldStoreInsn(insn.getOpcode())
                    || OpcodesUtil.isLocalVariableStoreInsn(insn.getOpcode())
                    || OpcodesUtil.isPushConstantOpcode(insn.getOpcode())
                    || (OpcodesUtil.isReturnOpcode(insn.getOpcode()) && insn.getOpcode() != RETURN)
                    || insn.getOpcode() == IINC) {
                instructions.insertBefore(insn, new LdcInsnNode(new CopyTagInfo(tracer.getLoopLevel(insn))));
            }
        }
    }

    /**
     * Adds FrameLoopStabilityInfo nodes before MethodInsnNodes and InvokeDynamicInsnNodes.
     */
    private void addLoopStabilityInfoNodes() {
        SinglyLinkedList<AbstractInsnNode> methodCalls = new SinglyLinkedList<>();
        Iterator<AbstractInsnNode> itr = instructions.iterator();
        while(itr.hasNext()) {
            AbstractInsnNode insn = itr.next();
            if(insn instanceof MethodInsnNode || insn instanceof InvokeDynamicInsnNode) {
                methodCalls.addLast(insn);
            }
        }
        for(AbstractInsnNode insn : methodCalls) {
            FrameLoopStabilityInfo stabilityInfo = tracer.generateMethodLoopStabilityInfo(insn);
            if(stabilityInfo != null) {
                instructions.insertBefore(insn, new LdcInsnNode(stabilityInfo));
            }
        }
    }

    /**
     * Adds ExitLoopLevelInfo nodes at the beginning of each basic block v for each edge (u,v) in the
     * control flow graph such that u is contained in some loop l and v is not contained in l.
     */
    private void markLoopExits() {
        Map<BasicBlock, Set<NaturalLoop<BasicBlock>>> containingLoopsMap = FlowGraphUtil.calculateContainingLoops(cfg);
        Set<NaturalLoop<BasicBlock>> loops = cfg.getNaturalLoops();
        for(NaturalLoop<BasicBlock> loop : loops) {
            BasicBlock header = loop.getHeader();
            Set<SimpleBasicBlock> exits = new HashSet<>();
            for(BasicBlock vertex : loop.getVertices()) {
                for(BasicBlock target : cfg.getSuccessors(vertex)) {
                    if(target instanceof BindingBranchEdge) {
                        target = ((BindingBranchEdge) target).getTarget();
                    }
                    if(target instanceof SimpleBasicBlock && !loop.contains(target)) {
                        exits.add((SimpleBasicBlock) target);
                    }
                }
            }
            int numContainingLoops = containingLoopsMap.get(header).size();
            ExitLoopLevelInfo exitLoopLevelInfo = new ExitLoopLevelInfo(numContainingLoops);
            for(BasicBlock exit : exits) {
                AbstractInsnNode nextInsn = FlowGraphUtil.findNextPrecedableInstruction(exit.getFirstInsn());
                instructions.insertBefore(nextInsn, new LdcInsnNode(exitLoopLevelInfo));
            }
        }
    }

    private int processEdges(Set<BindingBranchEdge> bindingEdges) {
        int nextBranchIDAssigned = 0;
        Iterator<BindingBranchEdge> itr = bindingEdges.iterator();
        while(itr.hasNext()) {
            BindingBranchEdge edge = itr.next();
            if(!edge.hasNonEmptyScope(cfg)) {
                itr.remove();
            } else {
                edge.setBranchId(nextBranchIDAssigned++);
                edge.setScopeEnds(cfg);
                setLoopLevel(edge);
            }
        }
        return nextBranchIDAssigned;
    }

    private void setLoopLevel(BindingBranchEdge edge) {
        LoopLevel level = tracer.getLoopLevel(edge.getSource().getLastInsn());
        edge.setLevel(level);
    }

    /**
     * A BindingBranchEdge w is used in conjunction with the edges (u, w) and (w, v) to represent some edge
     * (u, v).
     */
    private static class BindingBranchEdge extends BranchEdge {

        private LoopLevel level = null;

        BindingBranchEdge(BasicBlock source, BasicBlock target, boolean branchTaken) {
            super(source, target, branchTaken);
        }

        public void setLevel(LoopLevel level) {
            this.level = level;
        }

        @Override
        public AbstractInsnNode createScopeStartNode() {
            return new LdcInsnNode(new BindingBranchStart(level, getBranchId()));
        }

        boolean hasNonEmptyScope(FlowGraph<BasicBlock> cfg) {
            return cfg.getDominanceFrontiers().containsKey(this)
                    && !cfg.getDominanceFrontiers().get(this).contains(getTarget());
        }

        void setScopeEnds(FlowGraph<BasicBlock> controlFlowGraph) {
            if(controlFlowGraph.getDominanceFrontiers().containsKey(this)) {
                for(BasicBlock block : controlFlowGraph.getDominanceFrontiers().get(this)) {
                    if(!(block instanceof DummyBasicBlock)) {
                        addScopeEnd(block);
                    }
                }
            }
        }
    }

    public static <E> Set<E> findDuplicates(Collection<E> items) {
        Set<E> seen = new HashSet<>();
        Set<E> duplicates = new HashSet<>();
        for(E item : items) {
            if(!seen.add(item)) {
                duplicates.add(item);
            }
        }
        return duplicates;
    }

    public static <E extends BasicBlock> Set<E> findDuplicateTargets(Map<E, ? extends List<E>> switchCaseEdges,
                                                                     Map<E, E> switchDefaultEdges, E source) {
        E defaultBlock = switchDefaultEdges.get(source);
        List<E> allTargets = new LinkedList<>(switchCaseEdges.get(source));
        allTargets.add(defaultBlock);
        return findDuplicates(allTargets);
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

        /**
         * Used to determine whether an IFEQ or IFNE instruction has a boolean condition
         */
        private final TypeAnalyzer typeAnalyzer;

        /**
         * Maps basic blocks containing switch statements to lists containing the targets of their cases
         */
        private final Map<BasicBlock, List<BasicBlock>> switchCaseEdges = new HashMap<>();

        /**
         * Maps basic blocks containing switch statements to the targets of their default cases
         */
        private final Map<BasicBlock, BasicBlock> switchDefaultEdges = new HashMap<>();

        BindingControlFlowGraphCreator(Set<? super BindingBranchEdge> bindingBranchEdges, TypeAnalyzer typeAnalyzer) {
            this.bindingBranchEdges = bindingBranchEdges;
            this.typeAnalyzer = typeAnalyzer;
        }

        @Override
        protected void addBranchTakenEdge(BasicBlock source, BasicBlock target) {
            AbstractInsnNode insn = source.getLastInsn();
            switch(insn.getOpcode()) {
                case IF_ICMPEQ:
                case IF_ACMPEQ:
                case IFEQ:
                    addBindingBranchEdge(source, target, true);
                    break;
                case IFNE:
                    if(typeAnalyzer.isDoubleBindingBranch(insn)) {
                        addBindingBranchEdge(source, target, true);
                        break;
                    }
                default:
                    super.addBranchTakenEdge(source, target);
            }
        }

        @Override
        protected void addBranchNotTakenEdge(BasicBlock source, BasicBlock target) {
            AbstractInsnNode insn = source.getLastInsn();
            switch(insn.getOpcode()) {
                case IFNE:
                case IF_ICMPNE:
                case IF_ACMPNE:
                    addBindingBranchEdge(source, target, false);
                    break;
                case IFEQ:
                    if(typeAnalyzer.isDoubleBindingBranch(insn)) {
                        addBindingBranchEdge(source, target, false);
                        break;
                    }
                default:
                    super.addBranchNotTakenEdge(source, target);
            }
        }

        @Override
        protected void addNonDefaultCaseSwitchEdge(BasicBlock source, BasicBlock target) {
            if(!switchCaseEdges.containsKey(source)) {
                switchCaseEdges.put(source, new LinkedList<>());
            }
            switchCaseEdges.get(source).add(target);
        }

        @Override
        protected void addDefaultCaseSwitchEdge(BasicBlock source, BasicBlock target) {
            switchDefaultEdges.put(source, target);
            super.addDefaultCaseSwitchEdge(source, target);
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

        @Override
        protected FlowGraph<BasicBlock> buildGraph() {
            addSwitchEdges();
            return super.buildGraph();
        }

        private void addSwitchEdges() {
            // Find switch edges where multiple cases go to the same block
            for(BasicBlock source : switchCaseEdges.keySet()) {
                Set<BasicBlock> duplicates = findDuplicateTargets(switchCaseEdges, switchDefaultEdges, source);
                for(BasicBlock target : switchCaseEdges.get(source)) {
                    if(duplicates.contains(target)) {
                        super.addBranchTakenEdge(source, target);
                    } else {
                        addBindingBranchEdge(source, target, true);
                    }
                }
            }
        }
    }
}