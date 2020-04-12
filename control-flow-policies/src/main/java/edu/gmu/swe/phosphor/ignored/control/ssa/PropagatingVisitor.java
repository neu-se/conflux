package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashMap;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.*;

/**
 * Perform basic constant propagation, constant folding, and copy-propagation.
 */
public class PropagatingVisitor implements StatefulExpressionVisitor<Expression, VariableExpression>, StatementVisitor<Statement> {

    private final Map<VariableExpression, Expression> propagatingDefinitions = new HashMap<>();

    public PropagatingVisitor(FlowGraph<? extends AnnotatedBasicBlock> graph) {
        Map<VariableExpression, Expression> currentDefinitions = new HashMap<>();
        for(AnnotatedBasicBlock block : graph.getVertices()) {
            for(AnnotatedInstruction insn : block.getInstructions()) {
                for(Statement statement : insn.getStatements()) {
                    if(statement.definesVariable() && statement instanceof AssignmentStatement) {
                        Expression rhs = ((AssignmentStatement) statement).getRightHandSide();
                        if(canPropagate(rhs)) {
                            propagatingDefinitions.put(statement.getDefinedVariable(), rhs);
                        }
                        currentDefinitions.put(statement.getDefinedVariable(), rhs);
                    }
                }
            }
        }
        boolean changed;
        do {
            changed = false;
            for(Map.Entry<VariableExpression, Expression> entry : currentDefinitions.entrySet()) {
                VariableExpression lhs = entry.getKey();
                Expression rhs = currentDefinitions.get(lhs);
                Expression transformed = rhs.accept(this, lhs);
                if(!rhs.equals(transformed)) {
                    changed = true;
                    entry.setValue(transformed);
                    if(canPropagate(transformed)) {
                        propagatingDefinitions.put(lhs, transformed);
                    }
                }
            }
        } while(changed);
    }

    private boolean canPropagate(Expression valueExpr) {
        return valueExpr instanceof ConstantExpression
                || valueExpr instanceof ParameterExpression
                || valueExpr instanceof VariableExpression;
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
        if(propagatingDefinitions.containsKey(expression)) {
            return propagatingDefinitions.get(expression);
        } else {
            return expression;
        }
    }

    @Override
    public Expression visit(StackElement expression, VariableExpression lhs) {
        if(propagatingDefinitions.containsKey(expression)) {
            return propagatingDefinitions.get(expression);
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
        if(values.size() == 1) {
            return values.iterator().next();
        }
        return new PhiFunction(values);
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
}
