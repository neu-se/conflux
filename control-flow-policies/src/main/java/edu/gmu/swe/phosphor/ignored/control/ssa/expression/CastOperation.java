package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.TypeInsnNode;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public final class CastOperation implements UnaryOperation {

    public static final CastOperation TO_INT = new CastOperation("int");
    public static final CastOperation TO_LONG = new CastOperation("long");
    public static final CastOperation TO_FLOAT = new CastOperation("float");
    public static final CastOperation TO_DOUBLE = new CastOperation("double");
    public static final CastOperation TO_BYTE = new CastOperation("byte");
    public static final CastOperation TO_CHAR = new CastOperation("char");
    public static final CastOperation TO_SHORT = new CastOperation("short");

    private final String desc;

    private CastOperation(String desc) {
        if(desc == null) {
            throw new NullPointerException();
        }
        this.desc = desc;
    }

    @Override
    public String toString() {
        return String.format("(%s)", desc);
    }

    @Override
    public String format(Expression expression) {
        return String.format("(%s) %s", desc, expression);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof CastOperation)) {
            return false;
        }
        CastOperation that = (CastOperation) o;
        return desc.equals(that.desc);
    }

    @Override
    public int hashCode() {
        return desc.hashCode();
    }

    public static CastOperation getInstance(AbstractInsnNode insn) {
        switch(insn.getOpcode()) {
            case CHECKCAST:
                return new CastOperation(((TypeInsnNode) insn).desc);
            case I2L:
            case D2L:
            case F2L:
                return TO_LONG;
            case I2F:
            case D2F:
            case L2F:
                return TO_FLOAT;
            case I2D:
            case F2D:
            case L2D:
                return TO_DOUBLE;
            case L2I:
            case D2I:
            case F2I:
                return TO_INT;
            case I2B:
                return TO_BYTE;
            case I2C:
                return TO_CHAR;
            case I2S:
                return TO_SHORT;
            default:
                throw new IllegalArgumentException();
        }
    }
}
