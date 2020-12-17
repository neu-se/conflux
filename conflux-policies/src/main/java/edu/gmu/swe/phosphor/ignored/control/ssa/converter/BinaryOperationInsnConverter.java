package edu.gmu.swe.phosphor.ignored.control.ssa.converter;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.IincInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Value;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.AssignmentStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public class BinaryOperationInsnConverter extends InsnConverter {

    BinaryOperationInsnConverter(InsnConverter next) {
        super(next);
    }

    @Override
    protected boolean canProcess(AbstractInsnNode insn) {
        switch(insn.getOpcode()) {
            case IADD:
            case LADD:
            case FADD:
            case DADD:
            case ISUB:
            case LSUB:
            case FSUB:
            case DSUB:
            case IMUL:
            case LMUL:
            case FMUL:
            case DMUL:
            case IDIV:
            case LDIV:
            case FDIV:
            case DDIV:
            case IREM:
            case LREM:
            case FREM:
            case DREM:
            case ISHL:
            case LSHL:
            case ISHR:
            case LSHR:
            case IUSHR:
            case LUSHR:
            case IAND:
            case LAND:
            case IOR:
            case LOR:
            case IXOR:
            case LXOR:
            case LCMP:
            case FCMPL:
            case DCMPL:
            case FCMPG:
            case DCMPG:
            case IINC:
                return true;
            default:
                return false;
        }
    }

    @Override
    protected Statement[] process(AbstractInsnNode insn, Frame<? extends Value> frame) {
        if(insn instanceof IincInsnNode) {
            LocalVariable local = new LocalVariable(((IincInsnNode) insn).var);
            IntegerConstantExpression increment = new IntegerConstantExpression(((IincInsnNode) insn).incr);
            BinaryExpression expr = new BinaryExpression(BinaryOperation.ADD, local, increment);
            return new Statement[]{new AssignmentStatement(local, expr)};
        }
        BinaryOperation operation = BinaryOperation.getInstance(insn);
        StackElement first = new StackElement(frame.getStackSize() - 1);
        StackElement second = new StackElement(frame.getStackSize() - 2);
        Statement statement = new AssignmentStatement(second, new BinaryExpression(operation, second, first));
        return new Statement[]{statement};
    }
}
