package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public final class FieldExpression implements Expression {

    public final String owner;
    public final String name;
    public final Expression receiver;

    public FieldExpression(String owner, String name, Expression receiver) {
        if(owner == null || name == null) {
            throw new NullPointerException();
        }
        this.owner = owner;
        this.name = name;
        this.receiver = receiver;
    }

    @Override
    public String toString() {
        if(receiver == null) {
            return String.format("%s.%s", owner, name);
        } else {
            return String.format("%s.%s", receiver, name);
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof FieldExpression)) {
            return false;
        }
        FieldExpression that = (FieldExpression) o;
        if(!owner.equals(that.owner)) {
            return false;
        }
        if(!name.equals(that.name)) {
            return false;
        }
        return receiver != null ? receiver.equals(that.receiver) : that.receiver == null;
    }

    @Override
    public int hashCode() {
        int result = owner.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (receiver != null ? receiver.hashCode() : 0);
        return result;
    }
}
