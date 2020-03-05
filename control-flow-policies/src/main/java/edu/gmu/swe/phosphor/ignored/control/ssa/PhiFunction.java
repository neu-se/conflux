package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VersionedExpression;

import java.util.LinkedList;
import java.util.List;

public class PhiFunction {

    private final Set<VersionedExpression> possibleValues = new HashSet<>();
    private VersionedExpression leftHandSide;

    public Set<VersionedExpression> getPossibleValues() {
        return possibleValues;
    }

    public VersionedExpression getLeftHandSide() {
        return leftHandSide;
    }

    public void setLeftHandSide(VersionedExpression leftHandSide) {
        this.leftHandSide = leftHandSide;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if(leftHandSide == null) {
            builder.append("?");
        } else {
            builder.append(leftHandSide);
        }
        builder.append(" = phi(");
        List<String> values = new LinkedList<>();
        for(VersionedExpression possibleValue : possibleValues) {
            values.add(possibleValue.toString());
        }
        builder.append(String.join(", ", values.toArray(new String[0])));
        return builder.append(")").toString();
    }
}
