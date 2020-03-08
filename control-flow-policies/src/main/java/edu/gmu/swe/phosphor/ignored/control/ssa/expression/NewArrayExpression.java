package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Arrays;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.VariableTransformer;

public final class NewArrayExpression implements Expression {

    private final String desc;
    private final Type type;
    private final Expression[] dims;

    public NewArrayExpression(String desc, Expression dim) {
        if(desc == null || dim == null) {
            throw new NullPointerException();
        }
        this.desc = desc;
        this.type = Type.getType(desc);
        this.dims = new Expression[]{dim};
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

    @Override
    public List<VariableExpression> referencedVariables() {
        return Statement.gatherVersionedExpressions(dims);
    }

    @Override
    public Expression transform(VariableTransformer transformer) {
        Expression[] processedDims = new Expression[dims.length];
        for(int i = 0; i < dims.length; i++) {
            if(dims[i] != null) {
                processedDims[i] = dims[i].transform(transformer);
            }
        }
        return new NewArrayExpression(desc, processedDims);
    }
}
