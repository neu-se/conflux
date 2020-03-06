package edu.gmu.swe.phosphor.ignored.control.tac;

import edu.columbia.cs.psl.phosphor.control.graph.EntryPoint;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.LinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.SSABasicBlock;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VersionedExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

public class ThreeAddressEntryPoint extends EntryPoint implements ThreeAddressBasicBlock {

    private final List<Statement> threeAddressStatements;
    private List<Statement> ssaStatements = Collections.emptyList();

    public ThreeAddressEntryPoint(ThreeAddressMethod method) {
        threeAddressStatements = Collections.unmodifiableList(method.getParameterDefinitions());
    }

    @Override
    public List<Statement> getThreeAddressStatements() {
        return threeAddressStatements;
    }

    @Override
    public void addPhiFunctionForVariable(VersionedExpression expression) {

    }

    @Override
    public void addPhiFunctionValues(Map<VersionedExpression, VersionStack> versionStacks) {

    }

    @Override
    public void processStatements(Map<VersionedExpression, VersionStack> versionStacks) {
        ssaStatements = new LinkedList<>();
        for(Statement statement : threeAddressStatements) {
            ssaStatements.add(statement.process(versionStacks));
        }
        ssaStatements = Collections.unmodifiableList(ssaStatements);
    }

    @Override
    public SSABasicBlock createSSABasicBlock() {
        return new SSABasicBlock(ssaStatements, Collections.emptyMap());
    }
}
