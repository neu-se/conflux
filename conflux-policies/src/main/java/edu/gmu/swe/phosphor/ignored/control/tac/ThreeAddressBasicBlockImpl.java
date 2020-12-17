package edu.gmu.swe.phosphor.ignored.control.tac;

import edu.columbia.cs.psl.phosphor.control.graph.SimpleBasicBlock;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LabelNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.AnnotatedBasicBlock;
import edu.gmu.swe.phosphor.ignored.control.ssa.AnnotatedInstruction;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionAssigningVisitor;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.PhiFunction;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.StackElement;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.AssignmentStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

public class ThreeAddressBasicBlockImpl extends SimpleBasicBlock implements ThreeAddressBasicBlock {

    private final Map<VariableExpression, Set<VariableExpression>> phiValues = new HashMap<>();
    private final Map<VariableExpression, VariableExpression> phiAssignees = new HashMap<>();
    private final Map<AbstractInsnNode, Statement[]> threeAddressStatements = new LinkedHashMap<>();
    private final Map<AbstractInsnNode, Statement[]> ssaStatements = new LinkedHashMap<>();
    private final List<Statement> threeAddressStatementList;
    private List<AssignmentStatement> phiFunctions;
    private List<Statement> ssaStatementsList;

    public ThreeAddressBasicBlockImpl(AbstractInsnNode[] instructions, int index, ThreeAddressMethod method) {
        super(instructions, index);
        if(instructions.length > 0 && method.getExceptionHandlerStarts().containsKey(instructions[0])) {
            AssignmentStatement s = new AssignmentStatement(new StackElement(0),
                    method.getExceptionHandlerStarts().get(instructions[0]));
            threeAddressStatements.put(null, new Statement[]{s});
        }
        for(AbstractInsnNode insn : instructions) {
            threeAddressStatements.put(insn, method.getStatements(insn));
        }
        threeAddressStatementList = Collections.unmodifiableList(flattenMap(threeAddressStatements));
    }

    @Override
    public List<Statement> getThreeAddressStatements() {
        return threeAddressStatementList;
    }

    @Override
    public int getIndex() {
        return getIdentifier() + 1;
    }

    private void initializePhiFunctions() {
        if(phiFunctions == null) {
            phiFunctions = new LinkedList<>();
            for(VariableExpression expr : phiValues.keySet()) {
                PhiFunction phi = new PhiFunction(phiValues.get(expr));
                phiFunctions.add(new AssignmentStatement(phiAssignees.get(expr), phi));
            }
        }
    }

    private void initializeSSAStatements() {
        if(ssaStatementsList == null) {
            initializePhiFunctions();
            ssaStatementsList = new LinkedList<>();
            boolean foundLabel = false;
            for(AbstractInsnNode insn : ssaStatements.keySet()) {
                for(Statement s : ssaStatements.get(insn)) {
                    ssaStatementsList.add(s);
                }
                if(insn instanceof LabelNode && !foundLabel) {
                    foundLabel = true;
                    ssaStatementsList.addAll(phiFunctions);
                }
            }
            if(!foundLabel) {
                ssaStatementsList.addAll(phiFunctions);
            }
            ssaStatementsList = Collections.unmodifiableList(ssaStatementsList);
        }
    }

    @Override
    public void addPhiFunctionForVariable(VariableExpression expression) {
        if(!phiValues.containsKey(expression)) {
            phiValues.put(expression, new HashSet<>());
        }
    }

    @Override
    public void addPhiFunctionValues(VersionAssigningVisitor visitor) {
        for(VariableExpression key : phiValues.keySet()) {
            if(visitor.hasCurrentExpression(key)) {
                phiValues.get(key).add(visitor.getCurrentExpression(key));
            }
        }
    }

    @Override
    public void processStatements(VersionAssigningVisitor visitor) {
        for(VariableExpression expression : phiValues.keySet()) {
            phiAssignees.put(expression, visitor.createNewVersion(expression));
        }
        for(AbstractInsnNode insn : threeAddressStatements.keySet()) {
            Statement[] originalStatements = threeAddressStatements.get(insn);
            Statement[] processedStatements = new Statement[originalStatements.length];
            for(int i = 0; i < originalStatements.length; i++) {
                processedStatements[i] = originalStatements[i].accept(visitor);
            }
            ssaStatements.put(insn, processedStatements);
        }
    }

    @Override
    public AnnotatedBasicBlock createSSABasicBlock() {
        initializeSSAStatements();
        List<AnnotatedInstruction> instructions = new LinkedList<>();
        boolean foundLabel = false;
        for(AbstractInsnNode insn : ssaStatements.keySet()) {
            instructions.add(new AnnotatedInstruction(insn, Arrays.asList(ssaStatements.get(insn))));
            if(insn instanceof LabelNode && !foundLabel) {
                foundLabel = true;
                if(!phiFunctions.isEmpty()) {
                    instructions.add(new AnnotatedInstruction(null, phiFunctions));
                }
            }
        }
        if(!foundLabel && !phiFunctions.isEmpty()) {
            if(!phiFunctions.isEmpty()) {
                instructions.add(new AnnotatedInstruction(null, phiFunctions));
            }
        }
        return new AnnotatedBasicBlock(getIndex(), instructions);
    }

    @Override
    public String toDotString(Map<Label, String> labelNames) {
        StringBuilder builder = new StringBuilder("\"");
        for(int i = 0; i < getThreeAddressStatements().size(); i++) {
            builder.append(getThreeAddressStatements().get(i).toString(labelNames).replace("\"", "\\\""));
            if(i != getThreeAddressStatements().size() - 1) {
                builder.append("\\n");
            }
        }
        return builder.append("\"").toString();
    }

    public static <T> List<T> flattenMap(Map<?, T[]> map) {
        List<T> list = new LinkedList<>();
        for(Object o : map.keySet()) {
            for(T element : map.get(o)) {
                list.add(element);
            }
        }
        return list;
    }
}
