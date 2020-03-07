package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.VariableTransformer;

public abstract class VariableExpression implements Expression {

    private final int version;

    public VariableExpression(int version) {
        this.version = version;
    }

    public abstract VariableExpression setVersion(int version);

    public int getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof VariableExpression)) {
            return false;
        }
        VariableExpression that = (VariableExpression) o;
        return version == that.version;
    }

    @Override
    public int hashCode() {
        return version;
    }

    @Override
    public List<VariableExpression> referencedVariables() {
        return Collections.singletonList(this);
    }

    @Override
    public Expression transform(VariableTransformer transformer) {
        return transformer.transformUse(this);
    }
}
