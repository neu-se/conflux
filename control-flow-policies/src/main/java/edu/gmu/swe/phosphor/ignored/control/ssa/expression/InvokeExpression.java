package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.InvokeDynamicInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;
import jdk.nashorn.internal.codegen.types.Type;

import java.util.Arrays;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.INVOKESTATIC;

public final class InvokeExpression implements Expression {

    private final String owner;
    private final String name;
    private final Expression receiver;
    private final Expression[] arguments;

    public InvokeExpression(String owner, String name, Expression receiver, Expression[] arguments) {
        if(owner == null || name == null) {
            throw new NullPointerException();
        }
        this.owner = owner;
        this.name = name;
        this.receiver = receiver;
        this.arguments = arguments.clone();
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

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof InvokeExpression)) {
            return false;
        }
        InvokeExpression that = (InvokeExpression) o;
        if(!owner.equals(that.owner)) {
            return false;
        }
        if(!name.equals(that.name)) {
            return false;
        }
        if(receiver != null ? !receiver.equals(that.receiver) : that.receiver != null) {
            return false;
        }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        int result = owner.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (receiver != null ? receiver.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
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
