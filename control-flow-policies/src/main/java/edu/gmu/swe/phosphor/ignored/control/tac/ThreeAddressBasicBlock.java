package edu.gmu.swe.phosphor.ignored.control.tac;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.SSABasicBlock;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

public interface ThreeAddressBasicBlock {

    List<Statement> getThreeAddressStatements();

    void addPhiFunctionForVariable(VariableExpression expression);

    void addPhiFunctionValues(Map<VariableExpression, VersionStack> versionStacks);

    void processStatements(Map<VariableExpression, VersionStack> versionStacks);

    SSABasicBlock createSSABasicBlock(int index);
}
