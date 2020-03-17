package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

public class AnnotatedInstruction {
    private final AbstractInsnNode originalInstruction;
    private final List<Statement> rawStatements;
    private final List<Statement> processedStatements;
    private final Map<Statement, Statement> rawToProcessedMap = new HashMap<>();
    private final Map<Statement, Statement> processedToRawMap = new HashMap<>();

    public AnnotatedInstruction(AbstractInsnNode originalInstruction, List<? extends Statement> rawStatements,
                                List<? extends Statement> processedStatements) {
        this.originalInstruction = originalInstruction;
        this.rawStatements = Collections.unmodifiableList(new ArrayList<>(rawStatements));
        this.processedStatements = Collections.unmodifiableList(new ArrayList<>(processedStatements));
        for(int i = 0; i < rawStatements.size(); i++) {
            rawToProcessedMap.put(rawStatements.get(i), processedStatements.get(i));
            processedToRawMap.put(processedStatements.get(i), rawStatements.get(i));
        }
    }

    public AbstractInsnNode getOriginalInstruction() {
        return originalInstruction;
    }

    public List<Statement> getRawStatements() {
        return rawStatements;
    }

    public List<Statement> getProcessedStatements() {
        return processedStatements;
    }

    public Statement getRawStatement(Statement processedStatement) {
        return processedToRawMap.get(processedStatement);
    }

    public Statement getProcessedStatement(Statement rawStatement) {
        return rawToProcessedMap.get(rawStatement);
    }

    public String getRawStatementString() {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for(Statement s : rawStatements) {
            builder.append(s.toString());
            if(index != rawStatements.size() - 1) {
                builder.append("\n");
            }
            index++;
        }
        return builder.toString();
    }

    public String getProcessedStatementString() {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for(Statement s : processedStatements) {
            builder.append(s.toString());
            if(index != processedStatements.size() - 1) {
                builder.append("\n");
            }
            index++;
        }
        return builder.toString();
    }
}
