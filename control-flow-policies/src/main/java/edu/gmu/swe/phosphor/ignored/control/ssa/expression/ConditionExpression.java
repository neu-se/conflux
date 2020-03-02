package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public final class ConditionExpression {

    private final Condition condition;
    private final Expression operand1;
    private final Expression operand2;

    public ConditionExpression(Condition condition, Expression operand1, Expression operand2) {
        if(condition == null || operand1 == null || operand2 == null) {
            throw new NullPointerException();
        }
        this.condition = condition;
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    @Override
    public String toString() {
        return condition.format(operand1, operand2);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof ConditionExpression)) {
            return false;
        }
        ConditionExpression that = (ConditionExpression) o;
        if(condition != that.condition) {
            return false;
        }
        if(!operand1.equals(that.operand1)) {
            return false;
        }
        return operand2.equals(that.operand2);
    }

    @Override
    public int hashCode() {
        int result = condition.hashCode();
        result = 31 * result + operand1.hashCode();
        result = 31 * result + operand2.hashCode();
        return result;
    }
}
