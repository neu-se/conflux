package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.InvokeDynamicInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;
import jdk.nashorn.internal.codegen.types.Type;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.INVOKESTATIC;

public class InvokeExpression implements Expression {
    public final String owner;
    public final String name;
    public final Expression receiver;
    public final Expression[] arguments;

    public InvokeExpression(String owner, String name, Expression receiver, Expression[] arguments) {
        this.owner = owner;
        this.name = name;
        this.receiver = receiver;
        this.arguments = arguments.clone();
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    Expression getReceiver() {
        return receiver;
    }

    Expression[] getArguments() {
        return arguments;
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

    public static InvokeExpression getInstance(MethodInsnNode insn, Frame<TypeValue> frame) {
        int numArguments = Type.getMethodArguments(insn.desc).length;
        int top = frame.getStackSize();
        Expression[] arguments = new StackElement[numArguments];
        for(int i = 0; i < arguments.length; i++) {
            arguments[i] = new StackElement(top - arguments.length + i);
        }
        Expression receiver = null;
        if(insn.getOpcode() != INVOKESTATIC) {
            receiver = new StackElement(top - arguments.length - 1);
        }
        return new InvokeExpression(insn.owner, insn.name, receiver, arguments);
    }

    public static InvokeExpression getInstance(InvokeDynamicInsnNode insn, Frame<TypeValue> frame) {
        int numArguments = Type.getMethodArguments(insn.desc).length;
        int top = frame.getStackSize();
        Expression[] arguments = new StackElement[numArguments];
        for(int i = 0; i < arguments.length; i++) {
            arguments[i] = new StackElement(top - arguments.length + i);
        }
        Expression receiver = null;
        if(insn.getOpcode() != INVOKESTATIC) {
            receiver = new StackElement(top - arguments.length - 1);
        }
        return new InvokeExpression(insn.bsm.getOwner(), insn.name, receiver, arguments);
    }
}
