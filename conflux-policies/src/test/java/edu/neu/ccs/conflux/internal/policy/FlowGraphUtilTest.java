package edu.neu.ccs.conflux.internal.policy;

import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraphBuilder;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class FlowGraphUtilTest {

    @Test
    public void testGetAllSimplePathsHasPaths() {
        FlowGraph<Integer> graph = new FlowGraphBuilder<Integer>()
                .addEntryPoint(0)
                .addExitPoint(4)
                .addEdge(0, 1)
                .addEdge(0, 3)
                .addEdge(0, 5)
                .addEdge(1, 2)
                .addEdge(2, 1)
                .addEdge(2, 3)
                .addEdge(3, 4)
                .addEdge(5, 3)
                .addEdge(5, 6)
                .addEdge(6, 3)
                .addEdge(6, 4)
                .build();
        Set<List<Integer>> actualPaths = FlowGraphUtil.getAllSimplePaths(graph, 0, 3);
        Set<List<Integer>> expectedPaths = new HashSet<>();
        expectedPaths.add(Arrays.asList(0, 1, 2, 3));
        expectedPaths.add(Arrays.asList(0, 3));
        expectedPaths.add(Arrays.asList(0, 5, 3));
        expectedPaths.add(Arrays.asList(0, 5, 6, 3));
        assertEquals(expectedPaths, actualPaths);
    }

    @Test
    public void testGetAllSimplePathsNoPath() {
        FlowGraph<Integer> graph = new FlowGraphBuilder<Integer>()
                .addEntryPoint(0)
                .addExitPoint(4)
                .addEdge(0, 1)
                .addEdge(0, 3)
                .addEdge(0, 5)
                .addEdge(1, 2)
                .addEdge(2, 1)
                .addEdge(2, 3)
                .addEdge(3, 4)
                .addEdge(5, 3)
                .addEdge(5, 6)
                .addEdge(6, 3)
                .addEdge(6, 4)
                .build();
        Set<List<Integer>> actualPaths = FlowGraphUtil.getAllSimplePaths(graph, 2, 6);
        assertEquals(Collections.emptySet(), actualPaths);
    }

    @Test
    public void testGetAllSimplePathsSameSourceAndTarget() {
        FlowGraph<Integer> graph = new FlowGraphBuilder<Integer>()
                .addEntryPoint(0)
                .addExitPoint(4)
                .addEdge(0, 1)
                .addEdge(0, 3)
                .addEdge(0, 5)
                .addEdge(1, 2)
                .addEdge(2, 1)
                .addEdge(2, 3)
                .addEdge(3, 4)
                .addEdge(5, 3)
                .addEdge(5, 6)
                .addEdge(6, 3)
                .addEdge(6, 4)
                .build();
        Set<List<Integer>> actualPaths = FlowGraphUtil.getAllSimplePaths(graph, 5, 5);
        Set<List<Integer>> expectedPaths = new HashSet<>();
        expectedPaths.add(Arrays.asList(5));
        assertEquals(expectedPaths, actualPaths);
    }

    @Test(expected = NullPointerException.class)
    public void testGetAllSimplePathsNullGraph() {
        FlowGraphUtil.getAllSimplePaths(null, 5, 5);
    }

    @Test
    public void testGetAllSimplePathsNullTarget() {
        FlowGraph<Integer> graph = new FlowGraphBuilder<Integer>()
                .addEntryPoint(0)
                .addExitPoint(null)
                .addEdge(0, null)
                .build();
        Set<List<Integer>> actualPaths = FlowGraphUtil.getAllSimplePaths(graph, 0, null);
        Set<List<Integer>> expectedPaths = new HashSet<>();
        expectedPaths.add(Arrays.asList(0, null));
        assertEquals(expectedPaths, actualPaths);
    }

    @Test
    public void testGetAllSimplePathsNullSource() {
        FlowGraph<Integer> graph = new FlowGraphBuilder<Integer>()
                .addEntryPoint(null)
                .addExitPoint(0)
                .addEdge(null, 0)
                .build();
        Set<List<Integer>> actualPaths = FlowGraphUtil.getAllSimplePaths(graph, null, 0);
        Set<List<Integer>> expectedPaths = new HashSet<>();
        expectedPaths.add(Arrays.asList(null, 0));
        assertEquals(expectedPaths, actualPaths);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAllSimplePathsSourceNotInGraph() {
        FlowGraph<Integer> graph = new FlowGraphBuilder<Integer>()
                .addEntryPoint(0)
                .addExitPoint(1)
                .addEdge(0, 1)
                .build();
        FlowGraphUtil.getAllSimplePaths(graph, 2, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAllSimplePathsTargetNotInGraph() {
        FlowGraph<Integer> graph = new FlowGraphBuilder<Integer>()
                .addEntryPoint(0)
                .addExitPoint(1)
                .addEdge(0, 1)
                .build();
        FlowGraphUtil.getAllSimplePaths(graph, 0, 2);
    }
}