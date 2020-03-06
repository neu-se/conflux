package edu.gmu.swe.phosphor.ignored.control.tac;

import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.PhosphorOpcodeIgnoringAnalyzer;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeInterpreter;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.converter.InsnConverter;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.LocalVariable;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.ParameterExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.StackElement;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VersionedExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.AssignmentStatement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;

import java.util.Iterator;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.ACC_STATIC;
import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.ATHROW;

public class ThreeAddressMethod {

    private static final InsnConverter converter = InsnConverter.getChain();

    private final String owner;
    private final MethodNode originalMethod;
    private Map<AbstractInsnNode, Statement[]> statementMap = new HashMap<>();
    private Map<AbstractInsnNode, Frame<TypeValue>> frameMap = new HashMap<>();
    private List<Statement> parameterDefinitions = new LinkedList<>();

    public ThreeAddressMethod(String owner, MethodNode originalMethod) throws AnalyzerException {
        this.owner = owner;
        this.originalMethod = originalMethod;
        TypeInterpreter interpreter = new TypeInterpreter(owner, originalMethod);
        Frame<TypeValue>[] frames = new PhosphorOpcodeIgnoringAnalyzer<>(interpreter).analyze(owner, originalMethod);
        Iterator<AbstractInsnNode> itr = originalMethod.instructions.iterator();
        for(int i = 0; itr.hasNext(); i++) {
            AbstractInsnNode insn = itr.next();
            statementMap.put(insn, createStatements(insn, frames[i]));
            frameMap.put(insn, frames[i]);
        }
        int i = 0;
        for(LocalVariable local : computeParameterLocals(originalMethod)) {
            AssignmentStatement definition = new AssignmentStatement(local, new ParameterExpression(i++));
            parameterDefinitions.add(definition);
        }
        statementMap = Collections.unmodifiableMap(statementMap);
        frameMap = Collections.unmodifiableMap(frameMap);
        parameterDefinitions = Collections.unmodifiableList(parameterDefinitions);
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
                TypeValue top = frame.getStack(frame.getStackSize() - 1);
                Type type = top.getType();
                explicitExceptions.put(insn, type.getClassName().replace(".", "/"));
            }
        }
        return explicitExceptions;
    }

    /**
     * @return a set containing the local variables and stack elements that are assigned a value in at least one
     * statement in this method
     */
    public Set<VersionedExpression> collectDefinedVariables() {
        Set<VersionedExpression> definedVariables = new HashSet<>();
        for(Statement[] statements : statementMap.values()) {
            for(Statement statement : statements) {
                if(statement.definesVariable()) {
                    definedVariables.add(statement.definedVariable());
                }
            }
        }
        for(Statement statement : parameterDefinitions) {
            if(statement.definesVariable()) {
                definedVariables.add(statement.definedVariable());
            }
        }
        return definedVariables;
    }

    public boolean isDefinedAtInstruction(AbstractInsnNode insn, VersionedExpression expr) {
        int index = expr instanceof LocalVariable ? ((LocalVariable) expr).getIndex() : ((StackElement) expr).getIndex();
        Frame<TypeValue> frame = frameMap.get(insn);
        if(frame != null) {
            if(expr instanceof LocalVariable) {
                return index < frame.getLocals()
                        && frame.getLocal(index) != TypeValue.UNINITIALIZED_VALUE;
            } else {
                return index < frame.getStackSize()
                        && frame.getStack(index) != TypeValue.UNINITIALIZED_VALUE;
            }
        }
        return false;
    }

    private static Statement[] createStatements(AbstractInsnNode instruction, Frame<TypeValue> frame) {
        if(frame == null) {
            return new Statement[0];
        } else {
            return converter.convert(instruction, frame);
        }
    }

    private static List<LocalVariable> computeParameterLocals(MethodNode method) {
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
}
