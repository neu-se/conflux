package edu.neu.ccs.conflux.internal.policy.ssa.converter;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Value;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.StackElement;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.AssignmentStatement;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.Statement;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public class DupSwapInsnConverter extends InsnConverter {

    DupSwapInsnConverter(InsnConverter zero) {
        super(zero);
    }

    @Override
    protected boolean canProcess(AbstractInsnNode insn) {
        switch(insn.getOpcode()) {
            case DUP:
            case DUP_X1:
            case DUP_X2:
            case DUP2:
            case DUP2_X1:
            case DUP2_X2:
            case SWAP:
                return true;
            default:
                return false;
        }
    }

    @Override
    protected Statement[] process(AbstractInsnNode insn, Frame<? extends Value> frame) {
        int opcode = insn.getOpcode();
        StackElement plusOne = new StackElement(frame.getStackSize() + 1);
        StackElement zero = new StackElement(frame.getStackSize());
        StackElement first = new StackElement(frame.getStackSize() - 1);
        StackElement second = new StackElement(frame.getStackSize() - 2);
        StackElement third = new StackElement(frame.getStackSize() - 3);
        StackElement fourth = new StackElement(frame.getStackSize() - 4);
        boolean wide1 = frame.getStack(frame.getStackSize() - 1).getSize() == 2;
        if(opcode == DUP || (opcode == DUP2 && wide1)) {
            // -1    -1  0
            // v1 -> v1 v1
            return new Statement[]{new AssignmentStatement(zero, first)};
        }
        boolean wide2 = frame.getStack(frame.getStackSize() - 2).getSize() == 2;
        if(opcode == SWAP) {
            // v2 v1 -> v1 v2
            return new Statement[]{
                    new AssignmentStatement(zero, first),
                    new AssignmentStatement(first, second),
                    new AssignmentStatement(second, zero),
            };
        } else if(opcode == DUP_X1) {
            // -2 -1    -2 -1  0
            // v2 v1 -> v1 v2 v1
            return new Statement[]{
                    new AssignmentStatement(zero, first),
                    new AssignmentStatement(first, second),
                    new AssignmentStatement(second, zero),
            };
        } else if(opcode == DUP_X2) {
            if(wide2) {
                // -2 -1    -2 -1  0
                // v2 v1 -> v1 v2 v1
                return new Statement[]{
                        new AssignmentStatement(zero, first),
                        new AssignmentStatement(first, second),
                        new AssignmentStatement(second, zero),
                };
            } else {
                // -3 -2 -1    -3 -2 -1  0
                // v3 v2 v1 -> v1 v3 v2 v1
                return new Statement[]{
                        new AssignmentStatement(zero, first),
                        new AssignmentStatement(first, second),
                        new AssignmentStatement(second, third),
                        new AssignmentStatement(third, zero),
                };
            }
        } else if(opcode == DUP2) {
            // -2 -1    -2 -1  0 +1
            // v2 v1 -> v2 v1 v2 v1
            return new Statement[]{
                    new AssignmentStatement(zero, second),
                    new AssignmentStatement(plusOne, first),
            };
        }
        if(opcode == DUP2_X1) {
            // value3, {value2, value1} -> {value2, value1}, value3, {value2, value1}
            if(wide1) {
                // -2 -1    -2 -1  0
                // v2 v1 -> v1 v2 v1
                return new Statement[]{
                        new AssignmentStatement(zero, first),
                        new AssignmentStatement(first, second),
                        new AssignmentStatement(second, zero),
                };
            } else {
                // -3 -2 -1    -3 -2 -1  0 +1
                // v3 v2 v1 -> v2 v1 v3 v2 v1
                return new Statement[]{
                        new AssignmentStatement(zero, second),
                        new AssignmentStatement(plusOne, first),
                        new AssignmentStatement(first, third),
                        new AssignmentStatement(second, plusOne),
                        new AssignmentStatement(third, zero),
                };
            }
        } else if(opcode == DUP2_X2) {
            // {value4, value3}, {value2, value1} -> {value2, value1}, {value4, value3}, {value2, value1}
            if(wide1) {
                if(wide2) {
                    // v2 v1 -> v1 v2 v1
                    return new Statement[]{
                            new AssignmentStatement(zero, first),
                            new AssignmentStatement(first, second),
                            new AssignmentStatement(second, zero),
                    };
                } else {
                    // v3 v2 v1 -> v1 v3 v2 v1
                    return new Statement[]{
                            new AssignmentStatement(zero, first),
                            new AssignmentStatement(first, second),
                            new AssignmentStatement(second, third),
                            new AssignmentStatement(third, zero)
                    };
                }
            } else {
                boolean wide3 = frame.getStack(frame.getStackSize() - 3).getSize() == 2;
                if(wide3) {
                    // v3 v2 v1 -> v2 v1 v3 v2 v1
                    return new Statement[]{
                            new AssignmentStatement(zero, second),
                            new AssignmentStatement(plusOne, first),
                            new AssignmentStatement(first, third),
                            new AssignmentStatement(second, plusOne),
                            new AssignmentStatement(third, zero)
                    };
                } else {
                    // -4 -3 -2 -1    -4 -3 -2 -1  0 +1
                    // v4 v3 v2 v1 -> v2 v1 v4 v3 v2 v1
                    return new Statement[]{
                            new AssignmentStatement(zero, second),
                            new AssignmentStatement(plusOne, first),
                            new AssignmentStatement(first, third),
                            new AssignmentStatement(second, fourth),
                            new AssignmentStatement(third, plusOne),
                            new AssignmentStatement(fourth, zero)
                    };
                }
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
}
