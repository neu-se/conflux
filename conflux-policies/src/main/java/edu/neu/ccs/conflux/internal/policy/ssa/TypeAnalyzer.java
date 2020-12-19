package edu.neu.ccs.conflux.internal.policy.ssa;

import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.control.type.TypeValue;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Type;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.analysis.Frame;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.*;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.AssignmentStatement;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.IfStatement;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.Statement;

import java.util.Iterator;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.*;

public class TypeAnalyzer {

    private static final Type OBJECT_TYPE = Type.getType(Object.class);
    private static final Type THROWABLE_TYPE = Type.getType(Throwable.class);

    private final List<Type> parameterTypes;
    private final PropagatingVisitor propagatingVisitor;
    private final Map<AbstractInsnNode, AnnotatedInstruction> insnMap = new HashMap<>();
    private final Map<AssignmentStatement, Frame<TypeValue>> phiFrameMap;
    private final Map<VariableExpression, Type> typeMap = new HashMap<>();
    private final boolean considerSingleBitChecks;

    public TypeAnalyzer(SSAMethod ssaMethod, boolean considerSingleBitChecks) {
        this.considerSingleBitChecks = considerSingleBitChecks;
        parameterTypes = ssaMethod.getParameterTypes();
        propagatingVisitor = new PropagatingVisitor(ssaMethod.getControlFlowGraph(), true);
        phiFrameMap = calculatePhiFrameMap(ssaMethod);
        for(AnnotatedBasicBlock block : ssaMethod.getControlFlowGraph().getVertices()) {
            for(AnnotatedInstruction insn : block.getInstructions()) {
                insnMap.put(insn.getInstruction(), insn);
            }
        }
        calculateTypes(calculateDefinitions(ssaMethod.getControlFlowGraph()));
    }

    private void calculateTypes(Map<AssignmentStatement, AbstractInsnNode> definitions) {
        boolean changed;
        do {
            changed = false;
            Iterator<AssignmentStatement> itr = definitions.keySet().iterator();
            while(itr.hasNext()) {
                AssignmentStatement statement = itr.next();
                Type type;
                if(statement.getRightHandSide() instanceof PhiFunction) {
                    type = calculateType((PhiFunction) statement.getRightHandSide(), statement);
                } else {
                    type = calculateType(statement.getRightHandSide(), definitions.get(statement));
                }
                if(type != null) {
                    changed = true;
                    typeMap.put((VariableExpression) statement.getLeftHandSide(), type);
                    itr.remove();
                }
            }
        } while(changed);
        for(AssignmentStatement statement : definitions.keySet()) {
            // Unknown variables
            typeMap.put((VariableExpression) statement.getLeftHandSide(), null);
        }
    }

