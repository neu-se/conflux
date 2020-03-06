package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

public class SSABasicBlock {

    private final List<Statement> statements;
    private final Map<AbstractInsnNode, List<Statement>> statementMap;

    public SSABasicBlock(List<Statement> statements, Map<AbstractInsnNode, Statement[]> statementMap) {
        this.statements = Collections.unmodifiableList(new LinkedList<>(statements));
        Map<AbstractInsnNode, List<Statement>> temp = new HashMap<>();
        for(AbstractInsnNode insn : statementMap.keySet()) {
            temp.put(insn, Collections.unmodifiableList(Arrays.asList(statementMap.get(insn))));
        }
        this.statementMap = Collections.unmodifiableMap(temp);
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public Map<AbstractInsnNode, List<Statement>> getStatementMap() {
        return statementMap;
    }
}
