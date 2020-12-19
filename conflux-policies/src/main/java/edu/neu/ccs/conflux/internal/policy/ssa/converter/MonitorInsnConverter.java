package edu.neu.ccs.conflux.internal.policy.ssa.converter;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Value;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.StackElement;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.MonitorOperation;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.MonitorStatement;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.Statement;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.MONITORENTER;
import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.MONITOREXIT;

public class MonitorInsnConverter extends InsnConverter {

    MonitorInsnConverter(InsnConverter next) {
        super(next);
    }

    @Override
    protected boolean canProcess(AbstractInsnNode insn) {
        return insn.getOpcode() == MONITORENTER || insn.getOpcode() == MONITOREXIT;
    }

    @Override
    protected Statement[] process(AbstractInsnNode insn, Frame<? extends Value> frame) {
        StackElement first = new StackElement(frame.getStackSize() - 1);
        return new Statement[]{new MonitorStatement(MonitorOperation.getInstance(insn), first)};
    }
}
