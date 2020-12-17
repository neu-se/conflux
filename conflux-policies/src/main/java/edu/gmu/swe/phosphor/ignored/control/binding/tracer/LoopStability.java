package edu.gmu.swe.phosphor.ignored.control.binding.tracer;

import edu.columbia.cs.psl.phosphor.control.graph.FlowGraph.NaturalLoop;
import edu.columbia.cs.psl.phosphor.struct.BitSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import edu.gmu.swe.phosphor.ignored.control.binding.LoopLevel;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.ParameterExpression;

import static edu.gmu.swe.phosphor.ignored.control.binding.LoopLevel.StableLoopLevel.STABLE_LOOP_LEVEL;

interface LoopStability {

    LoopStability STABLE = Stable.STABLE;

    <T> LoopStability restrict(Set<NaturalLoop<T>> containingLoops);

    LoopLevel toLoopLevel();

    static LoopStability merge(LoopStability c1, LoopStability c2) {
        if(c1 instanceof Stable) {
            return c2;
        } else if(c2 instanceof Stable) {
            return c1;
        } else if(c1 instanceof ParameterDependent && c2 instanceof ParameterDependent) {
            return new ParameterDependent((ParameterDependent) c1, (ParameterDependent) c2);
        } else if(c1 instanceof ParameterDependent) {
            return c2;
        } else if(c2 instanceof ParameterDependent) {
            return c1;
        } else {
            return new Unstable((Unstable) c1, (Unstable) c2);
        }
    }

    enum Stable implements LoopStability {
        STABLE;

        @Override
        public <T> LoopStability restrict(Set<NaturalLoop<T>> containingLoops) {
            return this;
        }

        @Override
        public LoopLevel toLoopLevel() {
            return STABLE_LOOP_LEVEL;
        }
    }

    final class ParameterDependent implements LoopStability {

        private final BitSet dependencies;

        ParameterDependent(int dependency) {
            dependencies = new BitSet(dependency + 1);
            dependencies.add(dependency);
        }

        ParameterDependent(ParameterExpression expr) {
            dependencies = new BitSet(expr.getParameterNumber() + 1);
            dependencies.add(expr.getParameterNumber());
        }

        ParameterDependent(LoopStability.ParameterDependent p1, LoopStability.ParameterDependent p2) {
            dependencies = BitSet.union(p1.dependencies, p2.dependencies);
        }

        @Override
        public <T> LoopStability restrict(Set<NaturalLoop<T>> containingLoops) {
            return this;
        }

        @Override
        public LoopLevel toLoopLevel() {
            return new LoopLevel.DependentLoopLevel(dependencies.toList().toArray());
        }
    }

    final class Unstable implements LoopStability {
        private final Set<NaturalLoop<?>> unstableLoops = new HashSet<>();

        <T> Unstable(Set<NaturalLoop<T>> unstableLoops) {
            this.unstableLoops.addAll(unstableLoops);
        }

        Unstable(Unstable l1, Unstable l2) {
            unstableLoops.addAll(l1.unstableLoops);
            unstableLoops.addAll(l2.unstableLoops);
        }

        @Override
        public <T> LoopStability restrict(Set<NaturalLoop<T>> containingLoops) {
            if(containingLoops.containsAll(unstableLoops)) {
                return this;
            } else {
                Unstable result = new Unstable(containingLoops);
                result.unstableLoops.retainAll(unstableLoops);
                return result;

            }
        }

        @Override
        public LoopLevel toLoopLevel() {
            return new LoopLevel.VariantLoopLevel(unstableLoops.size());
        }
    }
}
