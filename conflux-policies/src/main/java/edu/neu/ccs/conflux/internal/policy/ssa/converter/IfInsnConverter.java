package edu.neu.ccs.conflux.internal.policy.ssa.converter;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.JumpInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Value;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.*;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.IfStatement;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.Statement;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public class IfInsnConverter extends InsnConverter {

    IfInsnConverter(InsnConverter next) {
        super(next);
    }

    @Override
    protected boolean canProcess(AbstractInsnNode insn) {
        switch(insn.getOpcode()) {
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
            case IFNULL:
            case IFNONNULL:
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
                return true;
            default:
                return false;
        }
    }

    @Override
    protected Statement[] process(AbstractInsnNode insn, Frame<? extends Value> frame) {
        BinaryOperation operation = BinaryOperation.getInstance(insn);
        Expression operand1 = new StackElement(frame.getStackSize() - 2);
        Expression operand2 = new StackElement(frame.getStackSize() - 1);
        switch(insn.getOpcode()) {
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
                operand1 = operand2;
                operand2 = ConstantExpression.I0;
                break;
            case IFNULL:
            case IFNONNULL:
                operand1 = operand2;
                operand2 = ConstantExpression.NULL;
        }
        BinaryExpression expression = new BinaryExpression(operation, operand1, operand2);
        return new Statement[]{new IfStatement(expression, ((JumpInsnNode) insn).label.getLabel())};
    }
}
