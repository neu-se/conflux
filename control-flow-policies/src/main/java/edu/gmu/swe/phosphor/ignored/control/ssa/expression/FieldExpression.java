package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

public class FieldExpression implements Expression {

    public final String owner;
    public final String name;
    public final Expression receiver;

    public FieldExpression(String owner, String name, Expression receiver) {
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
}
