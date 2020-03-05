package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;

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
        return String.format("new %s", desc);
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
    public NewExpression process(Map<VersionedExpression, VersionStack> versionStacks) {
        return this;
    }
}
