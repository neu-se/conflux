package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.TaintUtils;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.FrameNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LabelNode;

import java.util.Arrays;
import java.util.List;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public final class FrameStatement implements Statement {

    private final int type;
    private final String typeDesc;
    private final int nLocal;
    private final Object[] local;
    private final int nStack;
    private final Object[] stack;

    public FrameStatement(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        this.type = type;
        switch(type) {
            case F_NEW:
                typeDesc = "F_NEW";
                break;
            case F_FULL:
                typeDesc = "F_FULL";
                break;
            case TaintUtils.RAW_INSN:
                typeDesc = "RAW_INSN";
                break;
            case F_APPEND:
                typeDesc = "F_APPEND";
                break;
            case F_CHOP:
                typeDesc = "F_CHOP";
                break;
            case F_SAME:
                typeDesc = "F_SAME";
                break;
            case F_SAME1:
                typeDesc = "F_SAME1";
                break;
            default:
                throw new IllegalArgumentException();
        }
        this.nLocal = nLocal;
        this.local = local.clone();
        this.nStack = nStack;
        this.stack = stack.clone();
    }

    public FrameStatement(FrameNode frame) {
        this.type = frame.type;
        switch(type) {
            case F_NEW:
                typeDesc = "F_NEW";
                break;
            case F_FULL:
                typeDesc = "F_FULL";
                break;
            case TaintUtils.RAW_INSN:
                typeDesc = "RAW_INSN";
                break;
            case F_APPEND:
                typeDesc = "F_APPEND";
                break;
            case F_CHOP:
                typeDesc = "F_CHOP";
                break;
            case F_SAME:
                typeDesc = "F_SAME";
                break;
            case F_SAME1:
                typeDesc = "F_SAME1";
                break;
            default:
                throw new IllegalArgumentException();
        }
        switch(type) {
            case F_NEW:
            case F_FULL:
            case TaintUtils.RAW_INSN:
                nLocal = frame.local.size();
                local = asArray(frame.local);
                nStack = frame.stack.size();
                stack = asArray(frame.stack);
                break;
            case F_APPEND:
                nLocal = frame.local.size();
                local = asArray(frame.local);
                nStack = 0;
                stack = new Object[0];
                break;
            case F_CHOP:
                nLocal = frame.local.size();
                local = new Object[0];
                nStack = 0;
                stack = new Object[0];
                break;
            case F_SAME:
                nLocal = 0;
                local = new Object[0];
                nStack = 0;
                stack = new Object[0];
                break;
            case F_SAME1:
                nLocal = 0;
                local = new Object[0];
                nStack = 1;
                stack = asArray(frame.stack);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return String.format("{%s: %d | %s | %d | %s}", typeDesc, nLocal, Arrays.toString(local), nStack, Arrays.toString(stack));
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof FrameStatement)) {
            return false;
        }
        FrameStatement that = (FrameStatement) o;
        if(type != that.type) {
            return false;
        }
        if(nLocal != that.nLocal) {
            return false;
        }
        if(nStack != that.nStack) {
            return false;
        }
        if(!typeDesc.equals(that.typeDesc)) {
            return false;
        }
        if(!Arrays.equals(local, that.local)) {
            return false;
        }
        return Arrays.equals(stack, that.stack);
    }

    @Override
    public int hashCode() {
        int result = type;
        result = 31 * result + typeDesc.hashCode();
        result = 31 * result + nLocal;
        result = 31 * result + Arrays.hashCode(local);
        result = 31 * result + nStack;
        result = 31 * result + Arrays.hashCode(stack);
        return result;
    }

    private static Object[] asArray(final List<Object> l) {
        Object[] objs = new Object[l.size()];
        for(int i = 0; i < objs.length; ++i) {
            Object o = l.get(i);
            if(o instanceof LabelNode) {
                o = ((LabelNode) o).getLabel();
            }
            objs[i] = o;
        }
        return objs;
    }
}
