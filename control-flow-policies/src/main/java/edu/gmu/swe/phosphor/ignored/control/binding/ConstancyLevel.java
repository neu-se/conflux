package edu.gmu.swe.phosphor.ignored.control.binding;

import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph.NaturalLoop;
import edu.columbia.cs.psl.phosphor.struct.BitSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import edu.gmu.swe.phosphor.ignored.control.ssa.AnnotatedBasicBlock;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.ParameterExpression;

interface ConstancyLevel {

    LoopLevel toLoopLevel();

    static ConstancyLevel merge(ConstancyLevel c1, ConstancyLevel c2) {
        if(c1 instanceof ConstantLevel) {
            return c2;
        } else if(c2 instanceof ConstantLevel) {
            return c1;
        } else if(c1 instanceof ParameterDependent && c2 instanceof ParameterDependent) {
            return new ParameterDependent((ParameterDependent) c1, (ParameterDependent) c2);
        } else if(c1 instanceof ParameterDependent) {
            return c2;
        } else if(c2 instanceof ParameterDependent) {
            return c1;
        } else {
            return new LoopVariant((LoopVariant) c1, (LoopVariant) c2);
        }
    }

    enum ConstantLevel implements ConstancyLevel {
        CONSTANT_LEVEL;

        @Override
        public LoopLevel toLoopLevel() {
            return LoopLevel.ConstantLoopLevel.CONSTANT_LOOP_LEVEL;
        }
    }

    final class ParameterDependent implements ConstancyLevel {
        private BitSet dependencies;

        ParameterDependent(ParameterExpression expr) {
            dependencies = new BitSet(expr.getParameterNumber() + 1);
            dependencies.add(expr.getParameterNumber());
        }

        ParameterDependent(ParameterDependent p1, ParameterDependent p2) {
            dependencies = BitSet.union(p1.dependencies, p2.dependencies);
        }

        @Override
        public LoopLevel toLoopLevel() {
            return new LoopLevel.DependentLoopLevel(dependencies.toList().toArray());
        }
    }

    final class LoopVariant implements ConstancyLevel {
        private Set<NaturalLoop<AnnotatedBasicBlock>> variantLoops = new HashSet<>();

        LoopVariant(Set<NaturalLoop<AnnotatedBasicBlock>> variantLoops) {
            this.variantLoops.addAll(variantLoops);
        }

        LoopVariant(LoopVariant l1, LoopVariant l2) {
            variantLoops.addAll(l1.variantLoops);
            variantLoops.addAll(l2.variantLoops);
        }

        LoopVariant(LoopVariant l1, Set<NaturalLoop<AnnotatedBasicBlock>> containingLoops) {
            variantLoops.addAll(containingLoops);
            variantLoops.retainAll(l1.variantLoops);
        }

        @Override
        public LoopLevel toLoopLevel() {
            return new LoopLevel.VariantLoopLevel(variantLoops.size());
        }
    }
}
