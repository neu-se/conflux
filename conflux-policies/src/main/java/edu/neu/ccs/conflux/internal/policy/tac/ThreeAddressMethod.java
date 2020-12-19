package edu.neu.ccs.conflux.internal.policy.tac;

import edu.columbia.cs.psl.phosphor.control.type.TypeInterpreter;
import edu.columbia.cs.psl.phosphor.control.type.TypeValue;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.PhosphorOpcodeIgnoringAnalyzer;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.TryCatchBlockNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.neu.ccs.conflux.internal.policy.ssa.converter.InsnConverter;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.CaughtExceptionExpression;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.LocalVariable;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.ParameterExpression;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.VariableExpression;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.AssignmentStatement;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.Statement;

import java.util.Iterator;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.ACC_STATIC;
import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.ATHROW;

public class ThreeAddressMethod {

    private static final InsnConverter converter = InsnConverter.getChain();

    private final String owner;
    private final MethodNode originalMethod;
    private Map<AbstractInsnNode, Statement[]> statementMap = new LinkedHashMap<>();
    private Map<AbstractInsnNode, Frame<TypeValue>> frameMap = new HashMap<>();
    private List<Statement> parameterDefinitions = new LinkedList<>();
    private Map<AbstractInsnNode, CaughtExceptionExpression> exceptionHandlerStarts = new HashMap<>();

    public ThreeAddressMethod(String owner, MethodNode originalMethod) throws AnalyzerException {
        this.owner = owner;
        this.originalMethod = originalMethod;
        TypeInterpreter interpreter = new TypeInterpreter(owner, originalMethod);
        Frame<TypeValue>[] frames = new PhosphorOpcodeIgnoringAnalyzer<>(interpreter).analyze(owner, originalMethod);
        Iterator<AbstractInsnNode> itr = originalMethod.instructions.iterator();
        for(int i = 0; itr.hasNext(); i++) {
            AbstractInsnNode insn = itr.next();
            statementMap.put(insn, converter.convert(insn, frames[i]));
            frameMap.put(insn, frames[i]);
        }
        int i = 0;
        for(LocalVariable local : computeParameterDefinitions(originalMethod)) {
            AssignmentStatement definition = new AssignmentStatement(local, new ParameterExpression(i++));
            parameterDefinitions.add(definition);
        }
        statementMap = Collections.unmodifiableMap(statementMap);
        frameMap = Collections.unmodifiableMap(frameMap);
        parameterDefinitions = Collections.unmodifiableList(parameterDefinitions);
        int nextCaughtExceptionId = 0;
        for(TryCatchBlockNode tryCatch : originalMethod.tryCatchBlocks) {
            exceptionHandlerStarts.put(tryCatch.handler, new CaughtExceptionExpression(nextCaughtExceptionId++));
        }
        exceptionHandlerStarts = Collections.unmodifiableMap(exceptionHandlerStarts);
    }

    public String getOwner() {
        return owner;
    }

    public MethodNode getOriginalMethod() {
        return originalMethod;
    }

    public Map<AbstractInsnNode, Statement[]> getStatementMap() {
        return statementMap;
    }

    public Statement[] getStatements(AbstractInsnNode insn) {
        return statementMap.get(insn);
    }

    public Map<AbstractInsnNode, Frame<TypeValue>> getFrameMap() {
        return frameMap;
    }

    public List<Statement> getParameterDefinitions() {
        return parameterDefinitions;
    }

    public Map<AbstractInsnNode, String> calculateExplicitExceptions() {
        Map<AbstractInsnNode, String> explicitExceptions = new HashMap<>();
        for(AbstractInsnNode insn : statementMap.keySet()) {
            Frame<TypeValue> frame = frameMap.get(insn);
            if(insn.getOpcode() == ATHROW && frame != null) {
                Type type = frame.getStack(frame.getStackSize() - 1).getType();
                if(type != null) {
                    explicitExceptions.put(insn, type.getInternalName());
                }
            }
        }
        return explicitExceptions;
    }

    public Map<AbstractInsnNode, CaughtExceptionExpression> getExceptionHandlerStarts() {
        return exceptionHandlerStarts;
    }

    /**
     * @return a set containing the local variables and stack elements that are assigned a value in at least one
     * statement in this method
     */
    public Set<VariableExpression> collectDefinedVariables() {
        Set<VariableExpression> definedVariables = new HashSet<>();
        for(Statement[] statements : statementMap.values()) {
            for(Statement statement : statements) {
                if(statement.definesVariable()) {
                    definedVariables.add(statement.getDefinedVariable());
                }
            }
        }
        for(Statement statement : parameterDefinitions) {
            if(statement.definesVariable()) {
                definedVariables.add(statement.getDefinedVariable());
            }
        }
        return definedVariables;
    }

    private static List<LocalVariable> computeParameterDefinitions(MethodNode method) {
        List<LocalVariable> params = new LinkedList<>();
        int currentLocal = 0;
        boolean isInstanceMethod = (method.access & ACC_STATIC) == 0;
        if(isInstanceMethod) {
            params.add(new LocalVariable(currentLocal));
            currentLocal++;
        }
        Type[] argumentTypes = Type.getArgumentTypes(method.desc);
        for(Type argumentType : argumentTypes) {
            params.add(new LocalVariable(currentLocal));
            currentLocal += argumentType.getSize();
        }
        return params;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(Statement s : parameterDefinitions) {
            builder.append(s.toString()).append("\n");
        }
        for(Statement[] statements : statementMap.values()) {
            for(Statement s : statements) {
                builder.append(s.toString()).append("\n");
            }
        }
        return builder.toString();
    }
}
