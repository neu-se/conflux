package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;

public class GoToStatement implements Statement {

    private final Label target;

    public GoToStatement(Label target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "goto " + target;
    }
}
