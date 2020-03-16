package edu.gmu.swe.phosphor.ignored.control.tac;

import edu.columbia.cs.psl.phosphor.control.graph.EntryPoint;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.LinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.*;
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

    @Override
    public List<Statement> getSSAStatements() {
        return ssaStatements;
    }

    @Override
    public int getIndex() {
        return -1;
    }

    @Override
    public void addPhiFunctionForVariable(VariableExpression expression) {

    }

    @Override
    public void addPhiFunctionValues(Map<VariableExpression, VersionStack> versionStacks) {

    }

    @Override
    public void processStatements(Map<VariableExpression, VersionStack> versionStacks) {
        VersionAssigningTransformer transformer = new VersionAssigningTransformer(versionStacks);
        ssaStatements = new LinkedList<>();
        for(Statement statement : threeAddressStatements) {
            ssaStatements.add(statement.transform(transformer));
        }
        ssaStatements = Collections.unmodifiableList(ssaStatements);
    }

    @Override
    public AnnotatedBasicBlock createSSABasicBlock(PropagationTransformer transformer) {
        List<Statement> processedStatements = new LinkedList<>();
        for(Statement rawStatement : ssaStatements) {
            processedStatements.add(rawStatement.transform(transformer));
        }
        AnnotatedInstruction insn = new AnnotatedInstruction(null, ssaStatements, processedStatements);
        return new AnnotatedBasicBlock(getIndex(), Collections.singletonList(insn));
    }

    @Override
    public String toDotString(Map<Label, String> labelNames) {
        StringBuilder builder = new StringBuilder("\"ENTRY\\n");
        for(int i = 0; i < getThreeAddressStatements().size(); i++) {
            builder.append(getThreeAddressStatements().get(i).toString(labelNames).replace("\"", "\\\""));
            if(i != getThreeAddressStatements().size() - 1) {
                builder.append("\\n");
            }
        }
        return builder.append("\"").toString();
    }
}
