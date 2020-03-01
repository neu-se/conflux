package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.EmptyStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

public abstract class InsnConverter {

    private static InsnConverter chain;

    private final InsnConverter next;

    InsnConverter(InsnConverter next) {
        this.next = next;
    }

    protected abstract boolean canProcess(AbstractInsnNode insn);

    protected abstract Statement[] process(AbstractInsnNode insn, Frame<TypeValue> frame);

    public Statement[] convert(AbstractInsnNode insn, Frame<TypeValue> frame) {
        if(canProcess(insn)) {
            return process(insn, frame);
        } else if(next == null) {
            return new Statement[]{EmptyStatement.UNIMPLEMENTED};
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
            chain = new MethodInsnConverter(chain);
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
