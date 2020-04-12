package edu.gmu.swe.phosphor.ignored.control.tac;

import edu.columbia.cs.psl.phosphor.control.graph.BasicBlock;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.AnnotatedBasicBlock;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionAssigningVisitor;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

public interface ThreeAddressBasicBlock extends BasicBlock {

    List<Statement> getThreeAddressStatements();

    int getIndex();

    void addPhiFunctionForVariable(VariableExpression expression);

    void addPhiFunctionValues(VersionAssigningVisitor visitor);

    void processStatements(VersionAssigningVisitor visitor);

    AnnotatedBasicBlock createSSABasicBlock();
}
