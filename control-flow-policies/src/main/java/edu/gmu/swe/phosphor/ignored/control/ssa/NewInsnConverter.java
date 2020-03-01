package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.IntInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MultiANewArrayInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.TypeInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.NewArrayExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.NewExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.StackElement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.AssignmentStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.EmptyStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public class NewInsnConverter extends InsnConverter {

    NewInsnConverter(InsnConverter next) {
        super(next);
    }

    @Override
    protected boolean canProcess(AbstractInsnNode insn) {
        switch(insn.getOpcode()) {
            case NEW:
            case NEWARRAY:
            case ANEWARRAY:
            case MULTIANEWARRAY:
                return true;
            default:
                return false;
        }
    }

    @Override
    protected Statement[] process(AbstractInsnNode insn, Frame<TypeValue> frame) {
        int opcode = insn.getOpcode();
        Statement statement;
        if(opcode == NEW) {
            // -> objectref
            String desc = ((TypeInsnNode) insn).desc;
            NewExpression expr = new NewExpression(desc);
            StackElement next = new StackElement(frame.getStackSize());
            statement = new AssignmentStatement(next, expr);
        } else if(opcode == ANEWARRAY) {
            // count -> arrayref
            String desc = ((TypeInsnNode) insn).desc;
            StackElement first = new StackElement(frame.getStackSize() - 1);
            NewArrayExpression expr = new NewArrayExpression(desc, first);
            statement = new AssignmentStatement(first, expr);
        } else if(opcode == NEWARRAY) {
            // count -> arrayref
            int operand = ((IntInsnNode) insn).operand;
            String desc;
            switch(operand) {
                case T_BOOLEAN:
                    desc = "boolean";
                    break;
                case T_CHAR:
                    desc = "char";
                    break;
                case T_FLOAT:
                    desc = "float";
                    break;
                case T_DOUBLE:
                    desc = "double";
                    break;
                case T_BYTE:
                    desc = "byte";
                    break;
                case T_SHORT:
                    desc = "short";
                    break;
                case T_INT:
                    desc = "int";
                    break;
                case T_LONG:
                    desc = "long";
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            StackElement first = new StackElement(frame.getStackSize() - 1);
            NewArrayExpression expr = new NewArrayExpression(desc, first);
            statement = new AssignmentStatement(first, expr);
        } else if(opcode == MULTIANEWARRAY) {
            // count1, [count2, ...] -> arrayref
            String desc = ((MultiANewArrayInsnNode) insn).desc;
            int dims = ((MultiANewArrayInsnNode) insn).dims;
            // TODO
            statement = EmptyStatement.UNIMPLEMENTED;
        } else {
            throw new IllegalArgumentException();
        }
        return new Statement[]{statement};
    }
}