    private Type calculateType(Expression expr, AbstractInsnNode insn) {
        if(expr instanceof ArrayAccess) {
            ArrayAccess aa = (ArrayAccess) expr;
            switch(insn.getOpcode()) {
                case LALOAD:
                    return Type.LONG_TYPE;
                case FALOAD:
                    return Type.FLOAT_TYPE;
                case DALOAD:
                    return Type.DOUBLE_TYPE;
                case IALOAD:
                    return Type.INT_TYPE;
                case CALOAD:
                    return Type.CHAR_TYPE;
                case SALOAD:
                    return Type.SHORT_TYPE;
                case BALOAD:
                case AALOAD:
                    Expression arrayRef = aa.getArrayRef();
                    if(typeMap.containsKey(arrayRef)) {
                        Type arrayType = typeMap.get(arrayRef);
                        if(arrayType.getSort() == Type.ARRAY) {
                            Type elementType = arrayType.getElementType();
                            int dimensions = arrayType.getDimensions() - 1;
                            return makeArrayType(elementType, dimensions);
                        }
                    }
                    return null;
            }
        } else if(expr instanceof BinaryExpression) {
            switch(insn.getOpcode()) {
                case IADD:
                case ISUB:
                case IMUL:
                case IDIV:
                case IREM:
                case ISHL:
                case ISHR:
                case IUSHR:
                case IINC:
                    Expression processed = expr.accept(propagatingVisitor, null);
                    if(processed instanceof IntegerConstantExpression) {
                        return narrowestIntType((IntegerConstantExpression) processed);
                    }
                    return Type.INT_TYPE;
                case IAND:
                case IOR:
                case IXOR:
                    processed = expr.accept(propagatingVisitor, null);
                    if(processed instanceof IntegerConstantExpression) {
                        return narrowestIntType((IntegerConstantExpression) processed);
                    }
                    Expression operand1 = ((BinaryExpression) expr).getOperand1();
                    Expression operand2 = ((BinaryExpression) expr).getOperand2();
                    if(typeMap.containsKey(operand1) && typeMap.containsKey(operand2)) {
                        Type type1 = typeMap.get(operand1);
                        Type type2 = typeMap.get(operand2);
                        if(type1.getSort() == Type.BOOLEAN && type2.getSort() == Type.BOOLEAN) {
                            return Type.BOOLEAN_TYPE;
                        }
                        return Type.INT_TYPE;
                    }
                    return null;
                case LADD:
                case LSUB:
                case LMUL:
                case LDIV:
                case LREM:
                case LSHL:
                case LSHR:
                case LUSHR:
                case LAND:
                case LOR:
                case LXOR:
                    return Type.LONG_TYPE;
                case FADD:
                case FSUB:
                case FMUL:
                case FDIV:
                case FREM:
                    return Type.FLOAT_TYPE;
                case DADD:
                case DSUB:
                case DMUL:
                case DDIV:
                case DREM:
                    return Type.DOUBLE_TYPE;
                case LCMP:
                case FCMPL:
                case DCMPL:
                case FCMPG:
                case DCMPG:
                    return Type.INT_TYPE;
            }
        } else if(expr instanceof CaughtExceptionExpression) {
            return THROWABLE_TYPE;
        } else if(expr instanceof FieldAccess) {
            return Type.getType(((FieldAccess) expr).getDesc());
        } else if(expr instanceof InvokeExpression) {
            return Type.getReturnType(((InvokeExpression) expr).getDesc());
        } else if(expr instanceof DoubleConstantExpression) {
            return Type.DOUBLE_TYPE;
        } else if(expr instanceof FloatConstantExpression) {
            return Type.FLOAT_TYPE;
        } else if(expr instanceof LongConstantExpression) {
            return Type.LONG_TYPE;
        } else if(expr instanceof ObjectConstantExpression) {
            ObjectConstantExpression oce = (ObjectConstantExpression) expr;
            Object constant = oce.getConstant();
            return constant == null ? OBJECT_TYPE : Type.getType(constant.getClass());
        } else if(expr instanceof IntegerConstantExpression) {
            return narrowestIntType((IntegerConstantExpression) expr);
        } else if(expr instanceof NewArrayExpression) {
            NewArrayExpression nae = (NewArrayExpression) expr;
            return makeArrayType(nae.getType(), nae.getDims().length);
        } else if(expr instanceof NewExpression) {
            return Type.getObjectType(((NewExpression) expr).getDesc());
        } else if(expr instanceof ParameterExpression) {
            ParameterExpression pe = (ParameterExpression) expr;
            return parameterTypes.get(pe.getParameterNumber());
        } else if(expr instanceof UnaryExpression) {
            switch(insn.getOpcode()) {
                case INEG:
                    Expression processed = expr.accept(propagatingVisitor, null);
                    if(processed instanceof IntegerConstantExpression) {
                        return narrowestIntType((IntegerConstantExpression) processed);
                    }
                    return Type.INT_TYPE;
                case LNEG:
                case I2L:
                case D2L:
                case F2L:
                    return Type.LONG_TYPE;
                case FNEG:
                case I2F:
                case D2F:
                case L2F:
                    return Type.FLOAT_TYPE;
                case DNEG:
                case I2D:
                case F2D:
                case L2D:
                    return Type.DOUBLE_TYPE;
                case ARRAYLENGTH:
                case D2I:
                case F2I:
                    return Type.INT_TYPE;
                case INSTANCEOF:
                    return Type.BOOLEAN_TYPE;
                case CHECKCAST:
                    UnaryExpression ue = (UnaryExpression) expr;
                    String desc = ((CastOperation) ue.getOperation()).getDesc();
                    return Type.getObjectType(desc);
                case I2B:
                    return Type.BYTE_TYPE;
                case I2C:
                    return Type.CHAR_TYPE;
                case I2S:
                    return Type.SHORT_TYPE;
            }
        } else if(expr instanceof VariableExpression) {
            if(typeMap.containsKey(expr)) {
                return typeMap.get(expr);
            }
        }
        return null;
    }

