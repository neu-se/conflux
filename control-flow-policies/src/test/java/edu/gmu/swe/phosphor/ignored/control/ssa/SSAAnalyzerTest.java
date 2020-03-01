package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.gmu.swe.phosphor.ignored.control.ControlAnalysisTestUtil;
import org.junit.Test;

import static edu.gmu.swe.phosphor.ignored.control.ssa.SSAAnalyzerTestMethods.OWNER;

public class SSAAnalyzerTest {

    @Test
    public void testDup2X1() throws Exception {
        MethodNode methodNode = SSAAnalyzerTestMethods.getMethodNodeDup2X1();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
    }

    @Test
    public void testIndexOfBreak() throws Exception {
        MethodNode methodNode = ControlAnalysisTestUtil.getMethodNode(SSAAnalyzerTestMethods.class, "indexOfBreak");
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
    }
}