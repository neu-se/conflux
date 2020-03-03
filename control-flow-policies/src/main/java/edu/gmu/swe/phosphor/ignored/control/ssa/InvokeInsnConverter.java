package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.InvokeDynamicInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.InvokeExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.StackElement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.AssignmentStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.InvokeStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

public class InvokeInsnConverter extends InsnConverter {

    InvokeInsnConverter(InsnConverter next) {
        super(next);
    }

    @Override
    protected boolean canProcess(AbstractInsnNode insn) {
        return insn instanceof MethodInsnNode || insn instanceof InvokeDynamicInsnNode;
    }

    @Override
    protected Statement[] process(AbstractInsnNode insn, Frame<TypeValue> frame) {
        InvokeExpression expr;
        Type ret;
        Statement statement;
        if(insn instanceof MethodInsnNode) {
            expr = InvokeExpression.getInstance((MethodInsnNode) insn, frame);
            ret = Type.getReturnType(((MethodInsnNode) insn).desc);
        } else if(insn instanceof InvokeDynamicInsnNode) {
            expr = InvokeExpression.getInstance((InvokeDynamicInsnNode) insn, frame);
            ret = Type.getReturnType(((InvokeDynamicInsnNode) insn).desc);
        } else {
            throw new IllegalArgumentException();
        }
        int count = expr.getArguments().length;
        if(expr.getReceiver() != null) {
            count++;
        }
        if(ret.getSort() == Type.VOID) {
            statement = new InvokeStatement(expr);
        } else {
            StackElement next = new StackElement(frame.getStackSize() - count);
            statement = new AssignmentStatement(next, expr);
        }
        return new Statement[]{statement};
    }
}
