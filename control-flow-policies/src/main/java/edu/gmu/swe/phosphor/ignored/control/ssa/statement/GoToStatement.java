package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;

public final class GoToStatement implements Statement {

    private final Label target;

    public GoToStatement(Label target) {
        if(target == null) {
            throw new NullPointerException();
        }
        this.target = target;
    }

    @Override
    public String toString() {
        return "goto " + target;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof GoToStatement)) {
            return false;
        }
        GoToStatement that = (GoToStatement) o;
        return target.equals(that.target);
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }
}
