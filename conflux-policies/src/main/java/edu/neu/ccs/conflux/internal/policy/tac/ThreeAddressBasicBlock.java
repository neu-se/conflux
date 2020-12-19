package edu.neu.ccs.conflux.internal.policy.tac;

import edu.columbia.cs.psl.phosphor.control.graph.BasicBlock;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.neu.ccs.conflux.internal.policy.ssa.AnnotatedBasicBlock;
import edu.neu.ccs.conflux.internal.policy.ssa.VersionAssigningVisitor;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.VariableExpression;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.Statement;

public interface ThreeAddressBasicBlock extends BasicBlock {

    List<Statement> getThreeAddressStatements();

    int getIndex();

    void addPhiFunctionForVariable(VariableExpression expression);

    void addPhiFunctionValues(VersionAssigningVisitor visitor);

    void processStatements(VersionAssigningVisitor visitor);

    AnnotatedBasicBlock createSSABasicBlock();
}
