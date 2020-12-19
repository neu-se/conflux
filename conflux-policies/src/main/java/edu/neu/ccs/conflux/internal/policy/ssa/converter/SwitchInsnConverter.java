package edu.neu.ccs.conflux.internal.policy.ssa.converter;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LookupSwitchInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.TableSwitchInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Value;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.StackElement;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.Statement;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.SwitchStatement;

public class SwitchInsnConverter extends InsnConverter {

    SwitchInsnConverter(InsnConverter next) {
        super(next);
    }

    @Override
    protected boolean canProcess(AbstractInsnNode insn) {
        return insn instanceof TableSwitchInsnNode || insn instanceof LookupSwitchInsnNode;
    }

    @Override
    protected Statement[] process(AbstractInsnNode insn, Frame<? extends Value> frame) {
        StackElement first = new StackElement(frame.getStackSize() - 1);
        if(insn instanceof TableSwitchInsnNode) {
            return new Statement[]{new SwitchStatement(first, (TableSwitchInsnNode) insn)};
        } else if(insn instanceof LookupSwitchInsnNode) {
            return new Statement[]{new SwitchStatement(first, (LookupSwitchInsnNode) insn)};
        } else {
            throw new IllegalArgumentException();
        }
    }
}
