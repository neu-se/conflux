package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.VariableTransformer;

public final class FieldAccess implements Expression {

    private final String owner;
    private final String name;
    private final String desc;
    private final Expression receiver;

    public FieldAccess(String owner, String name, String desc, Expression receiver) {
        if(owner == null || name == null || desc == null) {
            throw new NullPointerException();
        }
        this.owner = owner;
        this.name = name;
        this.desc = desc;
        this.receiver = receiver;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public Expression getReceiver() {
        return receiver;
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
        } else if(!(o instanceof FieldAccess)) {
            return false;
        }
        FieldAccess that = (FieldAccess) o;
        if(!owner.equals(that.owner)) {
            return false;
        }
        if(!name.equals(that.name)) {
            return false;
        }
        if(!desc.equals(that.desc)) {
            return false;
        }
        return receiver != null ? receiver.equals(that.receiver) : that.receiver == null;
    }

    @Override
    public int hashCode() {
        int result = owner.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + desc.hashCode();
        result = 31 * result + (receiver != null ? receiver.hashCode() : 0);
        return result;
    }

    @Override
    public List<VariableExpression> referencedVariables() {
        return Statement.gatherVersionedExpressions(receiver);
    }

    @Override
    public FieldAccess transform(VariableTransformer transformer) {
        if(receiver == null) {
            return this;
        } else {
            return new FieldAccess(owner, name, desc, receiver.transform(transformer));
        }
    }

    @Override
    public <V> V accept(ExpressionVisitor<V> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <V, S> V accept(StatefulExpressionVisitor<V, ? super S> visitor, S state) {
        return visitor.visit(this, state);
    }
}
