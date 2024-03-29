package edu.neu.ccs.conflux.internal.policy.ssa.statement;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.Expression;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.MONITORENTER;
import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.MONITOREXIT;

public enum MonitorOperation {

    ENTER("entermonitor"),
    EXIT("exitmonitor");

    private final String symbol;

    MonitorOperation(String symbol) {
        this.symbol = symbol;
    }

    public String format(Expression operand) {
        return String.format("%s(%s)", symbol, operand);
    }

    public static MonitorOperation getInstance(AbstractInsnNode insn) {
        switch(insn.getOpcode()) {
            case MONITORENTER:
                return ENTER;
            case MONITOREXIT:
                return EXIT;
            default:
                throw new IllegalArgumentException();
        }
    }
}
