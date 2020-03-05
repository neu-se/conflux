package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.control.graph.BasicBlock;
import edu.columbia.cs.psl.phosphor.control.graph.InstructionTextifier;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.InsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collection;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashMap;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VersionedExpression;

public class AnnotatedBasicBlock implements BasicBlock {

    /**
     * An identifying number for this basic block
     */
    private final int identifier;

    /**
     * The instructions in this block
     */
    private final AnnotatedInstruction[] annotatedInstructions;

    private final Map<VersionedExpression, PhiFunction> phiFunctions = new HashMap<>();

    /**
     * Constructs a new basic block that represents the specified instruction sequence.
     *
     * @param annotatedInstructions the sequence of instructions in the basic block being constructed
     * @param identifier            a number used to identify the basic block being constructed
     * @throws NullPointerException if instructions is null
     */
    public AnnotatedBasicBlock(AnnotatedInstruction[] annotatedInstructions, int identifier) {
        this.annotatedInstructions = annotatedInstructions.clone();
        this.identifier = identifier;
    }

    public int getIdentifier() {
        return identifier;
    }

    Frame<TypeValue> getFirstFrame() {
        if(annotatedInstructions.length == 0) {
            return null;
        }
        return annotatedInstructions[0].getFrame();
    }

    boolean addPhiFunctionForExpression(VersionedExpression expression) {
        if(phiFunctions.containsKey(expression)) {
            return false;
        } else {
            phiFunctions.put(expression, new PhiFunction());
            return true;
        }
    }

    void addPossiblePhiFunctionValues(Map<VersionedExpression, VersionStack> versionStacks) {
        for(VersionedExpression key : phiFunctions.keySet()) {
            phiFunctions.get(key).getPossibleValues().add(versionStacks.get(key).getCurrentExpression());
        }
    }

    private void assignPhiFunctionLeftHandSides(Map<VersionedExpression, VersionStack> versionStacks) {
        for(VersionedExpression key : phiFunctions.keySet()) {
            phiFunctions.get(key).setLeftHandSide(versionStacks.get(key).createNewVersion());
        }
    }

    AnnotatedInstruction[] getAnnotatedInstructions() {
        return annotatedInstructions;
    }

    Collection<PhiFunction> getPhiFunctions() {
        return phiFunctions.values();
    }

    void processStatements(Map<VersionedExpression, VersionStack> versionStacks) {
        assignPhiFunctionLeftHandSides(versionStacks);
        for(AnnotatedInstruction annotatedInstruction : annotatedInstructions) {
            annotatedInstruction.processStatements(versionStacks);
        }
    }

    @Override
    public AbstractInsnNode getFirstInsn() {
        if(annotatedInstructions.length == 0) {
            return new InsnNode(Opcodes.NOP);
        }
        return annotatedInstructions[0].getInstruction();
    }

    @Override
    public AbstractInsnNode getLastInsn() {
        if(annotatedInstructions.length == 0) {
            return new InsnNode(Opcodes.NOP);
        }
        return annotatedInstructions[annotatedInstructions.length - 1].getInstruction();
    }

    @Override
    public String toString() {
        return String.format("BasicBlock{#%d: %s - %s}", identifier, getFirstInsn().getClass().getSimpleName(),
                getLastInsn().getClass().getSimpleName());
    }

    @Override
    public String toDotString(Map<Label, String> labelNames) {
        StringBuilder builder = new StringBuilder("\"");
        for(AnnotatedInstruction annotatedInstruction : annotatedInstructions) {
            AbstractInsnNode instruction = annotatedInstruction.getInstruction();
            String s = InstructionTextifier.getInstance().convertInstructionToString(instruction, labelNames);
            builder.append(s).append("\\n");
        }
        return builder.append('"').toString();
    }
}
