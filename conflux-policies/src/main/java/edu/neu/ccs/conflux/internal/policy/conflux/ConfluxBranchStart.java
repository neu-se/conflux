package edu.neu.ccs.conflux.internal.policy.conflux;

import edu.columbia.cs.psl.phosphor.control.standard.BranchStart;

public class ConfluxBranchStart extends BranchStart {

    private final LoopLevel level;

    public ConfluxBranchStart(LoopLevel level, int branchID) {
        super(branchID);
        if(level == null) {
            throw new NullPointerException();
        }
        this.level = level;
    }

    public LoopLevel getLevel() {
        return level;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof ConfluxBranchStart) || !super.equals(o)) {
            return false;
        }
        ConfluxBranchStart that = (ConfluxBranchStart) o;
        return level.equals(that.level);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + level.hashCode();
        return result;
    }
}
