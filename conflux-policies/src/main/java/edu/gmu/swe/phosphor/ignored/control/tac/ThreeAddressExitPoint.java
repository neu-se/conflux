package edu.gmu.swe.phosphor.ignored.control.tac;

import edu.columbia.cs.psl.phosphor.control.graph.ExitPoint;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.AnnotatedBasicBlock;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionAssigningVisitor;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

public class ThreeAddressExitPoint extends ExitPoint implements ThreeAddressBasicBlock {

    private int index;

    @Override
    public List<Statement> getThreeAddressStatements() {
        return Collections.emptyList();
    }

    @Override
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public void addPhiFunctionForVariable(VariableExpression expression) {

    }

    @Override
    public void addPhiFunctionValues(VersionAssigningVisitor visitor) {

    }

    @Override
    public void processStatements(VersionAssigningVisitor visitor) {

    }

    @Override
    public AnnotatedBasicBlock createSSABasicBlock() {
        return new AnnotatedBasicBlock(getIndex(), Collections.emptyList());
    }
}