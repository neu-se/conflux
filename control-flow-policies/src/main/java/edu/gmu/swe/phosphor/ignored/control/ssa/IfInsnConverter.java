package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.JumpInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.IfStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

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
    protected Statement[] process(AbstractInsnNode insn, Frame<TypeValue> frame) {
        Condition condition = Condition.getInstance(insn);
        Expression leftHandSide = new StackElement(frame.getStackSize() - 2);
        Expression rightHandSide = new StackElement(frame.getStackSize() - 1);
        switch(insn.getOpcode()) {
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
                leftHandSide = rightHandSide;
                rightHandSide = ConstantExpression.I0;
                break;
            case IFNULL:
            case IFNONNULL:
                leftHandSide = rightHandSide;
                rightHandSide = ConstantExpression.NULL;
        }
        ConditionExpression expression = new ConditionExpression(condition, leftHandSide, rightHandSide);
        return new Statement[]{new IfStatement(expression, ((JumpInsnNode) insn).label.getLabel())};
    }
}
