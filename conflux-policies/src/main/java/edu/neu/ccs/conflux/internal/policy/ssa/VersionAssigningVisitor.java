package edu.neu.ccs.conflux.internal.policy.ssa;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.*;
import edu.neu.ccs.conflux.internal.policy.ssa.expression.*;
import edu.neu.ccs.conflux.internal.policy.ssa.statement.*;

public class VersionAssigningVisitor implements ExpressionVisitor<Expression>, StatementVisitor<Statement> {

    private final Map<VariableExpression, VersionStack> versionStacks = new HashMap<>();

    public VersionAssigningVisitor(Set<VariableExpression> definedExpressions) {
        for(VariableExpression expression : definedExpressions) {
            if(expression == null) {
                throw new NullPointerException();
            }
            versionStacks.put(expression, new VersionStack(expression));
        }
    }

    public Map<VariableExpression, VersionStack> getVersionStacks() {
        return Collections.unmodifiableMap(versionStacks);
    }

    public void processingBlock() {
        for(VersionStack stack : versionStacks.values()) {
            stack.processingBlock();
        }
    }

    public void finishedProcessingBlock() {
        for(VersionStack stack : versionStacks.values()) {
            stack.finishedProcessingBlock();
        }
    }

    public VariableExpression createNewVersion(VariableExpression expression) {
        return versionStacks.get(expression).createNewVersion();
    }

    public boolean hasCurrentExpression(VariableExpression expression) {
        return versionStacks.get(expression).hasCurrentExpression();
    }

    public VariableExpression getCurrentExpression(VariableExpression expression) {
        return versionStacks.get(expression).getCurrentExpression();
    }

    @Override
    public Expression visit(ArrayAccess expression) {
        Expression arrayRef = expression.getArrayRef().accept(this);
        Expression index = expression.getIndex().accept(this);
        return new ArrayAccess(arrayRef, index);
    }

    @Override
    public Expression visit(BinaryExpression expression) {
        Expression operand1 = expression.getOperand1().accept(this);
        Expression operand2 = expression.getOperand2().accept(this);
        BinaryOperation operation = expression.getOperation();
        return new BinaryExpression(operation, operand1, operand2);
    }

    @Override
    public Expression visit(CaughtExceptionExpression expression) {
        return expression;
    }

    @Override
    public Expression visit(DoubleConstantExpression expression) {
        return expression;
    }

    @Override
    public Expression visit(FloatConstantExpression expression) {
        return expression;
    }

    @Override
    public Expression visit(IntegerConstantExpression expression) {
        return expression;
    }

    @Override
    public Expression visit(LongConstantExpression expression) {
        return expression;
    }

    @Override
    public Expression visit(ObjectConstantExpression expression) {
        return expression;
    }

    @Override
    public Expression visit(FieldAccess expression) {
        Expression receiver = expression.getReceiver();
        if(receiver == null) {
            return expression;
        } else {
            return new FieldAccess(expression.getOwner(), expression.getName(), expression.getDesc(),
                    receiver.accept(this));
        }
    }

    @Override
    public Expression visit(InvokeExpression expression) {
        Expression receiver = expression.getReceiver() == null ? null : expression.getReceiver().accept(this);
        Expression[] arguments = new Expression[expression.getArguments().length];
        for(int i = 0; i < arguments.length; i++) {
            arguments[i] = expression.getArguments()[i].accept(this);
        }
        return new InvokeExpression(expression.getOwner(), expression.getName(), expression.getDesc(), receiver,
                arguments, expression.getType());
    }

    @Override
    public Expression visit(NewArrayExpression expression) {
        Expression[] dims = new Expression[expression.getDims().length];
        for(int i = 0; i < dims.length; i++) {
            Expression dimension = expression.getDims()[i];
            if(dimension != null) {
                dims[i] = dimension.accept(this);
            }
        }
        return new NewArrayExpression(expression.getDesc(), dims);
    }

    @Override
    public Expression visit(NewExpression expression) {
        return expression;
    }

    @Override
    public Expression visit(ParameterExpression expression) {
        return expression;
    }

    @Override
    public Expression visit(UnaryExpression expression) {
        UnaryOperation operation = expression.getOperation();
        Expression operand = expression.getOperand().accept(this);
        return new UnaryExpression(operation, operand);
    }

    @Override
    public Expression visit(LocalVariable expression) {
        if(versionStacks.containsKey(expression)) {
            return versionStacks.get(expression).getCurrentExpression();
        } else {
            return expression;
        }
    }

    @Override
    public Expression visit(StackElement expression) {
        if(versionStacks.containsKey(expression)) {
            return versionStacks.get(expression).getCurrentExpression();
        } else {
            return expression;
        }
    }

    @Override
    public Expression visit(PhiFunction expression) {
        Set<Expression> values = new HashSet<>();
        for(Expression value : expression.getValues()) {
            values.add(value.accept(this));
        }
        return new PhiFunction(values);
    }

    @Override
    public Statement visit(AssignmentStatement statement) {
        Expression rhs = statement.getRightHandSide().accept(this);
        Expression lhs = statement.getLeftHandSide();
        if(lhs instanceof VariableExpression) {
            lhs = versionStacks.get(lhs).createNewVersion();
        } else {
            lhs = lhs.accept(this);
        }
        return new AssignmentStatement(lhs, rhs);
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
        return new IfStatement(statement.getExpression().accept(this), statement.getTarget());
    }

    @Override
    public Statement visit(InvokeStatement statement) {
        return new InvokeStatement((InvokeExpression) statement.getExpression().accept(this));
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
        Expression expression = statement.getExpression().accept(this);
        return new MonitorStatement(statement.getOperation(), expression);
    }

    @Override
    public Statement visit(ReturnStatement statement) {
        if(statement.isVoid()) {
            return statement;
        } else {
            return new ReturnStatement(statement.getExpression().accept(this));
        }
    }

    @Override
    public Statement visit(SwitchStatement statement) {
        Expression expression = statement.getExpression().accept(this);
        return new SwitchStatement(expression, statement.getDefaultLabel(), statement.getLabels(), statement.getKeys());
    }

    @Override
    public Statement visit(ThrowStatement statement) {
        return new ThrowStatement(statement.getExpression().accept(this));
    }
}
