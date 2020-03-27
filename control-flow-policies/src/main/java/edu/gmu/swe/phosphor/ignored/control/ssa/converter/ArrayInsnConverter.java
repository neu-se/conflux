package edu.gmu.swe.phosphor.ignored.control.ssa.converter;

import edu.columbia.cs.psl.phosphor.control.OpcodesUtil;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Value;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.ArrayAccess;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.StackElement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.AssignmentStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

public class ArrayInsnConverter extends InsnConverter {

    ArrayInsnConverter(InsnConverter next) {
        super(next);
    }

    @Override
    protected boolean canProcess(AbstractInsnNode insn) {
        return OpcodesUtil.isArrayLoad(insn.getOpcode()) || OpcodesUtil.isArrayStore(insn.getOpcode());
    }

    @Override
    protected Statement[] process(AbstractInsnNode insn, Frame<? extends Value> frame) {
        if(OpcodesUtil.isArrayStore(insn.getOpcode())) {
            // arrayref index value
            StackElement arrayRef = new StackElement(frame.getStackSize() - 3);
            StackElement index = new StackElement(frame.getStackSize() - 2);
            StackElement value = new StackElement(frame.getStackSize() - 1);
            Statement statement = new AssignmentStatement(new ArrayAccess(arrayRef, index), value);
            return new Statement[]{statement};
        } else if(OpcodesUtil.isArrayLoad(insn.getOpcode())) {
            // arrayref index
            StackElement arrayRef = new StackElement(frame.getStackSize() - 2);
            StackElement index = new StackElement(frame.getStackSize() - 1);
            Statement statement = new AssignmentStatement(new StackElement(frame.getStackSize() - 2),
                    new ArrayAccess(arrayRef, index));
            return new Statement[]{statement};
        } else {
            throw new IllegalArgumentException();
        }
    }
}
