package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Arrays;

public final class NewArrayExpression implements Expression {

    private final String desc;
    private final Type type;
    private final Expression[] dims;

    public NewArrayExpression(String desc, Expression dim) {
        this(desc, new Expression[]{dim});
    }

    public NewArrayExpression(String desc, Expression[] dims) {
        if(desc == null) {
            throw new NullPointerException();
        }
        this.desc = desc;
        this.type = Type.getType(desc);
        this.dims = dims.clone();
    }

    public String getDesc() {
        return desc;
    }

    public Type getType() {
        return type;
    }

    public Expression[] getDims() {
        return dims.clone();
    }

    @Override
    public <V> V accept(ExpressionVisitor<V> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <V, S> V accept(StatefulExpressionVisitor<V, ? super S> visitor, S state) {
        return visitor.visit(this, state);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("(new ")
                .append(type.getClassName());
        for(Expression dim : dims) {
            builder.append("[");
            if(dim != null) {
                builder.append(dim);
            }
            builder.append("]");
        }
        return builder.append(")").toString();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof NewArrayExpression)) {
            return false;
        }
        NewArrayExpression that = (NewArrayExpression) o;
        if(!desc.equals(that.desc)) {
            return false;
        }
        return Arrays.equals(dims, that.dims);
    }

    @Override
    public int hashCode() {
        int result = desc.hashCode();
        result = 31 * result + Arrays.hashCode(dims);
        return result;
    }
}
