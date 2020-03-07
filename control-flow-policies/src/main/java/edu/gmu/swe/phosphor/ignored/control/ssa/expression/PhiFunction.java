package edu.gmu.swe.phosphor.ignored.control.ssa.expression;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.Statement;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.VariableTransformer;

import java.util.Arrays;

public class PhiFunction implements Expression {

    private final Expression[] values;

    public PhiFunction(Set<? extends Expression> values) {
        this.values = values.toArray(new Expression[0]);
    }

    public Expression[] getValues() {
        return values.clone();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("phi<");
        for(int i = 0; i < values.length; i++) {
            builder.append(values[i]);
            if((i + 1) < values.length) {
                builder.append(", ");
            }
        }
        return builder.append(">").toString();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(!(o instanceof PhiFunction)) {
            return false;
        }
        PhiFunction that = (PhiFunction) o;
        return Arrays.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    @Override
    public List<VariableExpression> referencedVariables() {
        return Statement.gatherVersionedExpressions(values);
    }

    @Override
    public Expression transform(VariableTransformer transformer) {
        return transform(transformer, null);
    }

    @Override
    public Expression transform(VariableTransformer transformer, VariableExpression assignee) {
        Set<Expression> transformedValues = new HashSet<>();
        for(Expression value : values) {
            Expression transformed = value.transform(transformer);
            if(assignee != null && transformed.referencedVariables().contains(assignee)) {
                transformedValues.add(value);
            } else {
                transformedValues.add(transformed);
            }
        }
        if(transformer.foldingAllowed()) {
            transformedValues = mergeConstants(transformedValues);
            if(transformedValues.size() == 1) {
                return transformedValues.iterator().next();
            }
        }
        return new PhiFunction(transformedValues);
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
}
