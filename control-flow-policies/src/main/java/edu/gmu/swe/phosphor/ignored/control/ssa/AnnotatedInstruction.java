package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.ArrayList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

public class AnnotatedInstruction {
    private final AbstractInsnNode instruction;
    private final List<Statement> statements;
    private AnnotatedBasicBlock basicBlock;

    public AnnotatedInstruction(AbstractInsnNode instruction, List<? extends Statement> statements) {
        this.instruction = instruction;
        this.statements = Collections.unmodifiableList(new ArrayList<>(statements));
    }

    public AnnotatedBasicBlock getBasicBlock() {
        return basicBlock;
    }

    void setBasicBlock(AnnotatedBasicBlock basicBlock) {
        this.basicBlock = basicBlock;
    }

    public AbstractInsnNode getInstruction() {
        return instruction;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for(Statement s : statements) {
            builder.append(s.toString());
            if(index != statements.size() - 1) {
                builder.append("\n");
            }
            index++;
        }
        return builder.toString();
    }
}
