package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.control.graph.BaseControlFlowGraphCreator;
import edu.columbia.cs.psl.phosphor.control.graph.BasicBlock;
import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.PhosphorOpcodeIgnoringAnalyzer;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeInterpreter;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.*;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashMap;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.LinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.*;

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
        List<Statement[]> statements = new LinkedList<>();
        while(itr.hasNext()) {
            statements.add(convert(frames[i++], itr.next()));
        }
        List<Statement> flat = flatten(statements);
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

    private static List<Statement> flatten(List<Statement[]> statements) {
        List<Statement> flattenedList = new LinkedList<>();
        for(Statement[] arr : statements) {
            for(Statement s : arr) {
                flattenedList.add(s);
            }
        }
        return flattenedList;
    }

    public static Statement[] convert(Frame<TypeValue> frame, AbstractInsnNode insn) {
        if(insn instanceof LabelNode) {
            return new Statement[]{new LabelStatement((LabelNode) insn)};
        } else if(insn instanceof LineNumberNode) {
            return new Statement[]{new LineNumberStatement((LineNumberNode) insn)};
        }
        switch(insn.getOpcode()) {
            case NOP:
                return new Statement[]{EmptyStatement.NOP};
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
                StackElement next = new StackElement(frame.getStackSize());
                Statement statement = new AssignmentStatement(next, ConstantExpression.makeInstance(insn));
                return new Statement[]{statement};
            case ILOAD:
            case LLOAD:
            case FLOAD:
            case DLOAD:
            case ALOAD:
                next = new StackElement(frame.getStackSize());
                statement = new AssignmentStatement(next, new LocalVariable(((VarInsnNode) insn).var));
                return new Statement[]{statement};
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
                statement = new AssignmentStatement(new StackElement(frame.getStackSize() - 2),
                        new ArrayExpression(arrayRef, index));
                return new Statement[]{statement};
            case ISTORE:
            case LSTORE:
            case FSTORE:
            case DSTORE:
            case ASTORE:
                StackElement top = new StackElement(frame.getStackSize() - 1);
                statement = new AssignmentStatement(new LocalVariable(((VarInsnNode) insn).var), top);
                return new Statement[]{statement};
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
                statement = new AssignmentStatement(new ArrayExpression(arrayRef, index), value);
                return new Statement[]{statement};
            case POP:
                return new Statement[]{EmptyStatement.POP};
            case POP2:
                return new Statement[]{EmptyStatement.POP2};
            case DUP:
                top = new StackElement(frame.getStackSize() - 1);
                statement = new AssignmentStatement(new StackElement(frame.getStackSize()), top);
                return new Statement[]{statement};
            case DUP_X1:
                // value2, value1 -> value1, value2, value1
                next = new StackElement(frame.getStackSize());
                StackElement first = new StackElement(frame.getStackSize() - 1);
                StackElement second = new StackElement(frame.getStackSize() - 2);
                return new Statement[]{
                        new AssignmentStatement(next, first),
                        new AssignmentStatement(first, second),
                        new AssignmentStatement(second, next),
                };
            case DUP_X2:
                // {value3, value2}, value1 -> value1, {value3, value2}, value1
                next = new StackElement(frame.getStackSize());
                first = new StackElement(frame.getStackSize() - 1);
                second = new StackElement(frame.getStackSize() - 2);
                if(frame.getStack(frame.getStackSize() - 2).getSize() == 2) {
                    return new Statement[]{
                            new AssignmentStatement(next, first),
                            new AssignmentStatement(first, second),
                            new AssignmentStatement(second, next),
                    };
                } else {
                    StackElement third = new StackElement(frame.getStackSize() - 3);
                    return new Statement[]{
                            new AssignmentStatement(next, first),
                            new AssignmentStatement(first, second),
                            new AssignmentStatement(second, third),
                            new AssignmentStatement(third, next),
                    };
                }
            case DUP2:
                // {value2, value1} -> {value2, value1}, {value2, value1}
                next = new StackElement(frame.getStackSize());
                first = new StackElement(frame.getStackSize() - 1);
                if(frame.getStack(frame.getStackSize() - 1).getSize() == 2) {
                    return new Statement[]{
                            new AssignmentStatement(next, first),
                    };
                } else {
                    StackElement next2 = new StackElement(frame.getStackSize() + 1);
                    second = new StackElement(frame.getStackSize() - 2);
                    return new Statement[]{
                            new AssignmentStatement(next, second),
                            new AssignmentStatement(next2, first),
                    };
                }
            case DUP2_X1:
                //    -2            -1              -2           -1         0
                // value3, {value2, value1} -> {value2, value1}, value3, {value2, value1}
                next = new StackElement(frame.getStackSize());
                first = new StackElement(frame.getStackSize() - 1);
                second = new StackElement(frame.getStackSize() - 2);
                if(frame.getStack(frame.getStackSize() - 1).getSize() == 2) {
                    return new Statement[]{
                            new AssignmentStatement(next, first),
                            new AssignmentStatement(first, second),
                            new AssignmentStatement(second, next),
                    };
                } else {
                    StackElement next2 = new StackElement(frame.getStackSize() + 1);
                    StackElement third = new StackElement(frame.getStackSize() - 3);
                    return new Statement[]{
                            new AssignmentStatement(next, second),
                            new AssignmentStatement(next2, first),
                            new AssignmentStatement(first, third),
                            new AssignmentStatement(second, next2),
                            new AssignmentStatement(third, next),
                    };
                }
            case DUP2_X2:
                // {value4, value3}, {value2, value1} -> {value2, value1}, {value4, value3}, {value2, value1}
                next = new StackElement(frame.getStackSize());
                first = new StackElement(frame.getStackSize() - 1);
                second = new StackElement(frame.getStackSize() - 2);
                if(frame.getStack(frame.getStackSize() - 1).getSize() == 2) {
                    if(frame.getStack(frame.getStackSize() - 2).getSize() == 2) {
                        // v2 v1 -> v1 v2 v1
                        return new Statement[]{
                                new AssignmentStatement(next, first),
                                new AssignmentStatement(first, second),
                                new AssignmentStatement(second, next),
                        };
                    } else {
                        // v3 v2 v1 -> v1 v3 v2 v1
                        StackElement third = new StackElement(frame.getStackSize() - 3);
                        return new Statement[]{
                                new AssignmentStatement(next, first),
                                new AssignmentStatement(first, second),
                                new AssignmentStatement(second, third),
                                new AssignmentStatement(third, next)
                        };
                    }
                } else {
                    StackElement third = new StackElement(frame.getStackSize() - 3);
                    StackElement next2 = new StackElement(frame.getStackSize() + 1);
                    if(frame.getStack(frame.getStackSize() - 2).getSize() == 2) {
                        // v3 v2 v1 -> v2 v1 v3 v2 v1
                        return new Statement[]{
                                new AssignmentStatement(next, second),
                                new AssignmentStatement(next2, first),
                                new AssignmentStatement(first, third),
                                new AssignmentStatement(second, next2),
                                new AssignmentStatement(third, next)
                        };
                    } else {
                        // v4 v3 v2 v1 -> v2 v1 v4 v3 v2 v1
                        // -4 -3 -2 -1    -4 -3 -2 -1 0  +1
                        StackElement fourth = new StackElement(frame.getStackSize() - 4);
                        return new Statement[]{
                                new AssignmentStatement(next, second),
                                new AssignmentStatement(next2, first),
                                new AssignmentStatement(first, third),
                                new AssignmentStatement(second, fourth),
                                new AssignmentStatement(third, next2),
                                new AssignmentStatement(fourth, next)
                        };
                    }
                }
            case SWAP:
                //value2, value1 -> value1, value2
                next = new StackElement(frame.getStackSize());
                first = new StackElement(frame.getStackSize() - 1);
                second = new StackElement(frame.getStackSize() - 2);
                return new Statement[]{
                        new AssignmentStatement(next, first),
                        new AssignmentStatement(first, second),
                        new AssignmentStatement(second, next),
                };
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
            case LCMP:
            case FCMPL:
            case FCMPG:
            case DCMPL:
            case DCMPG:
                BinaryOperation operation = BinaryOperation.getInstance(insn.getOpcode());
                first = new StackElement(frame.getStackSize() - 1);
                second = new StackElement(frame.getStackSize() - 2);
                statement = new AssignmentStatement(first, new BinaryExpression(operation, second, first));
                return new Statement[]{statement};
            case INEG:
            case LNEG:
            case FNEG:
            case DNEG:
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
            case CHECKCAST:
                first = new StackElement(frame.getStackSize() - 1);
                statement = new AssignmentStatement(first, new UnaryExpression(UnaryOperation.getInstance(insn), first));
                return new Statement[]{statement};
            case IINC:
                LocalVariable local = new LocalVariable(((IincInsnNode) insn).var);
                statement = new AssignmentStatement(local, new BinaryExpression(BinaryOperation.ADD, local,
                        new IntegerConstantExpression(((IincInsnNode) insn).incr)));
                return new Statement[]{statement};
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
                first = new StackElement(frame.getStackSize() - 1);
                return new Statement[]{new ReturnStatement(first)};
            case RETURN:
                return new Statement[]{new ReturnStatement(null)};
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
            case IFNULL:
            case IFNONNULL:
            case GOTO:
            case TABLESWITCH:
            case LOOKUPSWITCH:
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
            case INSTANCEOF:
            case MONITORENTER:
            case MONITOREXIT:
            case MULTIANEWARRAY:
            case JSR:
            case RET:
            default:
                return new Statement[]{EmptyStatement.UNIMPLEMENTED};
        }
    }
}
