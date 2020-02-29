package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.control.graph.BaseControlFlowGraphCreator;
import edu.columbia.cs.psl.phosphor.control.graph.BasicBlock;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.PhosphorOpcodeIgnoringAnalyzer;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeInterpreter;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.InsnList;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.VarInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashMap;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.LinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.ArrayExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.ConstantExpression;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.LocalVariable;

import java.util.Iterator;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public class SSAAnalyzer {

    private final InsnList instructions;
    private final Frame<TypeValue>[] frames;
    private final Map<AbstractInsnNode, String> explicitExceptions = new HashMap<>();
    private final FlowGraph<BasicBlock> cfg;

    public SSAAnalyzer(String owner, MethodNode methodNode) throws AnalyzerException {
        instructions = methodNode.instructions;
        frames = new PhosphorOpcodeIgnoringAnalyzer<>(new TypeInterpreter(owner, methodNode)).analyze(owner, methodNode);
        calculateExplicitExceptions();
        cfg = new BaseControlFlowGraphCreator(true)
                .createControlFlowGraph(methodNode, explicitExceptions);
        Iterator<AbstractInsnNode> itr = instructions.iterator();
        int i = 0;
        List<Statement> statements = new LinkedList<>();
        while(itr.hasNext()) {
            statements.add(convert(frames[i++], itr.next()));
        }
    }

    private void calculateExplicitExceptions() {
        int i = 0;
        Iterator<AbstractInsnNode> itr = instructions.iterator();
        while(itr.hasNext()) {
            AbstractInsnNode insn = itr.next();
            if(insn.getOpcode() == ATHROW) {
                Frame<TypeValue> frame = frames[i];
                TypeValue top = frame.pop();
                Type type = top.getType();
                explicitExceptions.put(insn, type.getClassName().replace(".", "/"));
            }
            i++;
        }
    }

    public static Statement convert(Frame<TypeValue> frame, AbstractInsnNode insn) {
        int opcode = insn.getOpcode();
        switch(opcode) {
            case NOP:
                return EmptyStatement.NOP;
            case ACONST_NULL:
            case ICONST_M1:
            case ICONST_0:
            case ICONST_1:
            case ICONST_2:
            case ICONST_3:
            case ICONST_4:
            case ICONST_5:
            case LCONST_0:
            case LCONST_1:
            case FCONST_0:
            case FCONST_1:
            case FCONST_2:
            case DCONST_0:
            case DCONST_1:
            case BIPUSH:
            case SIPUSH:
            case LDC:
                return new AssignmentStatement(new StackElement(frame.getStackSize()), ConstantExpression.makeInstance(insn));
            case ILOAD:
            case LLOAD:
            case FLOAD:
            case DLOAD:
            case ALOAD:
                return new AssignmentStatement(new StackElement(frame.getStackSize()),
                        new LocalVariable(((VarInsnNode) insn).var));
            case IALOAD:
            case LALOAD:
            case FALOAD:
            case DALOAD:
            case AALOAD:
            case BALOAD:
            case CALOAD:
            case SALOAD:
                StackElement arrayRef = new StackElement(frame.getStackSize() - 2);
                StackElement index = new StackElement(frame.getStackSize() - 1);
                return new AssignmentStatement(new StackElement(frame.getStackSize() - 2),
                        new ArrayExpression(arrayRef, index));
            case ISTORE:
            case LSTORE:
            case FSTORE:
            case DSTORE:
            case ASTORE:
                return new AssignmentStatement(new LocalVariable(((VarInsnNode) insn).var),
                        new StackElement(frame.getStackSize() - 1));
            case IASTORE:
            case LASTORE:
            case FASTORE:
            case DASTORE:
            case AASTORE:
            case BASTORE:
            case CASTORE:
            case SASTORE:
                arrayRef = new StackElement(frame.getStackSize() - 3);
                index = new StackElement(frame.getStackSize() - 2);
                StackElement value = new StackElement(frame.getStackSize() - 1);
                return new AssignmentStatement(new ArrayExpression(arrayRef, index), value);
            case POP:
                return EmptyStatement.POP;
            case POP2:
                return EmptyStatement.POP2;
            case DUP:
                return new AssignmentStatement(new StackElement(frame.getStackSize()),
                        new StackElement(frame.getStackSize() - 1));
            case DUP_X1:
                // value2, value1 → value1, value2, value1
            case DUP_X2:
                // value3, value2, value1 → value1, value3, value2, value1
            case DUP2:
                // {value2, value1} → {value2, value1}, {value2, value1}
            case DUP2_X1:
                // value3, {value2, value1} → {value2, value1}, value3, {value2, value1}
            case DUP2_X2:
                // {value4, value3}, {value2, value1} → {value2, value1}, {value4, value3}, {value2, value1}
            case SWAP:
                //value2, value1 → value1, value2
            case IADD:
            case LADD:
            case FADD:
            case DADD:
            case ISUB:
            case LSUB:
            case FSUB:
            case DSUB:
            case IMUL:
            case LMUL:
            case FMUL:
            case DMUL:
            case IDIV:
            case LDIV:
            case FDIV:
            case DDIV:
            case IREM:
            case LREM:
            case FREM:
            case DREM:
            case INEG:
            case LNEG:
            case FNEG:
            case DNEG:
            case ISHL:
            case LSHL:
            case ISHR:
            case LSHR:
            case IUSHR:
            case LUSHR:
            case IAND:
            case LAND:
            case IOR:
            case LOR:
            case IXOR:
            case LXOR:
            case IINC:
            case I2L:
            case I2F:
            case I2D:
            case L2I:
            case L2F:
            case L2D:
            case F2I:
            case F2L:
            case F2D:
            case D2I:
            case D2L:
            case D2F:
            case I2B:
            case I2C:
            case I2S:
            case LCMP:
            case FCMPL:
            case FCMPG:
            case DCMPL:
            case DCMPG:
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
            case GOTO:
            case JSR:
            case RET:
            case TABLESWITCH:
            case LOOKUPSWITCH:
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
            case RETURN:
            case GETSTATIC:
            case PUTSTATIC:
            case GETFIELD:
            case PUTFIELD:
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
            case INVOKESTATIC:
            case INVOKEINTERFACE:
            case INVOKEDYNAMIC:
            case NEW:
            case NEWARRAY:
            case ANEWARRAY:
            case ARRAYLENGTH:
            case ATHROW:
            case CHECKCAST:
            case INSTANCEOF:
            case MONITORENTER:
            case MONITOREXIT:
            case MULTIANEWARRAY:
            case IFNULL:
            case IFNONNULL:
            default:
                return EmptyStatement.UNIMPLEMENTED;
        }
    }
}
