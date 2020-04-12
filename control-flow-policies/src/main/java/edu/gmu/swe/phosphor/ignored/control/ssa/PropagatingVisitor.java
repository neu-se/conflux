package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashMap;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.*;

public class PropagatingVisitor implements StatefulExpressionVisitor<Expression, VariableExpression>, StatementVisitor<Statement> {

    private final Map<VariableExpression, Expression> currentDefinitions = new HashMap<>();

    public PropagatingVisitor(FlowGraph<? extends AnnotatedBasicBlock> graph) {
        for(AnnotatedBasicBlock block : graph.getVertices()) {
            for(AnnotatedInstruction insn : block.getInstructions()) {
                for(Statement statement : insn.getStatements()) {
                    if(statement.definesVariable()) {
                        Expression rhs = ((AssignmentStatement) statement).getRightHandSide();
                        if(canPropagate(rhs)) {
                            currentDefinitions.put(statement.getDefinedVariable(), rhs);
                        }
                    }
                }
            }
        }
        boolean changed;
        do {
            changed = false;
            for(VariableExpression lhs : currentDefinitions.keySet()) {
                Expression rhs = currentDefinitions.get(lhs);
                Expression transformed = rhs.accept(this, lhs);
                if(!rhs.equals(transformed)) {
                    changed = true;
                    currentDefinitions.put(lhs, transformed);
                }
            }
        } while(changed);
    }

    @Override
    public Expression visit(ArrayAccess expression, VariableExpression lhs) {
        Expression arrayRef = expression.getArrayRef().accept(this, lhs);
        Expression index = expression.getIndex().accept(this, lhs);
        return new ArrayAccess(arrayRef, index);
    }

    @Override
    public Expression visit(BinaryExpression expression, VariableExpression lhs) {
        Expression operand1 = expression.getOperand1().accept(this, lhs);
        Expression operand2 = expression.getOperand2().accept(this, lhs);
        BinaryOperation operation = expression.getOperation();
        if(operation.canPerform(operand1, operand2)) {
            return operation.perform(operand1, operand2);
        } else {
            return new BinaryExpression(operation, operand1, operand2);
        }
    }

    @Override
    public Expression visit(CaughtExceptionExpression expression, VariableExpression lhs) {
        return expression;
    }

    @Override
    public Expression visit(DoubleConstantExpression expression, VariableExpression lhs) {
        return expression;
    }

    @Override
    public Expression visit(FloatConstantExpression expression, VariableExpression lhs) {
        return expression;
    }

    @Override
    public Expression visit(IntegerConstantExpression expression, VariableExpression lhs) {
        return expression;
    }

    @Override
    public Expression visit(LongConstantExpression expression, VariableExpression lhs) {
        return expression;
    }

    @Override
    public Expression visit(ObjectConstantExpression expression, VariableExpression lhs) {
        return expression;
    }

    @Override
    public Expression visit(FieldAccess expression, VariableExpression lhs) {
        Expression receiver = expression.getReceiver();
        if(receiver == null) {
            return expression;
        } else {
            return new FieldAccess(expression.getOwner(), expression.getName(), expression.getDesc(),
                    receiver.accept(this, lhs));
        }
    }

    @Override
    public Expression visit(InvokeExpression expression, VariableExpression lhs) {
        Expression receiver = expression.getReceiver() == null ? null : expression.getReceiver().accept(this, lhs);
        Expression[] arguments = new Expression[expression.getArguments().length];
        for(int i = 0; i < arguments.length; i++) {
            arguments[i] = expression.getArguments()[i].accept(this, lhs);
        }
        return new InvokeExpression(expression.getOwner(), expression.getName(), expression.getDesc(), receiver,
                arguments, expression.getType());
    }

    @Override
    public Expression visit(NewArrayExpression expression, VariableExpression lhs) {
        Expression[] dims = new Expression[expression.getDims().length];
        for(int i = 0; i < dims.length; i++) {
            Expression dimension = expression.getDims()[i];
            if(dimension != null) {
                dims[i] = dimension.accept(this, lhs);
            }
        }
        return new NewArrayExpression(expression.getDesc(), dims);
    }

    @Override
    public Expression visit(NewExpression expression, VariableExpression lhs) {
        return expression;
    }

    @Override
    public Expression visit(ParameterExpression expression, VariableExpression lhs) {
        return expression;
    }

