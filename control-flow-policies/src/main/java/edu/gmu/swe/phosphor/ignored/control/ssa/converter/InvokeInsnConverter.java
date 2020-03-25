package edu.gmu.swe.phosphor.ignored.control.ssa.converter;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.InvokeDynamicInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Value;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.LinkedList;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.InvocationType;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.InvokeExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.StackElement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.AssignmentStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.InvokeStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.INVOKEDYNAMIC;
import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.INVOKESTATIC;

public class InvokeInsnConverter extends InsnConverter {

    InvokeInsnConverter(InsnConverter next) {
        super(next);
    }

    @Override
    protected boolean canProcess(AbstractInsnNode insn) {
        return insn instanceof MethodInsnNode || insn instanceof InvokeDynamicInsnNode;
    }

    @Override
    protected Statement[] process(AbstractInsnNode insn, Frame<? extends Value> frame) {
        String owner;
        String desc;
        String name;
        InvocationType type = InvocationType.getInstance(insn.getOpcode());
        if(insn instanceof MethodInsnNode) {
            desc = ((MethodInsnNode) insn).desc;
            owner = ((MethodInsnNode) insn).owner;
            name = ((MethodInsnNode) insn).name;
        } else {
            desc = ((InvokeDynamicInsnNode) insn).desc;
            owner = null;
            name = ((InvokeDynamicInsnNode) insn).name;
        }
        LinkedList<Expression> arguments = new LinkedList<>();
        for(int i = Type.getArgumentTypes(desc).length; i > 0; --i) {
            arguments.addFirst(new StackElement(frame.getStackSize() - arguments.size() - 1));
        }
        int popCount = arguments.size();
        Expression receiver = null;
        if(insn.getOpcode() != INVOKESTATIC && insn.getOpcode() != INVOKEDYNAMIC) {
            receiver = new StackElement(frame.getStackSize() - arguments.size() - 1);
            popCount++;
        }
        InvokeExpression expr = new InvokeExpression(owner, name, desc, receiver, arguments.toArray(new Expression[0]), type);
        Statement statement;
        if(Type.getReturnType(desc) == Type.VOID_TYPE) {
            statement = new InvokeStatement(expr);
        } else {
            StackElement returnSlot = new StackElement(frame.getStackSize() - popCount);
            statement = new AssignmentStatement(returnSlot, expr);
        }
        return new Statement[]{statement};
    }
}
