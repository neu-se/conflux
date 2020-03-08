package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.control.graph.*;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.Expression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.PhiFunction;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.AssignmentStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;
import edu.gmu.swe.phosphor.ignored.control.tac.*;

/**
 * Uses algorithms from the following for placing phi functions and renaming variables:
 * <p>Ron Cytron, Jeanne Ferrante, Barry K. Rosen, Mark N. Wegman, and F. Kenneth Zadeck.
 * 1991. Efficiently computing static single assignment form and the control dependence graph.
 * ACM Trans. Program. Lang. Syst. 13, 4 (October 1991), 451–490. DOI: https://doi.org/10.1145/115372.115320
 */
public class SSAMethod {

    private final FlowGraph<SSABasicBlock> controlFlowGraph;
    private final Map<AbstractInsnNode, List<Statement>> statementMap;
    private final List<Statement> parameterDefinitions;

    public SSAMethod(ThreeAddressMethod method) {
        FlowGraph<ThreeAddressBasicBlock> threeAddressCFG = new ThreeAddressControlFlowGraphCreator(method)
                .createControlFlowGraph(method.getOriginalMethod(), method.calculateExplicitExceptions());
        placePhiFunctions(method, threeAddressCFG);
        renameVariables(method, threeAddressCFG);
        controlFlowGraph = createControlFlowGraph(threeAddressCFG);
        this.parameterDefinitions = initializeParameterDefinitions(threeAddressCFG);
        this.statementMap = initializeStatementMap(threeAddressCFG);
    }

    private List<Statement> initializeParameterDefinitions(FlowGraph<ThreeAddressBasicBlock> threeAddressCFG) {
        ThreeAddressEntryPoint entry = (ThreeAddressEntryPoint) threeAddressCFG.getEntryPoint();
        return entry.getSsaStatements();
    }

    public FlowGraph<SSABasicBlock> getControlFlowGraph() {
        return controlFlowGraph;
    }

    public Statement[] getStatements(AbstractInsnNode insn) {
        return statementMap.get(insn).toArray(new Statement[0]);
    }

    public List<Statement> getParameterDefinitions() {
        return parameterDefinitions;
    }

    private void placePhiFunctions(ThreeAddressMethod method, FlowGraph<ThreeAddressBasicBlock> cfg) {
        Map<VariableExpression, Set<ThreeAddressBasicBlock>> persistentVarDefs = locatePersistentVarDefs(method, cfg);
        for(VariableExpression expr : persistentVarDefs.keySet()) {
            LinkedList<ThreeAddressBasicBlock> workingSet = new LinkedList<>(persistentVarDefs.get(expr));
            Set<ThreeAddressBasicBlock> visited = new HashSet<>(workingSet);
            while(!workingSet.isEmpty()) {
                ThreeAddressBasicBlock x = workingSet.poll();
                for(ThreeAddressBasicBlock y : cfg.getDominanceFrontiers().get(x)) {
                    if(method.isDefinedAtInstruction(y.getFirstInsn(), expr)) {
                        y.addPhiFunctionForVariable(expr);
                    }
                    if(visited.add(y)) {
                        workingSet.add(y);
                    }
                }
            }
        }
    }

    private void renameVariables(ThreeAddressMethod method, FlowGraph<ThreeAddressBasicBlock> cfg) {
        Map<VariableExpression, VersionStack> versionStacks = new HashMap<>();
        for(VariableExpression expression : method.collectDefinedVariables()) {
            versionStacks.put(expression, new VersionStack(expression));
        }
        search(cfg.getEntryPoint(), versionStacks, cfg);
    }

    private void search(ThreeAddressBasicBlock block, Map<VariableExpression, VersionStack> versionStacks,
                        FlowGraph<ThreeAddressBasicBlock> cfg) {
        for(VersionStack stack : versionStacks.values()) {
            stack.processingBlock();
        }
        block.processStatements(versionStacks);
        for(ThreeAddressBasicBlock successor : cfg.getSuccessors(block)) {
            successor.addPhiFunctionValues(versionStacks);
        }
        for(ThreeAddressBasicBlock child : cfg.getDominatorTree().get(block)) {
            search(child, versionStacks, cfg);
        }
        for(VersionStack stack : versionStacks.values()) {
            stack.finishedProcessingBlock();
        }
    }

