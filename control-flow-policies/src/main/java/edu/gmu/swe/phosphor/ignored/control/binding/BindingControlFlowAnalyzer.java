package edu.gmu.swe.phosphor.ignored.control.binding;

import edu.columbia.cs.psl.phosphor.control.ControlFlowAnalyzer;
import edu.columbia.cs.psl.phosphor.control.OpcodesUtil;
import edu.columbia.cs.psl.phosphor.control.graph.*;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph.NaturalLoop;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.*;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.struct.SinglyLinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashMap;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import edu.gmu.swe.phosphor.ignored.control.BranchEdge;
import edu.gmu.swe.phosphor.ignored.control.ssa.SSAMethod;
import edu.gmu.swe.phosphor.ignored.control.ssa.TypeAnalyzer;

import java.util.Iterator;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;
import static edu.gmu.swe.phosphor.ignored.control.FlowGraphUtil.findNextPrecedableInstruction;
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
    private FlowGraph<BasicBlock> cfg;

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
                tracer = new LoopLevelTracer(methodNode, ssaMethod);
                Set<BindingBranchEdge> bindingEdges = new HashSet<>();
                BindingControlFlowGraphCreator creator = new BindingControlFlowGraphCreator(bindingEdges, new TypeAnalyzer(ssaMethod));
                cfg = creator.createControlFlowGraph(methodNode);
                containingLoopMap = calculateContainingLoops();
                numberOfUniqueBranchIDs = processEdges(bindingEdges);
                for(BranchEdge edge : bindingEdges) {
                    edge.markBranchStart(instructions);
                    edge.markBranchEnds(instructions);
                }
                addCopyTagInfo();
                addConstancyInfoNodes();
                markLoopExits();
            } catch(AnalyzerException e) {
                //
            }
        }
    }

    /**
     * @return a mapping between each instruction and the natural loops that contain it
     */
    private Map<AbstractInsnNode, Set<NaturalLoop<BasicBlock>>> calculateContainingLoops() {
        Set<NaturalLoop<BasicBlock>> loops = cfg.getNaturalLoops();
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
            if(OpcodesUtil.isArrayStore(insn.getOpcode())
                    || OpcodesUtil.isFieldStoreInsn(insn.getOpcode())
                    || OpcodesUtil.isLocalVariableStoreInsn(insn.getOpcode())
                    || OpcodesUtil.isPushConstantOpcode(insn.getOpcode())
                    || (OpcodesUtil.isReturnOpcode(insn.getOpcode()) && insn.getOpcode() != RETURN)
                    || insn.getOpcode() == IINC) {
                if(tracer.getLoopLevelMap().containsKey(insn)) {
                    instructions.insertBefore(insn, new LdcInsnNode(new CopyTagInfo(tracer.getLoopLevelMap().get(insn))));
                } else {
                    instructions.insertBefore(insn, new LdcInsnNode(new CopyTagInfo(CONSTANT_LOOP_LEVEL)));
                }
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
            int numContainingLoops = containingLoopMap.get(header.getFirstInsn()).size();
            ExitLoopLevelInfo exitLoopLevelInfo = new ExitLoopLevelInfo(numContainingLoops);
            for(BasicBlock exit : exits) {
                AbstractInsnNode nextInsn = findNextPrecedableInstruction(exit.getFirstInsn());
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
                edge.setBranchID(nextBranchIDAssigned++);
                edge.setScopeEnds(cfg);
                setLoopLevel(edge);
            }
        }
        return nextBranchIDAssigned;
    }

    private void setLoopLevel(BindingBranchEdge edge) {
        LoopLevel level = tracer.getLoopLevelMap().get(edge.getSource().getLastInsn());
        if(level instanceof LoopLevel.VariantLoopLevel && ((LoopLevel.VariantLoopLevel) level).getLevelOffset() != 0) {
            int revisableLoops = calculateRevisableContainingLoops(edge);
            if(((LoopLevel.VariantLoopLevel) level).getLevelOffset() > revisableLoops) {
                level = new LoopLevel.VariantLoopLevel(revisableLoops);
            }
        }
        edge.setLevel(level);
    }

    private int calculateRevisableContainingLoops(BindingBranchEdge edge) {
        Set<NaturalLoop<BasicBlock>> containingLoops = containingLoopMap.get(edge.getSource().getLastInsn());
        int revisableLoops = 0;
        for(NaturalLoop<BasicBlock> containingLoop : containingLoops) {
            for(BasicBlock successor : cfg.getSuccessors(edge.getSource())) {
                if(!successor.equals(edge) && cfg.containsPath(successor, containingLoop.getHeader())) {
                    revisableLoops++;
                    break;
                }
            }
        }
        return revisableLoops;
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
            return new LdcInsnNode(new BindingBranchStart(level, getBranchID()));
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