package edu.gmu.swe.phosphor.ignored.control.tac;

import edu.columbia.cs.psl.phosphor.control.graph.EntryPoint;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.LinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.ProcessVersionStackTransformer;
import edu.gmu.swe.phosphor.ignored.control.ssa.SSABasicBlock;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;
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

    public List<Statement> getSsaStatements() {
        return ssaStatements;
    }

    @Override
    public void addPhiFunctionForVariable(VariableExpression expression) {

    }

    @Override
    public void addPhiFunctionValues(Map<VariableExpression, VersionStack> versionStacks) {

    }

    @Override
    public void processStatements(Map<VariableExpression, VersionStack> versionStacks) {
        ProcessVersionStackTransformer transformer = new ProcessVersionStackTransformer(versionStacks);
        ssaStatements = new LinkedList<>();
        for(Statement statement : threeAddressStatements) {
            ssaStatements.add(statement.transform(transformer));
        }
        ssaStatements = Collections.unmodifiableList(ssaStatements);
    }

    @Override
    public SSABasicBlock createSSABasicBlock(int index) {
        return new SSABasicBlock(ssaStatements, Collections.emptyMap(), index);
    }
}
