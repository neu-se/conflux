package edu.gmu.swe.phosphor.ignored.control.ssa.converter;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.VarInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Value;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.LocalVariable;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.StackElement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.AssignmentStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public class LocalVariableInsnConverter extends InsnConverter {

    LocalVariableInsnConverter(InsnConverter next) {
        super(next);
    }

    @Override
    protected boolean canProcess(AbstractInsnNode insn) {
        return insn instanceof VarInsnNode && insn.getOpcode() != RET;
    }

    @Override
    protected Statement[] process(AbstractInsnNode insn, Frame<? extends Value> frame) {
        LocalVariable local = new LocalVariable(((VarInsnNode) insn).var);
        Statement statement;
        switch(insn.getOpcode()) {
            case ILOAD:
            case LLOAD:
            case FLOAD:
            case DLOAD:
            case ALOAD:
                statement = new AssignmentStatement(new StackElement(frame.getStackSize()), local);
                break;
            case ISTORE:
            case LSTORE:
            case FSTORE:
            case DSTORE:
            case ASTORE:
                statement = new AssignmentStatement(local, new StackElement(frame.getStackSize() - 1));
                break;
            default:
                throw new IllegalArgumentException();
        }
        return new Statement[]{statement};
    }
}
