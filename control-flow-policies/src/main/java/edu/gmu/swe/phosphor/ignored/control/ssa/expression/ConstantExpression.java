package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.IntInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LdcInsnNode;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public interface ConstantExpression extends Expression {

    ObjectConstantExpression NULL = new ObjectConstantExpression(null);
    IntegerConstantExpression M1 = new IntegerConstantExpression(-1);
    IntegerConstantExpression I0 = new IntegerConstantExpression(0);
    IntegerConstantExpression I1 = new IntegerConstantExpression(1);
    IntegerConstantExpression I2 = new IntegerConstantExpression(2);
    IntegerConstantExpression I3 = new IntegerConstantExpression(3);
    IntegerConstantExpression I4 = new IntegerConstantExpression(4);
    IntegerConstantExpression I5 = new IntegerConstantExpression(5);
    LongConstantExpression L0 = new LongConstantExpression(0);
    LongConstantExpression L1 = new LongConstantExpression(1);
    FloatConstantExpression F0 = new FloatConstantExpression(0);
    FloatConstantExpression F1 = new FloatConstantExpression(1);
    FloatConstantExpression F2 = new FloatConstantExpression(2);
    DoubleConstantExpression D0 = new DoubleConstantExpression(0);
    DoubleConstantExpression D1 = new DoubleConstantExpression(1);

    boolean canMerge(ConstantExpression other);

    default IntegerConstantExpression constantEqual(ConstantExpression expression) {
        return fromBoolean(canMerge(expression));
    }

    default IntegerConstantExpression notEqual(ConstantExpression expression) {
        return flipBoolean(constantEqual(expression));
    }

    static IntegerConstantExpression fromBoolean(boolean b) {
        return b ? I1 : I0;
    }

    static IntegerConstantExpression flipBoolean(IntegerConstantExpression b) {
        return b == I1 ? I0 : I1;
    }

    static ConstantExpression makeInstance(AbstractInsnNode insn) {
        int opcode = insn.getOpcode();
        switch(opcode) {
            case ACONST_NULL:
                return NULL;
            case ICONST_M1:
                return M1;
            case ICONST_0:
                return I0;
            case ICONST_1:
                return I1;
            case ICONST_2:
                return I2;
            case ICONST_3:
                return I3;
            case ICONST_4:
                return I4;
            case ICONST_5:
                return I5;
            case LCONST_0:
                return L0;
            case LCONST_1:
                return L1;
            case FCONST_0:
                return F0;
            case FCONST_1:
                return F1;
            case FCONST_2:
                return F2;
            case DCONST_0:
                return D0;
            case DCONST_1:
                return D1;
            case BIPUSH:
            case SIPUSH:
                return new IntegerConstantExpression(((IntInsnNode) insn).operand);
            case LDC:
                return new ObjectConstantExpression(((LdcInsnNode) insn).cst);
            default:
                throw new IllegalArgumentException();
        }
    }
}
