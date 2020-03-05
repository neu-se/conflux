package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;

public final class FloatConstantExpression implements ConstantExpression {

    private final float constant;

    public FloatConstantExpression(float constant) {
        this.constant = constant;
    }

    public float getConstant() {
        return constant;
    }

    FloatConstantExpression negate(int size, AbstractInsnNode insnSource) {
        return new FloatConstantExpression(-constant);
    }

    DoubleConstantExpression castToDouble(int size, AbstractInsnNode insnSource) {
        return new DoubleConstantExpression(constant);
    }

    IntegerConstantExpression castToInt(int size, AbstractInsnNode insnSource) {
        return new IntegerConstantExpression((int) constant);
    }

    LongConstantExpression castToLong(int size, AbstractInsnNode insnSource) {
        return new LongConstantExpression((long) constant);
    }

    FloatConstantExpression add(FloatConstantExpression other) {
        return new FloatConstantExpression(constant + other.constant);
    }

    FloatConstantExpression subtract(FloatConstantExpression other) {
        return new FloatConstantExpression(constant - other.constant);
    }

    FloatConstantExpression divide(FloatConstantExpression other) {
        return new FloatConstantExpression(constant / other.constant);
    }

    FloatConstantExpression multiply(FloatConstantExpression other) {
        return new FloatConstantExpression(constant * other.constant);
    }

    FloatConstantExpression remainder(FloatConstantExpression other) {
        return new FloatConstantExpression(constant % other.constant);
    }

    FloatConstantExpression compareG(FloatConstantExpression other) {
        float result;
        if(Float.isNaN(constant) || Float.isNaN(other.constant)) {
            result = 1;
        } else {
            result = Float.compare(constant, other.constant);
        }
        return new FloatConstantExpression(result);
    }

    FloatConstantExpression compareL(FloatConstantExpression other) {
        float result;
        if(Float.isNaN(constant) || Float.isNaN(other.constant)) {
            result = -1;
        } else {
            result = Float.compare(constant, other.constant);
        }
        return new FloatConstantExpression(result);
    }

    @Override
    public boolean canMerge(ConstantExpression other) {
        if(other instanceof IntegerConstantExpression) {
            return constant == ((IntegerConstantExpression) other).getConstant();
        } else if(other instanceof LongConstantExpression) {
            return constant == ((LongConstantExpression) other).getConstant();
        } else if(other instanceof FloatConstantExpression) {
            return constant == ((FloatConstantExpression) other).getConstant();
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof FloatConstantExpression)) {
            return false;
        }
        FloatConstantExpression that = (FloatConstantExpression) o;
        return Float.compare(that.constant, constant) == 0;
    }

    @Override
    public int hashCode() {
        return (constant != +0.0f ? Float.floatToIntBits(constant) : 0);
    }

    @Override
    public String toString() {
        return String.valueOf(constant);
    }

    @Override
    public FloatConstantExpression process(Map<VersionedExpression, VersionStack> versionStacks) {
        return this;
    }
}
