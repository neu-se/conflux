package edu.gmu.swe.phosphor.ignored.control.binding;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.InsnList;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.LinkedHashMap;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.PropagationTransformer;
import edu.gmu.swe.phosphor.ignored.control.ssa.SSAMethod;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;
import edu.gmu.swe.phosphor.ignored.control.tac.ThreeAddressMethod;

import java.util.Iterator;

public class LoopConstancyCalculator {

    private final Map<AbstractInsnNode, StatementInfo> insnStatementMap;
    private final StatementInfo parameterInfo;

    public LoopConstancyCalculator(String owner, MethodNode methodNode) throws AnalyzerException {
        ThreeAddressMethod threeAddressMethod = new ThreeAddressMethod(owner, methodNode);
        SSAMethod ssaMethod = new SSAMethod(threeAddressMethod);
        PropagationTransformer transformer = new PropagationTransformer(ssaMethod.propagateVariables());
        InsnList instructions = threeAddressMethod.getOriginalMethod().instructions;
        insnStatementMap = new LinkedHashMap<>();
        Iterator<AbstractInsnNode> itr = instructions.iterator();
        while(itr.hasNext()) {
            AbstractInsnNode insn = itr.next();
            Statement[] threeAddressStatements = threeAddressMethod.getStatements(insn);
            Statement[] ssaStatements = ssaMethod.getStatements(insn);
            Statement[] propagatedStatements = new Statement[ssaStatements.length];
            for(int i = 0; i < ssaStatements.length; i++) {
                propagatedStatements[i] = ssaStatements[i].transform(transformer);
            }
            StatementInfo info = new StatementInfo(insn, threeAddressStatements, ssaStatements, propagatedStatements);
            insnStatementMap.put(insn, info);
        }
        Statement[] threeAddressStatements = threeAddressMethod.getParameterDefinitions().toArray(new Statement[0]);
        Statement[] ssaStatements = ssaMethod.getParameterDefinitions().toArray(new Statement[0]);
        Statement[] propagatedStatements = new Statement[ssaStatements.length];
        for(int i = 0; i < ssaStatements.length; i++) {
            propagatedStatements[i] = ssaStatements[i].transform(transformer);
        }
        parameterInfo = new StatementInfo(null, threeAddressStatements, ssaStatements, propagatedStatements);
    }

    private static class StatementInfo {
        private final AbstractInsnNode insn;
        private final Statement[] threeAddressStatements;
        private final Statement[] ssaStatements;
        private final Statement[] propagatedStatements;

        public StatementInfo(AbstractInsnNode insn, Statement[] threeAddressStatements, Statement[] ssaStatements,
                             Statement[] propagatedStatements) {
            this.insn = insn;
            this.threeAddressStatements = threeAddressStatements;
            this.ssaStatements = ssaStatements;
            this.propagatedStatements = propagatedStatements;
        }
    }
}
