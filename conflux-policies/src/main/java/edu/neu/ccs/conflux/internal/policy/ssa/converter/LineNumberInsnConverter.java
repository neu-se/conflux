package edu.neu.ccs.conflux.internal.policy.ssa.converter;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LineNumberNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Value;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.LineNumberStatement;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.Statement;

public class LineNumberInsnConverter extends InsnConverter {

    LineNumberInsnConverter(InsnConverter next) {
        super(next);
    }

    @Override
    protected boolean canProcess(AbstractInsnNode insn) {
        return insn instanceof LineNumberNode;
    }

    @Override
    protected Statement[] process(AbstractInsnNode insn, Frame<? extends Value> frame) {
        return new Statement[]{new LineNumberStatement((LineNumberNode) insn)};
    }
}
