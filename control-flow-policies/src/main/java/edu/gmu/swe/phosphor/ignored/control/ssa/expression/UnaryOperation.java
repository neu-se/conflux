package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public enum UnaryOperation {
    NEGATE("-"),
    CAST_TO_INT("(int) "),
    CAST_TO_LONG("(long) "),
    CAST_TO_FLOAT("(float) "),
    CAST_TO_DOUBLE("(double) "),
    CAST_TO_BYTE("(byte) "),
    CAST_TO_CHAR("(char) "),
    CAST_TO_SHORT("(short) ");


    private final String symbol;

    UnaryOperation(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static UnaryOperation getInstance(int opcode) {
        switch(opcode) {
            case INEG:
            case LNEG:
            case FNEG:
            case DNEG:
                return NEGATE;
            case I2L:
            case D2L:
            case F2L:
                return CAST_TO_LONG;
            case I2F:
            case D2F:
            case L2F:
                return CAST_TO_FLOAT;
            case I2D:
            case F2D:
            case L2D:
                return CAST_TO_DOUBLE;
            case L2I:
            case D2I:
            case F2I:
                return CAST_TO_INT;
            case I2B:
                return CAST_TO_BYTE;
            case I2C:
                return CAST_TO_CHAR;
            case I2S:
                return CAST_TO_SHORT;
            default:
                throw new IllegalArgumentException();
        }
    }
}
