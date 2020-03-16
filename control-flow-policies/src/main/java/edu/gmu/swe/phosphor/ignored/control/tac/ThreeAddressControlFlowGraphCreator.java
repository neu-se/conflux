package edu.gmu.swe.phosphor.ignored.control.tac;

import edu.columbia.cs.psl.phosphor.control.graph.ControlFlowGraphCreator;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraphBuilder;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.TryCatchBlockNode;

public class ThreeAddressControlFlowGraphCreator extends ControlFlowGraphCreator<ThreeAddressBasicBlock> {

    private final ThreeAddressMethod method;
    private FlowGraphBuilder<ThreeAddressBasicBlock> builder = new FlowGraphBuilder<>();

    public ThreeAddressControlFlowGraphCreator(ThreeAddressMethod method) {
        super(true);
        this.method = method;
    }

    @Override
    protected void addEntryPoint() {
        builder.addEntryPoint(new ThreeAddressEntryPoint(method));
    }

    @Override
    protected void addExitPoint() {
        builder.addExitPoint(new ThreeAddressExitPoint());
    }

    @Override
    protected ThreeAddressBasicBlock addBasicBlock(AbstractInsnNode[] instructions, int index) {
        ThreeAddressBasicBlock ThreeAddressBasicBlock = new ThreeAddressBasicBlockImpl(instructions, index, method);
        builder.addVertex(ThreeAddressBasicBlock);
        return ThreeAddressBasicBlock;
    }

    @Override
    protected void addEntryExitEdge() {
        builder.addEdge(builder.getEntryPoint(), builder.getExitPoint());
    }

    @Override
    protected void addStandardEdgeFromEntryPoint(ThreeAddressBasicBlock target) {
        builder.addEdge(builder.getEntryPoint(), target);
    }

    @Override
    protected void addExceptionalEdgeFromEntryPoint(ThreeAddressBasicBlock target, TryCatchBlockNode tryCatchBlockNode) {
        builder.addEdge(builder.getEntryPoint(), target);
    }

    @Override
    protected void addExceptionalEdge(ThreeAddressBasicBlock source, ThreeAddressBasicBlock target) {
        builder.addEdge(source, target);
    }

    @Override
    protected void addStandardEdgeToExitPoint(ThreeAddressBasicBlock source) {
        builder.addEdge(source, builder.getExitPoint());
    }

    @Override
    protected void addExceptionalEdgeToExitPoint(ThreeAddressBasicBlock source) {
        builder.addEdge(source, builder.getExitPoint());
    }

    @Override
    protected void addSequentialEdge(ThreeAddressBasicBlock source, ThreeAddressBasicBlock target) {
        builder.addEdge(source, target);
    }

    @Override
    protected void addUnconditionalJumpEdge(ThreeAddressBasicBlock source, ThreeAddressBasicBlock target) {
        builder.addEdge(source, target);
    }

    @Override
    protected void addBranchTakenEdge(ThreeAddressBasicBlock source, ThreeAddressBasicBlock target) {
        builder.addEdge(source, target);
    }

    @Override
    protected void addBranchNotTakenEdge(ThreeAddressBasicBlock source, ThreeAddressBasicBlock target) {
        builder.addEdge(source, target);
    }

    @Override
    protected void addNonDefaultCaseSwitchEdge(ThreeAddressBasicBlock source, ThreeAddressBasicBlock target) {
        builder.addEdge(source, target);
    }

    @Override
    protected void addDefaultCaseSwitchEdge(ThreeAddressBasicBlock source, ThreeAddressBasicBlock target) {
        builder.addEdge(source, target);
    }

    @Override
    protected FlowGraph<ThreeAddressBasicBlock> buildGraph() {
        ThreeAddressExitPoint exit = (ThreeAddressExitPoint) builder.getExitPoint();
        FlowGraph<ThreeAddressBasicBlock> graph = builder.build();
        exit.setIndex(graph.getVertices().size() - 1);
        builder = new FlowGraphBuilder<>();
        return graph;
    }
}
