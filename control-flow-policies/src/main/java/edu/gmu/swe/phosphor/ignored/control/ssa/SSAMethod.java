package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.control.graph.BasicBlock;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraphBuilder;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VersionedExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;
import edu.gmu.swe.phosphor.ignored.control.tac.ThreeAddressBasicBlock;
import edu.gmu.swe.phosphor.ignored.control.tac.ThreeAddressControlFlowGraphCreator;
import edu.gmu.swe.phosphor.ignored.control.tac.ThreeAddressMethod;

/**
 * Uses algorithms from the following for placing phi functions and renaming variables:
 * <p>Ron Cytron, Jeanne Ferrante, Barry K. Rosen, Mark N. Wegman, and F. Kenneth Zadeck.
 * 1991. Efficiently computing static single assignment form and the control dependence graph.
 * ACM Trans. Program. Lang. Syst. 13, 4 (October 1991), 451–490. DOI: https://doi.org/10.1145/115372.115320
 */
public class SSAMethod {

    private final FlowGraph<SSABasicBlock> controlFlowGraph;

    public SSAMethod(ThreeAddressMethod method) {
        FlowGraph<ThreeAddressBasicBlock> threeAddressCFG = new ThreeAddressControlFlowGraphCreator(method)
                .createControlFlowGraph(method.getOriginalMethod(), method.calculateExplicitExceptions());
        placePhiFunctions(method, threeAddressCFG);
        renameVariables(method, threeAddressCFG);
        controlFlowGraph = createControlFlowGraph(threeAddressCFG);
    }

    private FlowGraph<SSABasicBlock> createControlFlowGraph(FlowGraph<ThreeAddressBasicBlock> threeAddressCFG) {
        FlowGraphBuilder<SSABasicBlock> builder = new FlowGraphBuilder<>();
        Map<ThreeAddressBasicBlock, SSABasicBlock> blockMap = new HashMap<>();
        for(ThreeAddressBasicBlock vertex : threeAddressCFG.getVertices()) {
            blockMap.put(vertex, vertex.createSSABasicBlock());
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

    public FlowGraph<SSABasicBlock> getControlFlowGraph() {
        return controlFlowGraph;
    }

    private void placePhiFunctions(ThreeAddressMethod method, FlowGraph<ThreeAddressBasicBlock> cfg) {
        Map<VersionedExpression, Set<ThreeAddressBasicBlock>> persistentVarDefs = locatePersistentVarDefs(method, cfg);
        for(VersionedExpression expr : persistentVarDefs.keySet()) {
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
        Map<VersionedExpression, VersionStack> versionStacks = new HashMap<>();
        for(VersionedExpression expression : method.collectDefinedVariables()) {
            versionStacks.put(expression, new VersionStack(expression));
        }
        search(cfg.getEntryPoint(), versionStacks, cfg);
    }

    private void search(ThreeAddressBasicBlock block, Map<VersionedExpression, VersionStack> versionStacks, FlowGraph<ThreeAddressBasicBlock> cfg) {
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

    private Map<VersionedExpression, Set<ThreeAddressBasicBlock>> locatePersistentVarDefs(ThreeAddressMethod method,
                                                                                          FlowGraph<ThreeAddressBasicBlock> cfg) {
        Map<VersionedExpression, Set<ThreeAddressBasicBlock>> persistentVarDefs = new HashMap<>();
        for(ThreeAddressBasicBlock block : cfg.getVertices()) {
            for(Statement s : block.getThreeAddressStatements()) {
                if(s.definesVariable()) {
                    VersionedExpression expr = s.definedVariable();
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

    private boolean isPersistentDefinition(ThreeAddressMethod method, ThreeAddressBasicBlock block, VersionedExpression expr,
                                           FlowGraph<ThreeAddressBasicBlock> cfg) {
        for(BasicBlock successor : cfg.getSuccessors(block)) {
            if(method.isDefinedAtInstruction(successor.getFirstInsn(), expr)) {
                return true;
            }
        }
        return false;
    }
}
