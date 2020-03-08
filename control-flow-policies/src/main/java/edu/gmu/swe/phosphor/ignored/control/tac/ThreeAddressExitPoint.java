package edu.gmu.swe.phosphor.ignored.control.tac;

import edu.columbia.cs.psl.phosphor.control.graph.ExitPoint;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.SSABasicBlock;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

public class ThreeAddressExitPoint extends ExitPoint implements ThreeAddressBasicBlock {

    @Override
    public List<Statement> getThreeAddressStatements() {
        return Collections.emptyList();
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
    public SSABasicBlock createSSABasicBlock(int index) {
        return new SSABasicBlock(Collections.emptyList(), Collections.emptyMap(), index);
    }
}