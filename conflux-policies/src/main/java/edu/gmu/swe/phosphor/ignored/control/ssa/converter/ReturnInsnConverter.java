package edu.gmu.swe.phosphor.ignored.control.ssa.converter;

import edu.columbia.cs.psl.phosphor.control.OpcodesUtil;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Value;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.StackElement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.ReturnStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.RETURN;

public class ReturnInsnConverter extends InsnConverter {

    ReturnInsnConverter(InsnConverter next) {
        super(next);
    }

    @Override
    protected boolean canProcess(AbstractInsnNode insn) {
        return OpcodesUtil.isReturnOpcode(insn.getOpcode());
    }

    @Override
    protected Statement[] process(AbstractInsnNode insn, Frame<? extends Value> frame) {
        if(insn.getOpcode() == RETURN) {
            return new Statement[]{new ReturnStatement(null)};
        } else {
            return new Statement[]{new ReturnStatement(new StackElement(frame.getStackSize() - 1))};
        }
    }
}
