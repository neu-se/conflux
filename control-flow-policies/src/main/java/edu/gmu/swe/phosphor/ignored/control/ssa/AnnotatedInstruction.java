package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.converter.InsnConverter;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VersionedExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.IdleStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

public class AnnotatedInstruction {

    private static final InsnConverter converter = InsnConverter.getChain();

    private final AbstractInsnNode instruction;
    private final Frame<TypeValue> frame;
    private final Statement[] statements;
    private final Statement[] processedStatements;
    private AnnotatedBasicBlock basicBlock;

    public AnnotatedInstruction(AbstractInsnNode instruction, Frame<TypeValue> frame) {
        this.instruction = instruction;
        this.frame = frame;
        if(frame == null) {
            statements = new Statement[]{IdleStatement.NOP};
        } else {
            statements = converter.convert(instruction, frame);
        }
        processedStatements = new Statement[statements.length];
    }

    AbstractInsnNode getInstruction() {
        return instruction;
    }

    Frame<TypeValue> getFrame() {
        return frame;
    }

    Statement[] getStatements() {
        return statements;
    }

    Statement[] getProcessedStatements() {
        return processedStatements;
    }

    AnnotatedBasicBlock getBasicBlock() {
        return basicBlock;
    }

    void setBasicBlock(AnnotatedBasicBlock basicBlock) {
        this.basicBlock = basicBlock;
    }

    void processStatements(Map<VersionedExpression, VersionStack> versionStacks) {
        for(int i = 0; i < statements.length; i++) {
            processedStatements[i] = statements[i].process(versionStacks);
        }
    }
}
