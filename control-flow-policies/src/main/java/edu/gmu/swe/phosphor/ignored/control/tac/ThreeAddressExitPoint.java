package edu.gmu.swe.phosphor.ignored.control.tac;

import edu.columbia.cs.psl.phosphor.control.graph.ExitPoint;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.AnnotatedBasicBlock;
import edu.gmu.swe.phosphor.ignored.control.ssa.PropagationTransformer;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

public class ThreeAddressExitPoint extends ExitPoint implements ThreeAddressBasicBlock {

    private int index = -2;

    @Override
    public List<Statement> getThreeAddressStatements() {
        return Collections.emptyList();
    }

    @Override
    public List<Statement> getSSAStatements() {
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
    public void addPhiFunctionValues(Map<VariableExpression, VersionStack> versionStacks) {

    }

    @Override
    public void processStatements(Map<VariableExpression, VersionStack> versionStacks) {

    }

    @Override
    public AnnotatedBasicBlock createSSABasicBlock(PropagationTransformer transformer) {
        return new AnnotatedBasicBlock(getIndex(), Collections.emptyList());
    }
}