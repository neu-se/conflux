package edu.neu.ccs.conflux.internal.policy.ssa.converter;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Value;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.IdleStatement;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.Statement;

public abstract class InsnConverter {

    private static InsnConverter chain;

    private final InsnConverter next;

    InsnConverter(InsnConverter next) {
        this.next = next;
    }

    protected abstract boolean canProcess(AbstractInsnNode insn);

    protected abstract Statement[] process(AbstractInsnNode insn, Frame<? extends Value> frame);

    public final Statement[] convert(AbstractInsnNode insn, Frame<? extends Value> frame) {
        if(frame == null) {
            // Unreachable instructions have no statements
            return new Statement[0];

        } else if(canProcess(insn)) {
            return process(insn, frame);
        } else if(next == null) {
            return new Statement[]{IdleStatement.UNIMPLEMENTED};
        } else {
            return next.convert(insn, frame);
        }
    }

    public static synchronized InsnConverter getChain() {
        if(chain == null) {
            chain = new ArrayInsnConverter(null);
            chain = new BinaryOperationInsnConverter(chain);
            chain = new ConstantInsnConverter(chain);
            chain = new DupSwapInsnConverter(chain);
            chain = new FieldInsnConverter(chain);
            chain = new FrameInsnConverter(chain);
            chain = new GotoInsnConverter(chain);
            chain = new IdleInsnConverter(chain);
            chain = new IfInsnConverter(chain);
            chain = new LabelInsnConverter(chain);
            chain = new LineNumberInsnConverter(chain);
            chain = new LocalVariableInsnConverter(chain);
            chain = new InvokeInsnConverter(chain);
            chain = new MonitorInsnConverter(chain);
            chain = new NewInsnConverter(chain);
            chain = new ReturnInsnConverter(chain);
            chain = new SwitchInsnConverter(chain);
            chain = new ThrowInsnConverter(chain);
            chain = new UnaryOperationInsnConverter(chain);
        }
        return chain;
    }
}
