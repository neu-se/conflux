package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;

public abstract class VersionedExpression implements Expression {

    private final int version;

    public VersionedExpression(int version) {
        this.version = version;
    }

    public abstract VersionedExpression setVersion(int version);

    public int getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof VersionedExpression)) {
            return false;
        }
        VersionedExpression that = (VersionedExpression) o;
        return version == that.version;
    }

    @Override
    public int hashCode() {
        return version;
    }

    @Override
    public VersionedExpression process(Map<VersionedExpression, VersionStack> versionStacks) {
        if(versionStacks.containsKey(this)) {
            return versionStacks.get(this).getCurrentExpression();
        } else {
            return this;
        }
    }
}
