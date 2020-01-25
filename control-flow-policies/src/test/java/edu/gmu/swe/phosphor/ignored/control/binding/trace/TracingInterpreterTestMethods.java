package edu.gmu.swe.phosphor.ignored.control.binding.trace;

import edu.columbia.cs.psl.phosphor.control.graph.*;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph.NaturalLoop;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.InsnList;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashMap;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import edu.gmu.swe.phosphor.ignored.control.binding.LoopLevel;

import java.util.Iterator;
import java.util.LinkedList;

@SuppressWarnings("unused")
class TracingInterpreterTestMethods {

    private int a = 1;
    private int[] a1 = new int[1];

    public void arrayFieldSelfComputation() {
        for(/* constant */ int i = 0; i < 5; i++) {
            a1[0] = a1[0] * 2; // this.a1 may have been redefined - variant +1
        }
    }

    public void fieldSelfComputation() {
        for(/* constant */ int i = 0; i < 5; i++) {
            a = a * 2; // dependent on arg0 (this)
        }
    }

    public void fieldAssignedVariantValue() {
        for(/* constant */ int i = 0; i < 5; i++) {
            a = i; // variant +1
        }
    }

    public void arrayFieldAssignedVariantValue() {
        for(/* constant */ int i = 0; i < 5; i++) {
            a1[0] = i; // variant +1
        }
    }

    public void variantArray(int[][] a) {
        for(/* constant */ int i = 0; i < 5; i++) {
            a[i][0] = 5; // variant +1
        }
    }

    public void variantArray2(LinkedList<int[]> in) {
        Iterator<int[]> itr = in.iterator(); // variant +0
        for(/* constant */ int i = 0; i < 5; i++) {
            int[] b = itr.next(); // variant +1
            b[0] = 5; // variant +1
        }
    }

    public static void allLocalAssignmentsConstant(boolean condition) {
        int a = 1;
        int b = a + 5;
        long c = b * a;
        c = c * c;
        int d;
        if(condition) {
            d = 9;
        } else {
            d = 9;
        }
        int e = d;
        int f = 6 * 7 + 88;
    }

    public static void allLocalAssignmentsConstant2(boolean condition) {
        int b = 0;
        int a = 77;
        if(condition) {
            b += 1;
        } else {
            b = 1;
        }
        if(condition) {
            a = a * b;
        }
    }

    public static void allLocalAssignmentsConstant3(boolean condition) {
        int c = 7; // constant
        int v = 2; // constant
        int w = 300; // constant
        int x = -4; // constant
        int y = 44; // constant
        int z = 5; // constant
        for(int i = 0; i < 55; i++) {
            for(int j = 0; j < 10; j++) {
                if(condition) {
                    v = (v * v + v + 5 * 77 - 99) / (2 * v); // constant: x = x OP C
                    w = c; // constant
                    x = c + x; // constant: x = x OP C
                    y = y / c; // constant: x = x OP C
                    z = -z; // constant: x = x OP C
                }
            }
        }
    }

    public static void argDependentAssignment(int a, int b, int c) {
        int d = b + 7; // dependent on arg1
        int e = -a; // dependent on arg0
        int f = 6 + c; // dependent on arg2
        int g = a + b; // dependent on arg0 and arg1
    }

    public static void argDependentBranching(boolean condition) {
        int a = -88; // constant
        int b;
        if(condition) {
            b = 77;  // constant
        } else {
            b = 144; // constant
        }
        b = b * a; // constant:  x = x OP C
        int c = b;  // variant 0: reaching definition of b varies
        int d = a + 77; // constant
        a = a + b; // variant 0: reaching definition of b varies
    }

    public static void localSelfComputation(int a) {
        for(/* constant */ int i = 0; i < 5; i++) {
            a = a * 2; // constant
        }
    }

    public static void arraySelfComputation(int[] a) {
        for(/* constant */ int i = 0; i < 5; i++) {
            a[0] = a[0] * 2; // dependent on arg0
        }
    }

