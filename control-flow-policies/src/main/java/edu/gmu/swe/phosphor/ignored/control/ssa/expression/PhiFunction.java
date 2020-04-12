package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;

public final class PhiFunction implements Expression {

    private final Set<Expression> values;

    public PhiFunction(Set<? extends Expression> values) {
        this.values = Collections.unmodifiableSet(new HashSet<>(values));
    }

    public Set<Expression> getValues() {
        return values;
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
        return new StringBuilder("phi<")
                .append(values)
                .append(">")
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof PhiFunction)) {
            return false;
        }
        PhiFunction that = (PhiFunction) o;
        return values != null ? values.equals(that.values) : that.values == null;
    }

    @Override
    public int hashCode() {
        return values != null ? values.hashCode() : 0;
    }
}
