package edu.gmu.swe.phosphor.ignored.control.ssa.converter;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.IntInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MultiANewArrayInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.TypeInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Value;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.NewArrayExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.NewExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.StackElement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.AssignmentStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public class NewInsnConverter extends InsnConverter {

    private static final String[] operandDescMap = new String[]{null, null, null, null, "Z", "C", "F", "D", "B",
            "S", "I", "J"};

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
    protected Statement[] process(AbstractInsnNode insn, Frame<? extends Value> frame) {
        int opcode = insn.getOpcode();
        if(opcode == NEW) {
            // -> objectref
            String desc = ((TypeInsnNode) insn).desc;
            NewExpression expr = new NewExpression(desc);
            StackElement next = new StackElement(frame.getStackSize());
            return new Statement[]{new AssignmentStatement(next, expr)};
        }
        String desc;
        int pushedDimensions;
        int dimensions = 0;
        if(opcode == ANEWARRAY) {
            desc = ((TypeInsnNode) insn).desc;
            if(desc.contains("/") && !desc.contains(";")) {
                desc = "L" + desc + ";";
            }
            pushedDimensions = 1;
            dimensions++;
        } else if(opcode == NEWARRAY) {
            desc = operandDescMap[((IntInsnNode) insn).operand];
            if(desc == null) {
                throw new IllegalArgumentException();
            }
            pushedDimensions = 1;
            dimensions++;
        } else if(opcode == MULTIANEWARRAY) {
            desc = ((MultiANewArrayInsnNode) insn).desc;
            pushedDimensions = ((MultiANewArrayInsnNode) insn).dims;
        } else {
            throw new IllegalArgumentException();
        }
        int count = 0;
        for(char c : desc.toCharArray()) {
            if(c == '[') {
                count++;
            } else {
                break;
            }
        }
        desc = desc.substring(count);
        dimensions += count;
        Expression[] dims = new Expression[dimensions];
        for(int i = 0; i < pushedDimensions; i++) {
            dims[i] = new StackElement(frame.getStackSize() - pushedDimensions + i);
        }
        NewArrayExpression expr = new NewArrayExpression(desc, dims);
        return new Statement[]{new AssignmentStatement(new StackElement(frame.getStackSize() - pushedDimensions), expr)};
    }
}
