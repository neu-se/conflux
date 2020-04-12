package edu.gmu.swe.phosphor.ignored.control.binding.tracer;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Collections;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.*;

public class UseGatheringVisitor implements ExpressionVisitor<Set<VariableExpression>>, StatementVisitor<Set<VariableExpression>> {

    @Override
    public Set<VariableExpression> visit(ArrayAccess expression) {
        Set<VariableExpression> result = new HashSet<>();
        result.addAll(expression.getArrayRef().accept(this));
        result.addAll(expression.getIndex().accept(this));
        return result;
    }

    @Override
    public Set<VariableExpression> visit(BinaryExpression expression) {
        Set<VariableExpression> result = new HashSet<>();
        result.addAll(expression.getOperand1().accept(this));
        result.addAll(expression.getOperand2().accept(this));
        return result;
    }

    @Override
    public Set<VariableExpression> visit(CaughtExceptionExpression expression) {
        return Collections.emptySet();
    }

    @Override
    public Set<VariableExpression> visit(DoubleConstantExpression expression) {
        return Collections.emptySet();
    }

    @Override
    public Set<VariableExpression> visit(FloatConstantExpression expression) {
        return Collections.emptySet();
    }

    @Override
    public Set<VariableExpression> visit(IntegerConstantExpression expression) {
        return Collections.emptySet();
    }

    @Override
    public Set<VariableExpression> visit(LongConstantExpression expression) {
        return Collections.emptySet();
    }

    @Override
    public Set<VariableExpression> visit(ObjectConstantExpression expression) {
        return Collections.emptySet();
    }

    @Override
    public Set<VariableExpression> visit(FieldAccess expression) {
        if(expression.getReceiver() != null) {
            return expression.getReceiver().accept(this);
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public Set<VariableExpression> visit(InvokeExpression expression) {
        Set<VariableExpression> result = new HashSet<>();
        if(expression.getReceiver() != null) {
            result.addAll(expression.getReceiver().accept(this));
        }
        for(Expression arg : expression.getArguments()) {
            result.addAll(arg.accept(this));
        }
        return result;
    }

    @Override
    public Set<VariableExpression> visit(NewArrayExpression expression) {
        Set<VariableExpression> result = new HashSet<>();
        for(Expression dimension : expression.getDims()) {
            if(dimension != null) {
                result.addAll(dimension.accept(this));
            }
        }
        return result;
    }

    @Override
    public Set<VariableExpression> visit(NewExpression expression) {
        return Collections.emptySet();
    }

    @Override
    public Set<VariableExpression> visit(ParameterExpression expression) {
        return Collections.emptySet();
    }

    @Override
    public Set<VariableExpression> visit(PhiFunction expression) {
        Set<VariableExpression> result = new HashSet<>();
        for(Expression value : expression.getValues()) {
            result.addAll(value.accept(this));
        }
        return result;
    }

    @Override
    public Set<VariableExpression> visit(UnaryExpression expression) {
        return expression.getOperand().accept(this);
    }

    @Override
    public Set<VariableExpression> visit(LocalVariable expression) {
        return Collections.singleton(expression);
    }

    @Override
    public Set<VariableExpression> visit(StackElement expression) {
        return Collections.singleton(expression);
    }

    @Override
    public Set<VariableExpression> visit(AssignmentStatement statement) {
        if(statement.definesVariable()) {
            return statement.getRightHandSide().accept(this);
        } else {
            Set<VariableExpression> result = new HashSet<>();
            result.addAll(statement.getRightHandSide().accept(this));
            result.addAll(statement.getLeftHandSide().accept(this));
            return result;
        }
    }

    @Override
    public Set<VariableExpression> visit(FrameStatement statement) {
        return Collections.emptySet();
    }

    @Override
    public Set<VariableExpression> visit(GoToStatement statement) {
        return Collections.emptySet();
    }

    @Override
    public Set<VariableExpression> visit(IdleStatement statement) {
        return Collections.emptySet();
    }

    @Override
    public Set<VariableExpression> visit(IfStatement statement) {
        return statement.getExpression().accept(this);
    }

    @Override
    public Set<VariableExpression> visit(InvokeStatement statement) {
        return statement.getExpression().accept(this);
    }

    @Override
    public Set<VariableExpression> visit(LabelStatement statement) {
        return Collections.emptySet();
    }

    @Override
    public Set<VariableExpression> visit(LineNumberStatement statement) {
        return Collections.emptySet();
    }

    @Override
    public Set<VariableExpression> visit(MonitorStatement statement) {
        return statement.getExpression().accept(this);
    }

    @Override
    public Set<VariableExpression> visit(ReturnStatement statement) {
        if(statement.isVoid()) {
            return Collections.emptySet();
        } else {
            return statement.getExpression().accept(this);
        }
    }

    @Override
    public Set<VariableExpression> visit(SwitchStatement statement) {
        return statement.getExpression().accept(this);
    }

    @Override
    public Set<VariableExpression> visit(ThrowStatement statement) {
        return statement.getExpression().accept(this);
    }
}