    public static Map<AbstractInsnNode, LoopLevel> calculateLoopLevelMap(MethodNode methodNode) throws AnalyzerException {
        ControlFlowGraphCreator creator = new BaseControlFlowGraphCreator();
        FlowGraph<BasicBlock> controlFlowGraph = creator.createControlFlowGraph(methodNode);
        Map<AbstractInsnNode, Set<NaturalLoop<BasicBlock>>> containingLoopMap = getContainingLoops(methodNode.instructions, controlFlowGraph);
        TracingInterpreter interpreter = new TracingInterpreter(Type.getInternalName(TracingInterpreterTestMethods.class),
                methodNode, containingLoopMap, controlFlowGraph);
        return interpreter.getLoopLevelMap();
    }

    public static Map<AbstractInsnNode, Set<NaturalLoop<BasicBlock>>> getContainingLoops(InsnList instructions, FlowGraph<BasicBlock> controlFlowGraph) {
        Set<NaturalLoop<BasicBlock>> loops = controlFlowGraph.getNaturalLoops();
        Map<AbstractInsnNode, Set<NaturalLoop<BasicBlock>>> loopMap = new HashMap<>();
        Iterator<AbstractInsnNode> itr = instructions.iterator();
        while(itr.hasNext()) {
            loopMap.put(itr.next(), new HashSet<>());
        }
        for(NaturalLoop<BasicBlock> loop : loops) {
            for(BasicBlock basicBlock : loop.getVertices()) {
                if(basicBlock instanceof SimpleBasicBlock) {
                    AbstractInsnNode start = basicBlock.getFirstInsn();
                    while(start != null) {
                        loopMap.get(start).add(loop);
                        if(start == basicBlock.getLastInsn()) {
                            break;
                        }
                        start = start.getNext();
                    }
                }
            }
        }
        return loopMap;
    }

    public static void multiArraySelfComputation(int[][] a) {
        for(/* constant */ int i = 0; i < 5; i++) {
            a[0][0] = a[0][0] * 2; // a[0] may have been redefined - variant +1
        }
    }

    public static void localAssignedVariantValue(int a) {
        for(/* constant */ int i = 0; i < 5; i++) {
            a = i; // variant +1
        }
    }

    public static void arrayAssignedVariantValue(int[] a) {
        for(/* constant */ int i = 0; i < 5; i++) {
            a[0] = i; // variant +1
        }
    }

    public static void multiArrayAssignedVariantValue(int[][] a) {
        for(/* constant */ int i = 0; i < 5; i++) {
            a[0][0] = i; // variant +1
        }
    }

    public static void twoArrays(int[] a, int[] b) {
        for(/* constant */ int i = 0; i < 5; i++) {
            b[0]++; // dependent on arg1
            a[0] = b[0] + 27; // variant +1
        }
    }

    public static void arrayAliasing(int[] a, int[] b) {
        b = a; // dependent on arg0
        a[0] = b[0] + 1; // dependent on arg0
    }

    public static void arrayAliasingVariant(int[] a, int[] b, boolean condition) {
        if(condition) {
            b = a; // dependent on arg0
        }
        a[0] = b[0] + 1; // variant +0
    }

    public static void arrayElementRedefined(int[] a1) {
        int i = a1[0]; // variant +0
        a1[0] = 9; // dependent on arg0
        a1[0] = i + 6; // variant +0
    }

    public static void methodCallBetweenUses(int[] a1) {
        int i = a1[0]; // variant +0
        arrayElementRedefined(a1);
        a1[0] = i + 6; // variant +0
    }

    public static void indexOf() {
        int z = 0; // constant
        int[] a = new int[5]; // variant +0
        for(/* constant */ int i = 0; i < a.length; i++) {
            if(a[i] == 0) {
                z = i; // variant +1
            }
        }
    }

    public static void indexOfBreak() {
        int z = 0; // constant
        int[] a = new int[5]; // variant +0
        for(/*constant */ int i = 0; i < a.length; i++) {
            if(a[i] == 0) {
                z = i; // variant +1
                break;
            }
        }
    }
}
