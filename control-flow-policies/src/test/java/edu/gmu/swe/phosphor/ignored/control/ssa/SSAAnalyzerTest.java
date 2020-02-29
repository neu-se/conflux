package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.gmu.swe.phosphor.ignored.control.ControlAnalysisTestUtil;
import jdk.internal.org.objectweb.asm.Type;
import org.junit.Test;

import static org.junit.Assert.*;

public class SSAAnalyzerTest {
    private static final String owner = Type.getInternalName(SSAAnalyzerTestMethods.class);

    @Test
    public void testIndexOfBreak() throws Exception {
        MethodNode methodNode = ControlAnalysisTestUtil.getMethodNode(SSAAnalyzerTestMethods.class, "indexOfBreak");
        SSAAnalyzer analyzer = new SSAAnalyzer(owner, methodNode);
    }
}