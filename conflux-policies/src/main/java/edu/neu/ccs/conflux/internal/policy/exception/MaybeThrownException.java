package edu.neu.ccs.conflux.internal.policy.exception;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.PhosphorInstructionInfo;
import edu.columbia.cs.psl.phosphor.instrumenter.LocalVariableManager;
import edu.columbia.cs.psl.phosphor.instrumenter.TaintMethodRecord;
import edu.columbia.cs.psl.phosphor.instrumenter.analyzer.NeverNullArgAnalyzerAdapter;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.MethodVisitor;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MultiANewArrayInsnNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public final class MaybeThrownException implements PhosphorInstructionInfo {

    private final ExceptionThrowingInstruction exceptionThrowingInstruction;
    private final int stackDepth;

    private MaybeThrownException(ExceptionThrowingInstruction exceptionThrowingInstruction, AbstractInsnNode instruction) {
        this.exceptionThrowingInstruction = exceptionThrowingInstruction;
        this.stackDepth = exceptionThrowingInstruction.stackDepth(instruction);
    }

    public void visit(MethodVisitor delegate, LocalVariableManager localVariableManager,
                      NeverNullArgAnalyzerAdapter analyzer) {
        if (analyzer.stack.size() >= stackDepth && exceptionThrowingInstruction.check(stackDepth, analyzer.stack)) {
            int[] indices = new int[stackDepth];
            Type[] types = new Type[indices.length];
            for (int i = 0; i < indices.length; i++) {
                types[i] = getTypeOfTop(analyzer);
                indices[i] = localVariableManager.getTmpLV();
                delegate.visitVarInsn(types[i].getOpcode(Opcodes.ISTORE), indices[i]);
            }
            exceptionThrowingInstruction.visit(delegate, indices, types, localVariableManager);
            for (int i = indices.length - 1; i >= 0; i--) {
                delegate.visitVarInsn(types[i].getOpcode(Opcodes.ILOAD), indices[i]);
                localVariableManager.freeTmpLV(indices[i]);
            }
        }
    }

    public static boolean mayThrowException(int opcode) {
        switch (opcode) {
            case AALOAD:
            case IALOAD:
            case LALOAD:
            case FALOAD:
            case DALOAD:
            case BALOAD:
            case CALOAD:
            case SALOAD:
            case AASTORE:
            case IASTORE:
            case LASTORE:
            case FASTORE:
            case DASTORE:
            case BASTORE:
            case CASTORE:
            case SASTORE:
            case IDIV:
            case LDIV:
            case IREM:
            case LREM:
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
            case RETURN:
            case GETFIELD:
            case PUTFIELD:
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
            case INVOKEINTERFACE:
            case NEWARRAY:
            case ANEWARRAY:
            case ARRAYLENGTH:
            case ATHROW:
            case CHECKCAST:
            case MONITORENTER:
            case MONITOREXIT:
            case MULTIANEWARRAY:
                return true;
            default:
                return false;
        }
    }

    public static MaybeThrownException getInstance(AbstractInsnNode instruction) {
        switch (instruction.getOpcode()) {
            case AALOAD:
            case IALOAD:
            case LALOAD:
            case FALOAD:
            case DALOAD:
            case BALOAD:
            case CALOAD:
            case SALOAD:
                return new MaybeThrownException(ExceptionThrowingInstruction.XALOAD, instruction);
            case AASTORE:
                return new MaybeThrownException(ExceptionThrowingInstruction.AASTORE, instruction);
            case IASTORE:
            case LASTORE:
            case FASTORE:
            case DASTORE:
            case BASTORE:
            case CASTORE:
            case SASTORE:
                return new MaybeThrownException(ExceptionThrowingInstruction.XASTORE, instruction);
            case IDIV:
            case LDIV:
            case IREM:
            case LREM:
                return new MaybeThrownException(ExceptionThrowingInstruction.IDIV_IREM_LDIV_LREM, instruction);
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
                return new MaybeThrownException(ExceptionThrowingInstruction.XRETURN, instruction);
            case RETURN:
                return new MaybeThrownException(ExceptionThrowingInstruction.RETURN, instruction);
            case GETFIELD:
                return new MaybeThrownException(ExceptionThrowingInstruction.GETFIELD, instruction);
            case PUTFIELD:
                return new MaybeThrownException(ExceptionThrowingInstruction.PUTFIELD, instruction);
            case INVOKEVIRTUAL:
                return new MaybeThrownException(ExceptionThrowingInstruction.INVOKEVIRTUAL,
                        instruction);
            case INVOKESPECIAL:
            case INVOKEINTERFACE:
                return new MaybeThrownException(ExceptionThrowingInstruction.INVOKESPECIAL_INVOKEINTERFACE,
                        instruction);
            case NEWARRAY:
            case ANEWARRAY:
                return new MaybeThrownException(ExceptionThrowingInstruction.NEWARRAY_ANEWARRAY, instruction);
            case ARRAYLENGTH:
                return new MaybeThrownException(ExceptionThrowingInstruction.ARRAYLENGTH, instruction);
            case ATHROW:
                return new MaybeThrownException(ExceptionThrowingInstruction.ATHROW, instruction);
            case CHECKCAST:
                return new MaybeThrownException(ExceptionThrowingInstruction.CHECKCAST, instruction);
            case MONITORENTER:
                return new MaybeThrownException(ExceptionThrowingInstruction.MONITORENTER, instruction);
            case MONITOREXIT:
                return new MaybeThrownException(ExceptionThrowingInstruction.MONITOREXIT, instruction);
            case MULTIANEWARRAY:
                return new MaybeThrownException(ExceptionThrowingInstruction.MULTIANEWARRAY,
                        instruction);
            default:
                throw new IllegalArgumentException();

        }
    }

    private static Type getTypeOfTop(NeverNullArgAnalyzerAdapter analyzer) {
        Object obj = analyzer.stack.get(analyzer.stack.size() - 1);
        if (obj instanceof String) {
            return Type.getObjectType((String) obj);
        } else if (obj == Opcodes.INTEGER) {
            return Type.INT_TYPE;
        } else if (obj == Opcodes.FLOAT) {
            return Type.FLOAT_TYPE;
        } else if (obj == Opcodes.DOUBLE) {
            return Type.DOUBLE_TYPE;
        } else if (obj == Opcodes.LONG) {
            return Type.LONG_TYPE;
        } else if (obj == Opcodes.TOP) {
            obj = analyzer.stack.get(analyzer.stack.size() - 2);
            if (obj == Opcodes.DOUBLE) {
                return Type.DOUBLE_TYPE;
            } else if (obj == Opcodes.LONG) {
                return Type.LONG_TYPE;
            }
        }
        return Type.getType("Ljava/lang/Object;");
    }

    private static boolean isTag(List<Object> stack, int fromTop) {
        int index = stack.size() - fromTop - 1;
        if (index < 0) {
            return false;
        }
        Object obj = stack.get(index);
        return obj.equals(Configuration.TAINT_TAG_STACK_TYPE);
    }

    private enum ExceptionThrowingInstruction {
        XALOAD() {
            @Override
            public void visit(MethodVisitor delegate, int[] indices, Type[] types,
                              LocalVariableManager localVariableManager) {
                // NullPointerException -> {arrayref}
                // ArrayIndexOutOfBoundsException -> {index, arrayref}
                // [arrayref, arrayref-taint, index, index-taint]
                delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
                delegate.visitVarInsn(types[0].getOpcode(Opcodes.ILOAD), indices[0]);
                delegate.visitVarInsn(types[2].getOpcode(Opcodes.ILOAD), indices[2]);
                TaintMethodRecord.COMBINE_TAGS.delegateVisit(delegate);
                ExceptionalMethodRecord.SET_INSTRUCTION_EXCEPTION_TAG.delegateVisit(delegate);
            }

            @Override
            int stackDepth(AbstractInsnNode instruction) {
                return 3;
            }

            @Override
            boolean check(int stackDepth, List<Object> stack) {
                return isTag(stack, 0) && isTag(stack, 2);
            }
        },
        AASTORE() {
            @Override
            public void visit(MethodVisitor delegate, int[] indices, Type[] types,
                              LocalVariableManager localVariableManager) {
                // NullPointerException -> {arrayref}
                // ArrayIndexOutOfBoundsException -> {index, arrayref}
                // ArrayStoreException -> {value, arrayref}
                // [arrayref, arrayref-taint, index, index-taint, value, value-taint]
                delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
                delegate.visitVarInsn(types[0].getOpcode(Opcodes.ILOAD), indices[0]);
                delegate.visitVarInsn(types[2].getOpcode(Opcodes.ILOAD), indices[2]);
                delegate.visitVarInsn(types[4].getOpcode(Opcodes.ILOAD), indices[4]);
                TaintMethodRecord.COMBINE_TAGS.delegateVisit(delegate);
                TaintMethodRecord.COMBINE_TAGS.delegateVisit(delegate);
                ExceptionalMethodRecord.SET_INSTRUCTION_EXCEPTION_TAG.delegateVisit(delegate);
            }

            @Override
            int stackDepth(AbstractInsnNode instruction) {
                return 5;
            }

            @Override
            boolean check(int stackDepth, List<Object> stack) {
                return isTag(stack, 0) && isTag(stack, 2) && isTag(stack, 4);
            }
        },
        XASTORE() {
            @Override
            public void visit(MethodVisitor delegate, int[] indices, Type[] types,
                              LocalVariableManager localVariableManager) {
                // NullPointerException -> {arrayref}
                // ArrayIndexOutOfBoundsException -> {index, arrayref}
                // [arrayref, arrayref-taint, index, index-taint, value, value-taint]
                delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
                delegate.visitVarInsn(types[2].getOpcode(Opcodes.ILOAD), indices[2]);
                delegate.visitVarInsn(types[4].getOpcode(Opcodes.ILOAD), indices[4]);
                TaintMethodRecord.COMBINE_TAGS.delegateVisit(delegate);
                ExceptionalMethodRecord.SET_INSTRUCTION_EXCEPTION_TAG.delegateVisit(delegate);
            }

            @Override
            int stackDepth(AbstractInsnNode instruction) {
                return 5;
            }

            @Override
            boolean check(int stackDepth, List<Object> stack) {
                return isTag(stack, 2) && isTag(stack, 4);
            }
        },
        IDIV_IREM_LDIV_LREM() {
            @Override
            public void visit(MethodVisitor delegate, int[] indices, Type[] types,
                              LocalVariableManager localVariableManager) {
                // [value1, value1-taint, value2, value2-taint]
                // ArithmeticException -> {value2}
                delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
                delegate.visitVarInsn(types[0].getOpcode(Opcodes.ILOAD), indices[0]);
                ExceptionalMethodRecord.SET_INSTRUCTION_EXCEPTION_TAG.delegateVisit(delegate);
            }

            @Override
            int stackDepth(AbstractInsnNode instruction) {
                return 1;
            }

            @Override
            boolean check(int stackDepth, List<Object> stack) {
                return isTag(stack, 0);
            }
        },
        XRETURN() {
            @Override
            public void visit(MethodVisitor delegate, int[] indices, Type[] types,
                              LocalVariableManager localVariableManager) {
                // [value, value-taint]
                // IllegalMonitorStateException -> {}
                delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
                TaintMethodRecord.NEW_EMPTY_TAINT.delegateVisit(delegate);
                ExceptionalMethodRecord.SET_INSTRUCTION_EXCEPTION_TAG.delegateVisit(delegate);
            }

            @Override
            int stackDepth(AbstractInsnNode instruction) {
                return 0;
            }

            @Override
            boolean check(int stackDepth, List<Object> stack) {
                return true;
            }
        },
        RETURN() {
            @Override
            public void visit(MethodVisitor delegate, int[] indices, Type[] types,
                              LocalVariableManager localVariableManager) {
                // []
                // IllegalMonitorStateException -> {}
                delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
                TaintMethodRecord.NEW_EMPTY_TAINT.delegateVisit(delegate);
                ExceptionalMethodRecord.SET_INSTRUCTION_EXCEPTION_TAG.delegateVisit(delegate);
            }

            @Override
            int stackDepth(AbstractInsnNode instruction) {
                return 0;
            }

            @Override
            boolean check(int stackDepth, List<Object> stack) {
                return true;
            }
        },
        GETFIELD() {
            @Override
            public void visit(MethodVisitor delegate, int[] indices, Type[] types,
                              LocalVariableManager localVariableManager) {
                // [objectref, objectref-taint]
                // NullPointerException -> {objectref}
                delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
                delegate.visitVarInsn(types[0].getOpcode(Opcodes.ILOAD), indices[0]);
                ExceptionalMethodRecord.SET_INSTRUCTION_EXCEPTION_TAG.delegateVisit(delegate);
            }

            @Override
            int stackDepth(AbstractInsnNode instruction) {
                return 1;
            }

            @Override
            boolean check(int stackDepth, List<Object> stack) {
                return isTag(stack, 0);
            }
        },
        PUTFIELD() {
            @Override
            public void visit(MethodVisitor delegate, int[] indices, Type[] types,
                              LocalVariableManager localVariableManager) {
                // [objectref, objectref-taint, value, value-taint]
                // NullPointerException -> {objectref}
                delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
                delegate.visitVarInsn(types[2].getOpcode(Opcodes.ILOAD), indices[2]);
                ExceptionalMethodRecord.SET_INSTRUCTION_EXCEPTION_TAG.delegateVisit(delegate);
            }

            @Override
            int stackDepth(AbstractInsnNode instruction) {
                return 3;
            }

            @Override
            boolean check(int stackDepth, List<Object> stack) {
                return isTag(stack, 2);
            }
        },
        INVOKEVIRTUAL() {
            @Override
            public void visit(MethodVisitor delegate, int[] indices, Type[] types,
                              LocalVariableManager localVariableManager) {
                // [objectref, objectref-taint, args1, arg1-taint, arg2, arg2-taint, ..., arg_n, arg_n-taint]
                // NullPointerException -> {objectref}
                // WrongMethodTypeException -> {objectref}
                delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
                delegate.visitVarInsn(types[indices.length - 2].getOpcode(Opcodes.ILOAD), indices[indices.length - 2]);
                ExceptionalMethodRecord.SET_INSTRUCTION_EXCEPTION_TAG.delegateVisit(delegate);
            }

            @Override
            int stackDepth(AbstractInsnNode instruction) {
                return 2 * (Type.getArgumentTypes(((MethodInsnNode) instruction).desc).length + 1);
            }

            @Override
            boolean check(int stackDepth, List<Object> stack) {
                return isTag(stack, stackDepth - 2);
            }
        },
        INVOKESPECIAL_INVOKEINTERFACE() {
            @Override
            public void visit(MethodVisitor delegate, int[] indices, Type[] types,
                              LocalVariableManager localVariableManager) {
                // [objectref, objectref-taint, args1, arg1-taint, arg2, arg2-taint, ..., arg_n, arg_n-taint]
                // NullPointerException -> {objectref}
                delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
                delegate.visitVarInsn(types[indices.length - 2].getOpcode(Opcodes.ILOAD), indices[indices.length - 2]);
                ExceptionalMethodRecord.SET_INSTRUCTION_EXCEPTION_TAG.delegateVisit(delegate);
            }

            @Override
            int stackDepth(AbstractInsnNode instruction) {
                return 2 * (Type.getArgumentTypes(((MethodInsnNode) instruction).desc).length + 1);
            }

            @Override
            boolean check(int stackDepth, List<Object> stack) {
                return isTag(stack, stackDepth - 2);
            }
        },
        NEWARRAY_ANEWARRAY() {
            @Override
            public void visit(MethodVisitor delegate, int[] indices, Type[] types,
                              LocalVariableManager localVariableManager) {
                // [count, count-taint]
                // NegativeArraySizeException -> {count}
                delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
                delegate.visitVarInsn(types[0].getOpcode(Opcodes.ILOAD), indices[0]);
                ExceptionalMethodRecord.SET_INSTRUCTION_EXCEPTION_TAG.delegateVisit(delegate);
            }

            @Override
            int stackDepth(AbstractInsnNode instruction) {
                return 1;
            }

            @Override
            boolean check(int stackDepth, List<Object> stack) {
                return isTag(stack, 0);
            }
        },
        ARRAYLENGTH() {
            @Override
            public void visit(MethodVisitor delegate, int[] indices, Type[] types,
                              LocalVariableManager localVariableManager) {
                // [arrayref, count-taint]
                //  NullPointerException -> {arrayref}
                delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
                delegate.visitVarInsn(types[0].getOpcode(Opcodes.ILOAD), indices[0]);
                ExceptionalMethodRecord.SET_INSTRUCTION_EXCEPTION_TAG.delegateVisit(delegate);
            }

            @Override
            int stackDepth(AbstractInsnNode instruction) {
                return 1;
            }

            @Override
            boolean check(int stackDepth, List<Object> stack) {
                return isTag(stack, 0);
            }
        },
        ATHROW() {
            @Override
            public void visit(MethodVisitor delegate, int[] indices, Type[] types,
                              LocalVariableManager localVariableManager) {
                // [objectref, objectref-taint]
                // NullPointerException -> {objectref}
                // IllegalMonitorStateException -> {}
                // Throwable -> {objectref}
                delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
                delegate.visitVarInsn(types[0].getOpcode(Opcodes.ILOAD), indices[0]);
                ExceptionalMethodRecord.SET_INSTRUCTION_EXCEPTION_TAG.delegateVisit(delegate);
            }

            @Override
            int stackDepth(AbstractInsnNode instruction) {
                return 1;
            }

            @Override
            boolean check(int stackDepth, List<Object> stack) {
                return isTag(stack, 0);
            }
        },
        CHECKCAST() {
            @Override
            public void visit(MethodVisitor delegate, int[] indices, Type[] types,
                              LocalVariableManager localVariableManager) {
                // [objectref, objectref-taint]
                // ClassCastException -> {objectref}
                delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
                delegate.visitVarInsn(types[0].getOpcode(Opcodes.ILOAD), indices[0]);
                ExceptionalMethodRecord.SET_INSTRUCTION_EXCEPTION_TAG.delegateVisit(delegate);
            }

            @Override
            int stackDepth(AbstractInsnNode instruction) {
                return 1;
            }

            @Override
            boolean check(int stackDepth, List<Object> stack) {
                return isTag(stack, 0);
            }
        },
        MONITORENTER() {
            @Override
            public void visit(MethodVisitor delegate, int[] indices, Type[] types,
                              LocalVariableManager localVariableManager) {
                // [objectref, objectref-taint]
                // NullPointerException -> {objectref}
                delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
                delegate.visitVarInsn(types[0].getOpcode(Opcodes.ILOAD), indices[0]);
                ExceptionalMethodRecord.SET_INSTRUCTION_EXCEPTION_TAG.delegateVisit(delegate);
            }

            @Override
            int stackDepth(AbstractInsnNode instruction) {
                return 1;
            }

            @Override
            boolean check(int stackDepth, List<Object> stack) {
                return isTag(stack, 0);
            }
        },
        MONITOREXIT() {
            @Override
            public void visit(MethodVisitor delegate, int[] indices, Type[] types,
                              LocalVariableManager localVariableManager) {
                // [objectref, objectref-taint]
                // NullPointerException -> {objectref}
                // IllegalMonitorStateException -> {objectref}
                delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
                delegate.visitVarInsn(types[0].getOpcode(Opcodes.ILOAD), indices[0]);
                ExceptionalMethodRecord.SET_INSTRUCTION_EXCEPTION_TAG.delegateVisit(delegate);
            }

            @Override
            int stackDepth(AbstractInsnNode instruction) {
                return 1;
            }

            @Override
            boolean check(int stackDepth, List<Object> stack) {
                return isTag(stack, 0);
            }
        },
        MULTIANEWARRAY() {
            @Override
            public void visit(MethodVisitor delegate, int[] indices, Type[] types,
                              LocalVariableManager localVariableManager) {
                // [count1, count1-taint, count2, count2-taint, ..., count_n, count_n-taint]
                // NegativeArraySizeException  -> {count1, counts...}
                delegate.visitVarInsn(ALOAD, localVariableManager.getIndexOfMasterControlLV());
                TaintMethodRecord.NEW_EMPTY_TAINT.delegateVisit(delegate);
                for (int i = 0; i < indices.length; i += 2) {
                    delegate.visitVarInsn(types[i].getOpcode(Opcodes.ILOAD), indices[i]);
                    TaintMethodRecord.COMBINE_TAGS.delegateVisit(delegate);
                }
                ExceptionalMethodRecord.SET_INSTRUCTION_EXCEPTION_TAG.delegateVisit(delegate);
            }

            @Override
            int stackDepth(AbstractInsnNode instruction) {
                return ((MultiANewArrayInsnNode) instruction).dims * 2;
            }

            @Override
            boolean check(int stackDepth, List<Object> stack) {
                for (int i = 0; i < stackDepth; i += 2) {
                    if (!isTag(stack, i)) {
                        return false;
                    }
                }
                return true;
            }
        };

        abstract void visit(MethodVisitor delegate, int[] indices, Type[] types,
                            LocalVariableManager localVariableManager);

        abstract int stackDepth(AbstractInsnNode instruction);

        abstract boolean check(int stackDepth, List<Object> stack);
    }
}
