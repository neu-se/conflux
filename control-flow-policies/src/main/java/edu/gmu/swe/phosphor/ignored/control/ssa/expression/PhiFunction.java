package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;

import java.util.Arrays;

public class PhiFunction implements Expression {

    private final Expression[] values;

    public PhiFunction(Set<? extends Expression> values) {
        this.values = values.toArray(new Expression[0]);
    }

    public Expression[] getValues() {
        return values.clone();
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
        StringBuilder builder = new StringBuilder("phi<");
        for(int i = 0; i < values.length; i++) {
            builder.append(values[i]);
            if((i + 1) < values.length) {
                builder.append(", ");
            }
        }
        return builder.append(">").toString();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof PhiFunction)) {
            return false;
        }
        PhiFunction that = (PhiFunction) o;
        return Arrays.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }
}
