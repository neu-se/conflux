package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;
import edu.gmu.swe.phosphor.ignored.control.ssa.StackElement;
import jdk.nashorn.internal.codegen.types.Type;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.INVOKESTATIC;

public class InvokeExpression implements Expression {
    public final String owner;
    public final String name;
    public final StackElement receiver;
    public final StackElement[] arguments;

    public InvokeExpression(MethodInsnNode insn, Frame<TypeValue> frame) {
        this.owner = insn.owner;
        this.name = insn.owner;
        int numArguments = Type.getMethodArguments(insn.desc).length;
        int top = frame.getStackSize();
        arguments = new StackElement[numArguments];
        for(int i = 0; i < arguments.length; i++) {
            arguments[i] = new StackElement(top - arguments.length + i);
        }
        if(insn.getOpcode() == INVOKESTATIC) {
            receiver = null;
        } else {
            receiver = new StackElement(top - arguments.length - 1);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if(receiver == null) {
            builder.append(owner);
        } else {
            builder.append(receiver);
        }
        String[] stringArgs = new String[arguments.length];
        for(int i = 0; i < arguments.length; i++) {
            stringArgs[i] = arguments[i].toString();
        }
        builder.append('.').append(name).append('(').append(String.join(", ", stringArgs)).append(')');
        return builder.toString();
    }
}
