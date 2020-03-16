package edu.gmu.swe.phosphor.ignored.control.tac;

import edu.columbia.cs.psl.phosphor.control.graph.SimpleBasicBlock;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LabelNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.PhiFunction;
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
    public List<Statement> getSSAStatements() {
        initializeSSAStatements();
        return ssaStatementsList;
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
    public void addPhiFunctionValues(Map<VariableExpression, VersionStack> versionStacks) {
        for(VariableExpression key : phiValues.keySet()) {
            phiValues.get(key).add(versionStacks.get(key).getCurrentExpression());
        }
    }

    @Override
    public void processStatements(Map<VariableExpression, VersionStack> versionStacks) {
        VersionAssigningTransformer transformer = new VersionAssigningTransformer(versionStacks);
        for(VariableExpression expression : phiValues.keySet()) {
            phiAssignees.put(expression, versionStacks.get(expression).createNewVersion());
        }
        for(AbstractInsnNode insn : threeAddressStatements.keySet()) {
            Statement[] originalStatements = threeAddressStatements.get(insn);
            Statement[] processedStatements = new Statement[originalStatements.length];
            for(int i = 0; i < originalStatements.length; i++) {
                processedStatements[i] = originalStatements[i].transform(transformer);
            }
            ssaStatements.put(insn, processedStatements);
        }
    }

    @Override
    public AnnotatedBasicBlock createSSABasicBlock(PropagationTransformer transformer) {
        initializeSSAStatements();
        List<AnnotatedInstruction> instructions = new LinkedList<>();
        List<Statement> processedPhiFunctions = new LinkedList<>();
        for(Statement phiFunction : phiFunctions) {
            processedPhiFunctions.add(phiFunction.transform(transformer));
        }
        boolean foundLabel = false;
        for(AbstractInsnNode insn : ssaStatements.keySet()) {
            List<Statement> rawStatements = new LinkedList<>();
            List<Statement> processedStatements = new LinkedList<>();
            for(Statement rawStatement : ssaStatements.get(insn)) {
                rawStatements.add(rawStatement);
                processedStatements.add(rawStatement.transform(transformer));
            }
            instructions.add(new AnnotatedInstruction(insn, rawStatements, processedStatements));
            if(insn instanceof LabelNode && !foundLabel) {
                foundLabel = true;
                instructions.add(new AnnotatedInstruction(null, phiFunctions, processedPhiFunctions));
            }
        }
        if(!foundLabel) {
            instructions.add(new AnnotatedInstruction(null, phiFunctions, processedPhiFunctions));
        }
        return new AnnotatedBasicBlock(getIndex(), instructions);
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
