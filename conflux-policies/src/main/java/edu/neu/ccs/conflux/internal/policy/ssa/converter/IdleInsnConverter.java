package edu.neu.ccs.conflux.internal.policy.ssa.converter;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Value;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.IdleStatement;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.Statement;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public class IdleInsnConverter extends InsnConverter {

    IdleInsnConverter(InsnConverter next) {
        super(next);
    }

    @Override
    protected boolean canProcess(AbstractInsnNode insn) {
        switch(insn.getOpcode()) {
            case NOP:
            case POP:
            case POP2:
                return true;
            default:
                return false;
        }
    }

    @Override
    protected Statement[] process(AbstractInsnNode insn, Frame<? extends Value> frame) {
        switch(insn.getOpcode()) {
            case NOP:
                return new Statement[]{IdleStatement.NOP};
            case POP:
                return new Statement[]{IdleStatement.POP};
            case POP2:
                return new Statement[]{IdleStatement.POP2};
            default:
                throw new IllegalArgumentException();
        }
    }
}
