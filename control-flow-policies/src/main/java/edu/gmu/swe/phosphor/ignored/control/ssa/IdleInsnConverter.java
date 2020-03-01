package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.EmptyStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

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
    protected Statement[] process(AbstractInsnNode insn, Frame<TypeValue> frame) {
        switch(insn.getOpcode()) {
            case NOP:
                return new Statement[]{EmptyStatement.NOP};
            case POP:
                return new Statement[]{EmptyStatement.POP};
            case POP2:
                return new Statement[]{EmptyStatement.POP2};
            default:
                throw new IllegalArgumentException();
        }
    }
}
