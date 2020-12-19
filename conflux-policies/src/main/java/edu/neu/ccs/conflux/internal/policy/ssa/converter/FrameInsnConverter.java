package edu.neu.ccs.conflux.internal.policy.ssa.converter;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.FrameNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Value;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.FrameStatement;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.Statement;

public class FrameInsnConverter extends InsnConverter {

    FrameInsnConverter(InsnConverter next) {
        super(next);
    }

    @Override
    protected boolean canProcess(AbstractInsnNode insn) {
        return insn instanceof FrameNode;
    }

    @Override
    protected Statement[] process(AbstractInsnNode insn, Frame<? extends Value> frame) {
        if(!(insn instanceof FrameNode)) {
            throw new IllegalArgumentException();
        }
        return new Statement[]{new FrameStatement((FrameNode) insn)};
    }
}