    private Type calculateType(PhiFunction pf, AssignmentStatement statement) {
        if(phiFrameMap.containsKey(statement)) {
            Frame<TypeValue> frame = phiFrameMap.get(statement);
            VariableExpression var = (VariableExpression) statement.getLeftHandSide();
            TypeValue tv;
            if(var instanceof StackElement) {
                tv = frame.getStack(((StackElement) var).getIndex());
            } else {
                tv = frame.getLocal(((LocalVariable) var).getIndex());
            }
            switch(tv.getType().getSort()) {
                case Type.DOUBLE:
                case Type.FLOAT:
                case Type.LONG:
                case Type.ARRAY:
                case Type.OBJECT:
                    return tv.getType();
            }
        }
        Set<Type> types = new HashSet<>();
        boolean allTypesKnown = true;
        boolean allTypesIntLike = true;
        boolean allTypeReferenceTypes = true;
        for(Expression value : pf.getValues()) {
            if(typeMap.containsKey(value)) {
                Type t = typeMap.get(value);
                types.add(t);
                switch(t.getSort()) {
                    case Type.DOUBLE:
                        return Type.DOUBLE_TYPE;
                    case Type.FLOAT:
                        return Type.FLOAT_TYPE;
                    case Type.LONG:
                        return Type.LONG_TYPE;
                    case Type.INT:
                        return Type.INT_TYPE;
                    case Type.ARRAY:
                    case Type.OBJECT:
                        allTypesIntLike = false;
                        break;
                    case Type.BOOLEAN:
                    case Type.BYTE:
                    case Type.CHAR:
                    case Type.SHORT:
                        allTypeReferenceTypes = false;
                        break;
                }
            } else {
                allTypesKnown = false;
            }
        }
        if(allTypesKnown) {
            if(types.size() == 1) {
                return types.iterator().next();
            }
            if(allTypeReferenceTypes) {
                return OBJECT_TYPE; // close enough
            } else if(allTypesIntLike) {
                boolean containsByte = false;
                boolean containsChar = false;
                boolean containsShort = false;
                for(Type t : types) {
                    switch(t.getSort()) {
                        case Type.BYTE:
                            containsByte = true;
                            break;
                        case Type.CHAR:
                            containsChar = true;
                            break;
                        case Type.SHORT:
                            containsShort = true;
                            break;
                    }
                }
                if(containsShort) {
                    return Type.SHORT_TYPE;
                } else if(containsChar) {
                    return Type.CHAR_TYPE;
                } else if(containsByte) {
                    return Type.BYTE_TYPE;
                } else {
                    return Type.BOOLEAN_TYPE;
                }
            }
        }
        return null;
    }

    private Type narrowestIntType(IntegerConstantExpression expr) {
        int constant = expr.getConstant();
        if(constant == 0 || constant == 1) {
            return Type.BOOLEAN_TYPE;
        } else if(Byte.MIN_VALUE <= constant && constant <= Byte.MAX_VALUE) {
            return Type.BYTE_TYPE;
        } else if(Character.MIN_VALUE <= constant && constant <= Character.MAX_VALUE) {
            return Type.CHAR_TYPE;
        } else {
            return Type.INT_TYPE;
        }
    }

    public boolean isDoubleBindingBranch(AbstractInsnNode insn) {
        if(!insnMap.containsKey(insn)) {
            return false;
        }
        AnnotatedInstruction ai = insnMap.get(insn);
        List<Statement> rawStatements = ai.getStatements();
        if(rawStatements.size() != 1 || !(rawStatements.get(0) instanceof IfStatement)) {
            return false;
        }
        Expression condition = ((IfStatement) rawStatements.get(0)).getExpression();
        if(condition instanceof BinaryExpression) {
            BinaryExpression be = (BinaryExpression) condition;
            switch(be.getOperation()) {
                case EQUAL:
                case NOT_EQUAL:
                    Type t1 = calculateType(be.getOperand1(), insn);
                    Type t2 = calculateType(be.getOperand2(), insn);
                    if((t1 == null || t1.getSort() == Type.BOOLEAN) && (t2 == null || t2.getSort() == Type.BOOLEAN)) {
                        return true;
                    }
            }
        }
        return considerSingleBitChecks && isSingleBitCheck(condition);
    }

    private static Map<AssignmentStatement, AbstractInsnNode> calculateDefinitions(FlowGraph<? extends AnnotatedBasicBlock> graph) {
        Map<AssignmentStatement, AbstractInsnNode> definitions = new HashMap<>();
        for(AnnotatedBasicBlock block : graph.getVertices()) {
            for(AnnotatedInstruction insn : block.getInstructions()) {
                for(Statement s : insn.getStatements()) {
                    if(s instanceof AssignmentStatement && s.definesVariable()) {
                        definitions.put((AssignmentStatement) s, insn.getInstruction());
                    }
                }
            }
        }
        return definitions;
    }

