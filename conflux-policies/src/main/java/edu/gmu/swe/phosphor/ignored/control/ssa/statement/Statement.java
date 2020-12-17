package edu.gmu.swe.phosphor.ignored.control.ssa.statement;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;

public interface Statement {

    default String toString(Map<Label, String> labelNames) {
        return toString();
    }

    default boolean definesVariable() {
        return false;
    }

    default VariableExpression getDefinedVariable() {
        throw new IllegalStateException();
    }

    <V> V accept(StatementVisitor<V> visitor);

    <V, S> V accept(StatefulStatementVisitor<V, S> visitor, S state);
}
