package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Arrays;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

public final class InvokeExpression implements Expression {

    /**
     * The internal name of the class that owns the method being called or null for INVOKEDYNAMIC instructions
     */
    private final String owner;

    /**
     * The name of the method, non-null.
     */
    private final String name;

    /**
     * The receiver instance of the method call for INVOKEVIRTUAL, INVOKESPECIAL and INVOKEINTERFACE instructions.
     * Null for INVOKESTATIC and INVOKEDYNAMIC instructions.
     */
    private final Expression receiver;

    /**
     * The arguments passed to the call, non-null.
     */
    private final Expression[] arguments;

    /**
     * The type of method call, non-null.
     */
    private final InvocationType type;

    public InvokeExpression(String owner, String name, Expression receiver, Expression[] arguments, InvocationType type) {
        if(name == null || type == null) {
            throw new NullPointerException();
        }
        switch(type) {
            case INVOKE_STATIC:
            case INVOKE_DYNAMIC:
                if(receiver != null) {
                    throw new IllegalArgumentException();
                }
                break;
            case INVOKE_SPECIAL:
            case INVOKE_VIRTUAL:
            case INVOKE_INTERFACE:
                if(receiver == null) {
                    throw new IllegalArgumentException();
                }
        }
        switch(type) {
            case INVOKE_DYNAMIC:
                if(owner != null) {
                    throw new IllegalArgumentException();
                }
                break;
            case INVOKE_STATIC:

            case INVOKE_SPECIAL:
            case INVOKE_VIRTUAL:
            case INVOKE_INTERFACE:
                if(owner == null) {
                    throw new IllegalArgumentException();
                }
        }
        this.owner = owner;
        this.name = name;
        this.receiver = receiver;
        this.arguments = arguments.clone();
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        switch(type) {
            case INVOKE_STATIC:
                builder.append(owner);
                break;
            case INVOKE_DYNAMIC:
                builder.append(type);
                break;
            case INVOKE_SPECIAL:
            case INVOKE_VIRTUAL:
            case INVOKE_INTERFACE:
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
        if(owner != null ? !owner.equals(that.owner) : that.owner != null) {
            return false;
        }
        if(!name.equals(that.name)) {
            return false;
        }
        if(receiver != null ? !receiver.equals(that.receiver) : that.receiver != null) {
            return false;
        }
        if(!Arrays.equals(arguments, that.arguments)) {
            return false;
        }
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = owner != null ? owner.hashCode() : 0;
        result = 31 * result + name.hashCode();
        result = 31 * result + (receiver != null ? receiver.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(arguments);
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public InvokeExpression process(Map<VersionedExpression, VersionStack> versionStacks) {
        Expression processedReceiver = null;
        Expression[] processedArguments = new Expression[arguments.length];
        if(receiver != null) {
            processedReceiver = receiver.process(versionStacks);
        }
        for(int i = 0; i < arguments.length; i++) {
            processedArguments[i] = arguments[i].process(versionStacks);
        }
        return new InvokeExpression(owner, name, processedReceiver, processedArguments, type);
    }

    @Override
    public List<VersionedExpression> referencedVariables() {
        return Statement.gatherVersionedExpressions(receiver, arguments);
    }
}
