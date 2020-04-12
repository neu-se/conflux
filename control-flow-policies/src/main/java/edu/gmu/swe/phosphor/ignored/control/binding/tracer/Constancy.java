package edu.gmu.swe.phosphor.ignored.control.binding.tracer;

import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph.NaturalLoop;
import edu.columbia.cs.psl.phosphor.struct.BitSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import edu.gmu.swe.phosphor.ignored.control.binding.LoopLevel;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.ParameterExpression;

import static edu.gmu.swe.phosphor.ignored.control.binding.LoopLevel.ConstantLoopLevel.CONSTANT_LOOP_LEVEL;

interface Constancy {

    Constancy CONSTANT = Constant.CONSTANT;

    <T> Constancy restrict(Set<NaturalLoop<T>> containingLoops);

    LoopLevel toLoopLevel();

    static Constancy merge(Constancy c1, Constancy c2) {
        if(c1 instanceof Constant) {
            return c2;
        } else if(c2 instanceof Constant) {
            return c1;
        } else if(c1 instanceof ParameterDependent && c2 instanceof ParameterDependent) {
            return new ParameterDependent((ParameterDependent) c1, (ParameterDependent) c2);
        } else if(c1 instanceof ParameterDependent) {
            return c2;
        } else if(c2 instanceof ParameterDependent) {
            return c1;
        } else {
            return new Nonconstant((Nonconstant) c1, (Nonconstant) c2);
        }
    }

    enum Constant implements Constancy {
        CONSTANT;

        @Override
        public <T> Constancy restrict(Set<NaturalLoop<T>> containingLoops) {
            return this;
        }

        @Override
        public LoopLevel toLoopLevel() {
            return CONSTANT_LOOP_LEVEL;
        }
    }

    final class ParameterDependent implements Constancy {

        private final BitSet dependencies;

        ParameterDependent(int dependency) {
            dependencies = new BitSet(dependency + 1);
            dependencies.add(dependency);
        }

        ParameterDependent(ParameterExpression expr) {
            dependencies = new BitSet(expr.getParameterNumber() + 1);
            dependencies.add(expr.getParameterNumber());
        }

        ParameterDependent(Constancy.ParameterDependent p1, Constancy.ParameterDependent p2) {
            dependencies = BitSet.union(p1.dependencies, p2.dependencies);
        }

        @Override
        public <T> Constancy restrict(Set<NaturalLoop<T>> containingLoops) {
            return this;
        }

        @Override
        public LoopLevel toLoopLevel() {
            return new LoopLevel.DependentLoopLevel(dependencies.toList().toArray());
        }
    }

    final class Nonconstant implements Constancy {
        private final Set<NaturalLoop<?>> nonconstantLoops = new HashSet<>();

        <T> Nonconstant(Set<NaturalLoop<T>> nonconstantLoops) {
            this.nonconstantLoops.addAll(nonconstantLoops);
        }

        Nonconstant(Nonconstant l1, Nonconstant l2) {
            nonconstantLoops.addAll(l1.nonconstantLoops);
            nonconstantLoops.addAll(l2.nonconstantLoops);
        }

        @Override
        public <T> Constancy restrict(Set<NaturalLoop<T>> containingLoops) {
            if(containingLoops.containsAll(nonconstantLoops)) {
                return this;
            } else {
                Nonconstant result = new Nonconstant(containingLoops);
                result.nonconstantLoops.retainAll(nonconstantLoops);
                return result;

            }
        }

        @Override
        public LoopLevel toLoopLevel() {
            return new LoopLevel.VariantLoopLevel(nonconstantLoops.size());
        }
    }
}
