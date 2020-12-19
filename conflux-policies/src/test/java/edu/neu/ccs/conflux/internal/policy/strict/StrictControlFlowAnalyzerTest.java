package edu.neu.ccs.conflux.internal.policy.strict;

import edu.columbia.cs.psl.phosphor.control.standard.BranchStart;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.*;
import org.junit.Test;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.GOTO;
import static org.junit.Assert.*;

public class StrictControlFlowAnalyzerTest {
    @Test
    public void testSwitchMultipleBranchesSameTarget() {
        MethodNode mn = StrictControlFlowAnalyzerTestMethods.switchMultipleBranchesSameTarget();
        StrictControlFlowAnalyzer analyzer = new StrictControlFlowAnalyzer();
        analyzer.annotate(StrictControlFlowAnalyzerTestMethods.OWNER, mn);
        assertEquals(analyzer.getNumberOfUniqueBranchIDs(), 1);
        AbstractInsnNode[] instructions = mn.instructions.toArray();
        LookupSwitchInsnNode switchNode = null;
        for(AbstractInsnNode insn : instructions) {
            if(insn instanceof LookupSwitchInsnNode) {
                switchNode = (LookupSwitchInsnNode) insn;
                break;
            }
        }
        // Check that you could find the LookupSwitchInsnNode
        assertNotNull(switchNode);
        LabelNode strictEdgeLabel = switchNode.labels.get(0);
        boolean found = false;
        for(int i = 0; i < instructions.length; i++) {
            if(instructions[i] == strictEdgeLabel) {
                found = true;
                i++;
                // Check for the block added to push the tag
                assertTrue(instructions[i++] instanceof FrameNode);
                assertTrue(instructions[i] instanceof LdcInsnNode);
                Object cst = ((LdcInsnNode) instructions[i++]).cst;
                assertTrue(cst instanceof BranchStart);
                assertEquals(GOTO, instructions[i++].getOpcode());
            } else if(instructions[i] instanceof LdcInsnNode) {
                Object cst = ((LdcInsnNode) instructions[i]).cst;
                // Only one branch start should have been added
                assertFalse(cst instanceof BranchStart);
            }
        }
        assertTrue(found);
    }
}