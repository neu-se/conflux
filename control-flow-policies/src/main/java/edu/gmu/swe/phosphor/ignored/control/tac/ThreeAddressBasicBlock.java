package edu.gmu.swe.phosphor.ignored.control.tac;

import edu.columbia.cs.psl.phosphor.control.graph.BasicBlock;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.SSABasicBlock;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VersionedExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

public interface ThreeAddressBasicBlock extends BasicBlock {

    List<Statement> getThreeAddressStatements();

    void addPhiFunctionForVariable(VersionedExpression expression);

    void addPhiFunctionValues(Map<VersionedExpression, VersionStack> versionStacks);

    void processStatements(Map<VersionedExpression, VersionStack> versionStacks);

    SSABasicBlock createSSABasicBlock();
}
