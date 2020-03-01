package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public class NewArrayExpression implements Expression {

    private final String desc;
    private final Expression[] dims;

    public NewArrayExpression(String desc, Expression dim) {
        this.desc = desc;
        this.dims = new Expression[]{dim};
    }

    public NewArrayExpression(String desc, Expression[] dims) {
        this.desc = desc;
        this.dims = dims.clone();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("new ")
                .append(desc);
        for(Expression dim : dims) {
            builder.append("[");
            if(dim != null) {
                builder.append(dim);
            }
            builder.append("]");
        }
        return builder.toString();
    }
}
