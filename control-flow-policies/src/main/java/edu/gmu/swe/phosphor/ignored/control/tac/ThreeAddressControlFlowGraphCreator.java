package edu.gmu.swe.phosphor.ignored.control.tac;

import edu.columbia.cs.psl.phosphor.control.graph.BaseControlFlowGraphCreator;
import edu.columbia.cs.psl.phosphor.control.graph.BasicBlock;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashMap;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;

public class ThreeAddressControlFlowGraphCreator extends BaseControlFlowGraphCreator {

    private final ThreeAddressMethod method;
    private final Map<BasicBlock, ThreeAddressBasicBlock> shadowMap = new HashMap<>();

    public ThreeAddressControlFlowGraphCreator(ThreeAddressMethod method) {
        super(true);
        this.method = method;
    }

    public Map<BasicBlock, ThreeAddressBasicBlock> getShadowMap() {
        return shadowMap;
    }

    @Override
    protected void addEntryPoint() {
        super.addEntryPoint();
        BasicBlock block = super.builder.getEntryPoint();
        ThreeAddressBasicBlock shadow = new ThreeAddressEntryPoint(method);
        shadowMap.put(block, shadow);
    }

    @Override
    protected void addExitPoint() {
        super.addExitPoint();
        BasicBlock block = super.builder.getExitPoint();
        ThreeAddressBasicBlock shadow = new ThreeAddressExitPoint();
        shadowMap.put(block, shadow);
    }

    @Override
    protected BasicBlock addBasicBlock(AbstractInsnNode[] instructions, int index) {
        BasicBlock block = super.addBasicBlock(instructions, index);
        ThreeAddressBasicBlock shadow = new ThreeAddressBasicBlockImpl(instructions, method);
        shadowMap.put(block, shadow);
        return block;
    }
}
