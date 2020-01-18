package edu.gmu.swe.phosphor.ignored.runtime;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class FlowBenchResultImpl extends FlowBenchResult {

    private final List<SetComparison> comparisons = new LinkedList<>();

    @Override
    public String getBenchmarkTypeDesc() {
        return "Flow Benchmark";
    }

    public List<SetComparison> getComparisons() {
        return comparisons;
    }

    double jaccardSimilarity() {
        if(comparisons.size() == 0) {
            throw new IllegalStateException("No samples were evaluated");
        }
        double sum = 0;
        for(SetComparison comparison : comparisons) {
            sum += comparison.jaccardSimilarity();
        }
        return sum / comparisons.size();
    }

    double subsetAccuracy() {
        if(comparisons.size() == 0) {
            throw new IllegalStateException("No samples were evaluated");
        }
        int sum = 0;
        for(SetComparison comparison : comparisons) {
            sum += comparison.subsetAccuracy();
        }
        return (1.0 * sum) / comparisons.size();
    }

    @TableStat(name = "Precision")
    double precision() {
        if(comparisons.size() == 0) {
            throw new IllegalStateException("No samples were evaluated");
        }
        double sum = 0;
        for(SetComparison comparison : comparisons) {
            sum += comparison.precision();
        }
        return sum / comparisons.size();
    }

    @TableStat(name = "Recall")
    double recall() {
        if(comparisons.size() == 0) {
            throw new IllegalStateException("No samples were evaluated");
        }
        double sum = 0;
        for(SetComparison comparison : comparisons) {
            sum += comparison.recall();
        }
        return sum / comparisons.size();
    }

    @TableStat(name = "F1")
    double f1Score() {
        if(comparisons.size() == 0) {
            throw new IllegalStateException("No samples were evaluated");
        }
        double sum = 0;
        for(SetComparison comparison : comparisons) {
            sum += comparison.f1Score();
        }
        return sum / comparisons.size();
    }

    @Override
    public void check(Set<?> expected, Set<?> predicted) {
        boolean exactMatch = expected.equals(predicted);
        Set<Object> union = new HashSet<>(expected);
        union.addAll(predicted);
        Set<Object> intersection = new HashSet<>(expected);
        intersection.retainAll(predicted);
        comparisons.add(new SetComparison(exactMatch, expected.size(), predicted.size(), union.size(), intersection.size()));
    }

    @Override
    public String toString() {
        return "FlowBenchResult{comparisons=" + comparisons + '}';
    }

    private static class SetComparison {
        private final boolean exactMatch;
        private final int predictedSetMagnitude;
        private final int expectedSetMagnitude;
        private final int unionMagnitude;
        private final int intersectionMagnitude;

        SetComparison(boolean exactMatch, int expectedSetMagnitude, int predictedSetMagnitude, int unionMagnitude,
                      int intersectionMagnitude) {
            if(predictedSetMagnitude < 0 || expectedSetMagnitude < 0 || unionMagnitude < 0 || intersectionMagnitude < 0) {
                throw new IllegalArgumentException("Set magnitudes must be non-negative");
            }
            this.exactMatch = exactMatch;
            this.predictedSetMagnitude = predictedSetMagnitude;
            this.expectedSetMagnitude = expectedSetMagnitude;
            this.unionMagnitude = unionMagnitude;
            this.intersectionMagnitude = intersectionMagnitude;
        }

        private double jaccardSimilarity() {
            return unionMagnitude == 0 ? 1 : (1.0 * intersectionMagnitude) / unionMagnitude;
        }

        private int subsetAccuracy() {
            return exactMatch ? 1 : 0;
        }

        private double precision() {
            if(predictedSetMagnitude == 0) {
                return 1;
            } else {
                return (1.0 * intersectionMagnitude) / predictedSetMagnitude;
            }
        }

        private double recall() {
            if(expectedSetMagnitude == 0) {
                return 1;
            } else {
                return (1.0 * intersectionMagnitude) / expectedSetMagnitude;
            }
        }

        private double f1Score() {
            if(predictedSetMagnitude + expectedSetMagnitude == 0) {
                return 1;
            } else {
                return (2.0 * intersectionMagnitude)/(predictedSetMagnitude + expectedSetMagnitude);
            }
        }

        @Override
        public String toString() {
            return "SetComparison{" +
                    "exactMatch=" + exactMatch +
                    ", predictionSetMagnitude=" + predictedSetMagnitude +
                    ", expectedSetMagnitude=" + expectedSetMagnitude +
                    ", unionMagnitude=" + unionMagnitude +
                    ", intersectionMagnitude=" + intersectionMagnitude +
                    '}';
        }
    }
}