    private Map<VariableExpression, Set<ThreeAddressBasicBlock>> locatePersistentVarDefs(ThreeAddressMethod method,
                                                                                         FlowGraph<ThreeAddressBasicBlock> cfg) {
        Map<VariableExpression, Set<ThreeAddressBasicBlock>> persistentVarDefs = new HashMap<>();
        for(ThreeAddressBasicBlock block : cfg.getVertices()) {
            for(Statement s : block.getThreeAddressStatements()) {
                if(s.definesVariable()) {
                    VariableExpression expr = s.getDefinedVariable();
                    if(isPersistentDefinition(method, block, expr, cfg)) {
                        if(!persistentVarDefs.containsKey(expr)) {
                            persistentVarDefs.put(expr, new HashSet<>());
                        }
                        persistentVarDefs.get(expr).add(block);
                    }
                }
            }
        }
        return persistentVarDefs;
    }

    private boolean isPersistentDefinition(ThreeAddressMethod method, ThreeAddressBasicBlock block, VariableExpression expr,
                                           FlowGraph<ThreeAddressBasicBlock> cfg) {
        for(BasicBlock successor : cfg.getSuccessors(block)) {
            if(method.isDefinedAtInstruction(successor.getFirstInsn(), expr)) {
                return true;
            }
        }
        return false;
    }

    public Map<VariableExpression, Expression> propagateVariables() {
        Map<VariableExpression, Expression> definitions = new HashMap<>();
        for(SSABasicBlock block : controlFlowGraph.getVertices()) {
            for(Statement statement : block.getStatements()) {
                if(statement.definesVariable() && statement instanceof AssignmentStatement) {
                    definitions.put(statement.getDefinedVariable(),
                            ((AssignmentStatement) statement).getRightHandSide());
                }
            }
        }
        PropagationTransformer transformer = new PropagationTransformer(definitions);
        boolean changed;
        do {
            changed = false;
            for(VariableExpression assignee : definitions.keySet()) {
                Expression assigned = definitions.get(assignee);
                Expression transformed = assigned.transform(transformer, assignee);
                if(!assigned.equals(transformed)) {
                    changed = true;
                    definitions.put(assignee, transformed);
                }
            }
        } while(changed);
        return definitions;
    }

    public List<Statement> createStatementList() {
        List<Statement> list = new LinkedList<>();
        List<SSABasicBlock> blocks = new LinkedList<>(getControlFlowGraph().getVertices());
        Collections.sort(blocks, (object1, object2) -> Integer.compare(object1.getIndex(), object2.getIndex()));
        for(SSABasicBlock block : blocks) {
            list.addAll(block.getStatements());
        }
        return list;
    }

    private static Map<AbstractInsnNode, List<Statement>> initializeStatementMap(FlowGraph<ThreeAddressBasicBlock> threeAddressCFG) {
        Map<AbstractInsnNode, List<Statement>> statementMap = new HashMap<>();
        for(ThreeAddressBasicBlock vertex : threeAddressCFG.getVertices()) {
            if(vertex instanceof ThreeAddressBasicBlockImpl) {
                ThreeAddressBasicBlockImpl block = (ThreeAddressBasicBlockImpl) vertex;
                for(AbstractInsnNode insn : block.getSsaStatements().keySet()) {
                    statementMap.put(insn, Collections.unmodifiableList(Arrays.asList(block.getSsaStatements().get(insn))));
                }
            }
        }
        return Collections.unmodifiableMap(statementMap);
    }

    private static FlowGraph<SSABasicBlock> createControlFlowGraph(FlowGraph<ThreeAddressBasicBlock> threeAddressCFG) {
        FlowGraphBuilder<SSABasicBlock> builder = new FlowGraphBuilder<>();
        Map<ThreeAddressBasicBlock, SSABasicBlock> blockMap = new HashMap<>();
        for(ThreeAddressBasicBlock vertex : threeAddressCFG.getVertices()) {
            int rank = -2;
            if(vertex instanceof EntryPoint) {
                rank = -1;
            } else if(vertex instanceof ExitPoint) {
                rank = threeAddressCFG.getVertices().size() - 1;
            } else if(vertex instanceof SimpleBasicBlock) {
                rank = ((SimpleBasicBlock) vertex).getIdentifier() + 1;
            }
            blockMap.put(vertex, vertex.createSSABasicBlock(rank));
            builder.addVertex(blockMap.get(vertex));
        }
        builder.addEntryPoint(blockMap.get(threeAddressCFG.getEntryPoint()));
        builder.addExitPoint(blockMap.get(threeAddressCFG.getExitPoint()));
        for(ThreeAddressBasicBlock source : blockMap.keySet()) {
            for(ThreeAddressBasicBlock target : threeAddressCFG.getSuccessors(source)) {
                builder.addEdge(blockMap.get(source), blockMap.get(target));
            }
        }
        return builder.build();
    }

    public static boolean isPhiFunctionStatement(Statement statement) {
        return statement instanceof AssignmentStatement
                && ((AssignmentStatement) statement).getRightHandSide() instanceof PhiFunction;
    }

}
