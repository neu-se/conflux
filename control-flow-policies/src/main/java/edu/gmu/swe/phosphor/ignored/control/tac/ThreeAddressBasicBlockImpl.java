package edu.gmu.swe.phosphor.ignored.control.tac;

import edu.columbia.cs.psl.phosphor.control.graph.SimpleBasicBlock;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LabelNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.ProcessVersionStackTransformer;
import edu.gmu.swe.phosphor.ignored.control.ssa.SSABasicBlock;
import edu.gmu.swe.phosphor.ignored.control.ssa.VersionStack;
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

    public ThreeAddressBasicBlockImpl(AbstractInsnNode[] instructions, int identifier, ThreeAddressMethod method) {
        super(instructions, identifier);
        for(AbstractInsnNode insn : instructions) {
            threeAddressStatements.put(insn, method.getStatements(insn));
        }
        threeAddressStatementList = Collections.unmodifiableList(flattenMap(threeAddressStatements));
    }

    @Override
    public List<Statement> getThreeAddressStatements() {
        return threeAddressStatementList;
    }

    private List<AssignmentStatement> createPhiFunctions() {
        List<AssignmentStatement> phiFunctions = new LinkedList<>();
        for(VariableExpression expr : phiValues.keySet()) {
            PhiFunction phi = new PhiFunction(phiValues.get(expr));
            phiFunctions.add(new AssignmentStatement(phiAssignees.get(expr), phi));
        }
        return phiFunctions;
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
        ProcessVersionStackTransformer transformer = new ProcessVersionStackTransformer(versionStacks);
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
    public SSABasicBlock createSSABasicBlock() {
        List<Statement> statements = new LinkedList<>();
        boolean foundLabel = false;
        for(AbstractInsnNode insn : ssaStatements.keySet()) {
            for(Statement s : ssaStatements.get(insn)) {
                statements.add(s);
            }
            if(insn instanceof LabelNode && !foundLabel) {
                foundLabel = true;
                statements.addAll(createPhiFunctions());
            }
        }
        if(!foundLabel) {
            statements.addAll(createPhiFunctions());
        }
        return new SSABasicBlock(statements, ssaStatements);
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
