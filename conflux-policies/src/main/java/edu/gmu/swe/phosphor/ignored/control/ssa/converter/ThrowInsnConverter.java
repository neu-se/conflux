package edu.gmu.swe.phosphor.ignored.control.ssa.converter;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Value;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.StackElement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.ThrowStatement;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.ATHROW;

public class ThrowInsnConverter extends InsnConverter {

    ThrowInsnConverter(InsnConverter next) {
        super(next);
    }

    @Override
    protected boolean canProcess(AbstractInsnNode insn) {
        return insn.getOpcode() == ATHROW;
    }

    @Override
    protected Statement[] process(AbstractInsnNode insn, Frame<? extends Value> frame) {
        StackElement first = new StackElement(frame.getStackSize() - 1);
        return new Statement[]{new ThrowStatement(first)};
    }
}
