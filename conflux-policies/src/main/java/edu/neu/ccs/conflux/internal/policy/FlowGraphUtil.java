package edu.neu.ccs.conflux.internal.policy;

import edu.columbia.cs.psl.phosphor.control.graph.BasicBlock;
import edu.columbia.cs.psl.phosphor.control.graph.DummyBasicBlock;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraphBuilder;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;

import java.util.function.Function;

public class FlowGraphUtil {

    private FlowGraphUtil() {
        throw new UnsupportedOperationException("FlowGraphUtil is a utility class not intended to be instantiated");
    }

    /**
     * @param controlFlowGraph graph whose containing loop sets are to be calculated
     * @param <V>              type of vertices in the graph
     * @return An mapping from each vertex in the specified control flow graph to an unmodifiable set of
     * natural loops that contain it
     * @throws NullPointerException if controlFlowGraph is null
     */
    public static <V> Map<V, Set<FlowGraph.NaturalLoop<V>>> calculateContainingLoops(FlowGraph<V> controlFlowGraph) {
        Set<FlowGraph.NaturalLoop<V>> loops = controlFlowGraph.getNaturalLoops();
        Map<V, Set<FlowGraph.NaturalLoop<V>>> containingLoopsMap = new HashMap<>();
        for(V vertex : controlFlowGraph.getVertices()) {
            containingLoopsMap.put(vertex, new HashSet<>());
        }
        for(FlowGraph.NaturalLoop<V> loop : loops) {
            for(V vertex : loop.getVertices()) {
                containingLoopsMap.get(vertex).add(loop);
            }
        }
        for(V key : containingLoopsMap.keySet()) {
            containingLoopsMap.put(key, Collections.unmodifiableSet(containingLoopsMap.get(key)));
        }
        return containingLoopsMap;
    }

    /**
     * @param graph  the graph whose paths are to be returned
     * @param source the first vertex in the paths
     * @param target the last vertex in the paths
     * @param <V>    type of vertices in the graph
     * @return the set of simple paths from the specified source vertex to the specified target vertex in the specified
     * graph; if the source and target vertices are equal returns a set containing the singleton list containing the
     * specified source vertex
     * @throws NullPointerException     if graph is null
     * @throws IllegalArgumentException if the specified graph does not contain either the source or the target vertex
     */
    public static <V> Set<List<V>> getAllSimplePaths(FlowGraph<V> graph, V source, V target) {
        if(!graph.getVertices().contains(source) || !graph.getVertices().contains(target)) {
            throw new IllegalArgumentException();
        }
        Set<List<V>> paths = new HashSet<>();
        search(graph, source, target, paths, new LinkedList<>(), new HashSet<>());
        return paths;
    }

    private static <V> void search(FlowGraph<V> graph, V vertex, V target, Set<List<V>> paths, LinkedList<V> currentPath,
                                   Set<V> visited) {
        if(visited.add(vertex)) {
            currentPath.addLast(vertex);
            if(vertex == target || (vertex != null && vertex.equals(target))) {
                paths.add(new ArrayList<>(currentPath));
            } else {
                for(V successor : graph.getSuccessors(vertex)) {
                    search(graph, successor, target, paths, currentPath, visited);
                }
            }
            currentPath.removeLast();
            visited.remove(vertex);
        }
    }

    /**
     * @param graph the graph whose vertices' instructions are to be mapped
     * @param <V>   type of vertices in the graph
     * @return a mapping from between an instruction in a non DummyBasicBlock in the specified graph to the basic block
     * that contains it in the graph
     * @throws NullPointerException if graph is null
     */
    public static <V extends BasicBlock> Map<AbstractInsnNode, V> createInsnBlockMap(FlowGraph<V> graph) {
        Map<AbstractInsnNode, V> insnBlockMap = new HashMap<>();
        for(V vertex : graph.getVertices()) {
            if(!(vertex instanceof DummyBasicBlock)) {
                AbstractInsnNode insn = vertex.getFirstInsn();
                while(insn != null) {
                    insnBlockMap.put(insn, vertex);
                    if(insn == vertex.getLastInsn()) {
                        break;
                    }
                    insn = insn.getNext();
                }
            }
        }
        return insnBlockMap;
    }

    /**
     * @param originalGraph the graph to be converted
     * @param converter     function used to create the new vertices
     * @param <U>           the type of the vertices in the new graph
     * @param <V>           the type of the vertices in the original graph
     * @return a flow graph with the same edges are the original graph but with each vertex converted to a new object
     * using the specified converter
     */
    public static <U, V> FlowGraph<U> convertVertices(FlowGraph<V> originalGraph, Function<? super V, U> converter) {
        FlowGraphBuilder<U> builder = new FlowGraphBuilder<>();
        Map<V, U> blockMap = new HashMap<>();
        for(V vertex : originalGraph.getVertices()) {
            blockMap.put(vertex, converter.apply(vertex));
            builder.addVertex(blockMap.get(vertex));
        }
        builder.addEntryPoint(blockMap.get(originalGraph.getEntryPoint()));
        builder.addExitPoint(blockMap.get(originalGraph.getExitPoint()));
        for(V source : blockMap.keySet()) {
            for(V target : originalGraph.getSuccessors(source)) {
                builder.addEdge(blockMap.get(source), blockMap.get(target));
            }
        }
        return builder.build();
    }

    public static AbstractInsnNode findNextPrecedableInstruction(AbstractInsnNode insn) {
        while(insn != null && (insn.getType() == AbstractInsnNode.FRAME || insn.getType() == AbstractInsnNode.LINE
                || insn.getType() == AbstractInsnNode.LABEL || insn.getOpcode() > 200)) {
            insn = insn.getNext();
        }
        return insn;
    }
}
