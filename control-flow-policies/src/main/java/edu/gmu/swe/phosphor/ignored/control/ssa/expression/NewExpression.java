package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.VariableTransformer;

public final class NewExpression implements Expression {

    private final String desc;

    public NewExpression(String desc) {
        if(desc == null) {
            throw new NullPointerException();
        }
        this.desc = desc;
    }

    @Override
    public String toString() {
        return String.format("(new %s)", desc);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof NewExpression)) {
            return false;
        }
        NewExpression that = (NewExpression) o;
        return desc.equals(that.desc);
    }

    @Override
    public int hashCode() {
        return desc.hashCode();
    }

    @Override
    public List<VariableExpression> referencedVariables() {
        return Collections.emptyList();
    }

    @Override
    public Expression transform(VariableTransformer transformer) {
        return this;
    }
}
