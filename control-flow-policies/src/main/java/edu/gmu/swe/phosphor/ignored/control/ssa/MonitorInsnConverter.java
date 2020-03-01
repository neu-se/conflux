package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.MonitorOperation;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.StackElement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.MonitorStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

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
    protected Statement[] process(AbstractInsnNode insn, Frame<TypeValue> frame) {
        StackElement first = new StackElement(frame.getStackSize() - 1);
        return new Statement[]{new MonitorStatement(MonitorOperation.getInstance(insn), first)};
    }
}