    private static Map<AssignmentStatement, Frame<TypeValue>> calculatePhiFrameMap(SSAMethod method) {
        Map<AssignmentStatement, Frame<TypeValue>> phiFrameMap = new HashMap<>();
        for(AnnotatedBasicBlock block : method.getControlFlowGraph().getVertices()) {
            for(AnnotatedInstruction insn : block.getInstructions()) {
                for(Statement s : insn.getStatements()) {
                    if(s instanceof AssignmentStatement) {
                        AssignmentStatement as = (AssignmentStatement) s;
                        if(as.getLeftHandSide() instanceof VariableExpression
                                && as.getRightHandSide() instanceof PhiFunction
                                && block.getFirstInsn().getOpcode() != NOP) {
                            phiFrameMap.put(as, method.getFrameMap().get(block.getFirstInsn()));
                        }
                    }
                }
            }
        }
        return phiFrameMap;
    }

    private static Type makeArrayType(Type elementType, int dimensions) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < dimensions; i++) {
            builder.append("[");
        }
        return Type.getType(builder.append(elementType).toString());
    }

    /**
     * Returns true if the specified expression is a conditional expression which test is a single bit in some int or
     * long is set to one.
     * <p>
     * Checks the specified expression is of one of the following forms:
     * <ul>
     *     <li> X == 0 </li>
     *     <li> X != 0 </li>
     *     <li> 0 == X </li>
     *     <li> 0 != X </li>
     * </ul>
     * where X is of some form Y or (Y LCMP 0) or (0 LCMP Y), and Y is of one of the following forms:
     * <ul>
     *     <li> Z & ? </li>
     *     <li> ? & Z </li>
     *     <li> (Z OP ?) & ? </li>
     *     <li> ? & (Z OP ?)</li>
     * </ul>
     * where Z is a constant with only one bit set to one, OP is a shift operator, and ? is any value.
     */
    private boolean isSingleBitCheck(Expression condition) {
        condition = condition.accept(propagatingVisitor, null);
        if(condition instanceof BinaryExpression) {
            BinaryExpression be = (BinaryExpression) condition;
            BinaryOperation operation = be.getOperation();
            Expression operand1 = be.getOperand1();
            Expression operand2 = be.getOperand2();
            if(operation != BinaryOperation.EQUAL && operation != BinaryOperation.NOT_EQUAL) {
                return false;
            }
            Expression x;
            if(isZeroConstant(operand1)) {
                x = operand2;
            } else if(isZeroConstant(operand2)) {
                x = operand1;
            } else {
                return false;
            }
            if(x instanceof BinaryExpression) {
                BinaryExpression bex = (BinaryExpression) x;
                if(bex.getOperation() == BinaryOperation.COMPARE) {
                    if(isZeroConstant(bex.getOperand1())) {
                        x = bex.getOperand2();
                    } else if(isZeroConstant(bex.getOperand2())) {
                        x = bex.getOperand1();
                    } else {
                        return false;
                    }
                }
            }
            if(x instanceof BinaryExpression) {
                BinaryExpression bey = (BinaryExpression) x;
                if(bey.getOperation() == BinaryOperation.BITWISE_AND) {
                    Expression y1 = bey.getOperand1();
                    Expression y2 = bey.getOperand2();
                    return isSingleBitMask(y1) || isSingleBitMask(y2);
                }
            }
        }
        return false;
    }

    private static boolean isZeroConstant(Expression e) {
        if(e instanceof IntegerConstantExpression) {
            return ((IntegerConstantExpression) e).getConstant() == 0;
        } else if(e instanceof LongConstantExpression) {
            return ((LongConstantExpression) e).getConstant() == 0;
        } else if(e instanceof DoubleConstantExpression) {
            return ((DoubleConstantExpression) e).getConstant() == 0;
        } else if(e instanceof FloatConstantExpression) {
            return ((FloatConstantExpression) e).getConstant() == 0;
        } else {
            return false;
        }
    }

    private static boolean isSingleBitMask(Expression e) {
        if(isSingleBitConstant(e)) {
            return true;
        }
        if(e instanceof BinaryExpression) {
            BinaryExpression be = (BinaryExpression) e;
            return (be.getOperation() == BinaryOperation.SHIFT_LEFT || be.getOperation() == BinaryOperation.SHIFT_RIGHT_UNSIGNED)
                    && isSingleBitConstant(be.getOperand1());
        }
        return false;
    }

    private static boolean isSingleBitConstant(Expression e) {
        if(e instanceof IntegerConstantExpression) {
            int c = ((IntegerConstantExpression) e).getConstant();
            return Integer.bitCount(c) == 1;
        } else if(e instanceof LongConstantExpression) {
            long c = ((LongConstantExpression) e).getConstant();
            return Long.bitCount(c) == 1;
        } else {
            return false;
        }
    }
}
