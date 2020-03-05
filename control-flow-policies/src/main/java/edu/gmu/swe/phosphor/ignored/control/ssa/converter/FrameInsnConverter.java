package edu.gmu.swe.phosphor.ignored.control.ssa.converter;

import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.FrameNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.FrameStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

public class FrameInsnConverter extends InsnConverter {

    FrameInsnConverter(InsnConverter next) {
        super(next);
    }

    @Override
    protected boolean canProcess(AbstractInsnNode insn) {
        return insn instanceof FrameNode;
    }

    @Override
    protected Statement[] process(AbstractInsnNode insn, Frame<TypeValue> frame) {
        if(!(insn instanceof FrameNode)) {
            throw new IllegalArgumentException();
        }
        return new Statement[]{new FrameStatement((FrameNode) insn)};
    }
}
