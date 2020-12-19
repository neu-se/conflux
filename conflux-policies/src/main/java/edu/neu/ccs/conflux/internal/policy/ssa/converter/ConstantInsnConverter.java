package edu.neu.ccs.conflux.internal.policy.ssa.converter;

import edu.columbia.cs.psl.phosphor.control.OpcodesUtil;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Value;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.ConstantExpression;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.StackElement;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.AssignmentStatement;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.Statement;

public class ConstantInsnConverter extends InsnConverter {

    ConstantInsnConverter(InsnConverter next) {
        super(next);
    }

    @Override
    protected boolean canProcess(AbstractInsnNode insn) {
        return OpcodesUtil.isPushConstantOpcode(insn.getOpcode());
    }

    @Override
    protected Statement[] process(AbstractInsnNode insn, Frame<? extends Value> frame) {
        StackElement next = new StackElement(frame.getStackSize());
        Statement statement = new AssignmentStatement(next, ConstantExpression.makeInstance(insn));
        return new Statement[]{statement};
    }
}