    @Override
    public Expression visit(UnaryExpression expression, VariableExpression lhs) {
        UnaryOperation operation = expression.getOperation();
        Expression operand = expression.getOperand().accept(this, lhs);
        if(operation.canPerform(operand)) {
            return operation.perform(operand);
        } else {
            return new UnaryExpression(operation, operand);
        }
    }

    @Override
    public Expression visit(LocalVariable expression, VariableExpression lhs) {
        if(currentDefinitions.containsKey(expression)) {
            return currentDefinitions.get(expression);
        } else {
            return expression;
        }
    }

    @Override
    public Expression visit(StackElement expression, VariableExpression lhs) {
        if(currentDefinitions.containsKey(expression)) {
            return currentDefinitions.get(expression);
        } else {
            return expression;
        }
    }

    @Override
    public Expression visit(PhiFunction expression, VariableExpression lhs) {
        Set<Expression> values = new HashSet<>();
        for(Expression value : expression.getValues()) {
            Expression propagated = value.accept(this, lhs);
            if(propagated.equals(lhs)) {
                values.add(value);
            } else {
                values.add(propagated);
            }
        }
        values = mergeConstants(values);
        if(values.size() == 1) {
            return values.iterator().next();
        }
        return new PhiFunction(values);
    }

    private Set<Expression> mergeConstants(Set<Expression> values) {
        Set<Expression> result = new HashSet<>();
        for(Expression v1 : values) {
            boolean keep = true;
            if(v1 instanceof ConstantExpression) {
                for(Expression v2 : values) {
                    if(v2 instanceof ConstantExpression && v1 != v2
                            && ((ConstantExpression) v2).canMerge((ConstantExpression) v1)) {
                        keep = false;
                        break;
                    }
                }
            }
            if(keep) {
                result.add(v1);
            }
        }
        return result;
    }

    @Override
    public Statement visit(AssignmentStatement statement) {
        Expression rhs;
        if(statement.definesVariable()) {
            rhs = statement.getRightHandSide().accept(this, statement.getDefinedVariable());
        } else {
            rhs = statement.getRightHandSide().accept(this, null);
        }
        return new AssignmentStatement(statement.getLeftHandSide(), rhs);
    }

    @Override
    public Statement visit(FrameStatement statement) {
        return statement;
    }

    @Override
    public Statement visit(GoToStatement statement) {
        return statement;
    }

    @Override
    public Statement visit(IdleStatement statement) {
        return statement;
    }

    @Override
    public Statement visit(IfStatement statement) {
        return new IfStatement(statement.getExpression().accept(this, null), statement.getTarget());
    }

    @Override
    public Statement visit(InvokeStatement statement) {
        return new InvokeStatement((InvokeExpression) statement.getExpression().accept(this, null));
    }

    @Override
    public Statement visit(LabelStatement statement) {
        return statement;
    }

    @Override
    public Statement visit(LineNumberStatement statement) {
        return statement;
    }

    @Override
    public Statement visit(MonitorStatement statement) {
        Expression expression = statement.getExpression().accept(this, null);
        return new MonitorStatement(statement.getOperation(), expression);
    }

    @Override
    public Statement visit(ReturnStatement statement) {
        if(statement.isVoid()) {
            return statement;
        } else {
            return new ReturnStatement(statement.getExpression().accept(this, null));
        }
    }

    @Override
    public Statement visit(SwitchStatement statement) {
        Expression expression = statement.getExpression().accept(this, null);
        return new SwitchStatement(expression, statement.getDefaultLabel(), statement.getLabels(), statement.getKeys());
    }

    @Override
    public Statement visit(ThrowStatement statement) {
        return new ThrowStatement(statement.getExpression().accept(this, null));
    }

    public static boolean canPropagate(Expression valueExpr) {
        if(valueExpr instanceof ConstantExpression || valueExpr instanceof ParameterExpression
                || valueExpr instanceof VariableExpression) {
            return true;
        } else if(valueExpr instanceof BinaryExpression) {
            Expression operand1 = ((BinaryExpression) valueExpr).getOperand1();
            Expression operand2 = ((BinaryExpression) valueExpr).getOperand2();
            return canPropagate(operand1) && canPropagate(operand2);
        } else if(valueExpr instanceof UnaryExpression) {
            Expression operand = ((UnaryExpression) valueExpr).getOperand();
            return canPropagate(operand);
        } else {
            return false;
        }
    }
}
