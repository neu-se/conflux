package edu.gmu.swe.phosphor.ignored.runtime;

import java.util.*;

public class MultiLabelFlowBenchResult extends FlowBenchResult {

    private final List<SetComparison> comparisons = new LinkedList<>();

    @Override
    public String getBenchmarkTypeDesc() {
        return "Multi-label Flow Benchmark";
    }

    public List<SetComparison> getComparisons() {
        return comparisons;
    }

    @TableStat(name = "Jaccard Sim.")
    double macroAverageJaccardSimilarity() {
        if(comparisons.size() == 0) {
            throw new IllegalStateException("Cannot compute average Jaccard similarity of zero set comparisons");
        }
        double sum = 0;
        for(SetComparison comparison : comparisons) {
            sum += comparison.jaccardSimilarity();
        }
        return sum/comparisons.size();
    }

    @TableStat(name = "Subset Acc.")
    double subsetAccuracy() {
        if(comparisons.size() == 0) {
            throw new IllegalStateException("Cannot compute the subset accuracy of zero set comparisons");
        }
        int sum = 0;
        for(SetComparison comparison : comparisons) {
            sum += comparison.subsetAccuracy();
        }
        return (1.0 * sum)/comparisons.size();
    }

    double precision() {
        if(comparisons.size() == 0) {
            throw new IllegalStateException("Cannot compute the precision of zero set comparisons");
        }
        int truePositives = truePositives();
        int falsePositives = falsePositives();
        if(truePositives + falsePositives == 0) {
            return 1;
        } else {
            return (1.0 * truePositives)/(truePositives + falsePositives);
        }
    }

    double recall() {
        if(comparisons.size() == 0) {
            throw new IllegalStateException("Cannot compute the recall of zero set comparisons");
        }
        int truePositives = truePositives();
        int falseNegatives = falseNegatives();
        if(truePositives + falseNegatives == 0) {
            return 1;
        } else {
            return (1.0 * truePositives)/(truePositives + falseNegatives);
        }
    }

    private int truePositives() {
        int sum = 0;
        for(SetComparison comparison : comparisons) {
            sum += comparison.truePositives();
        }
        return sum;
    }

    private int trueNegatives() {
        int sum = 0;
        for(SetComparison comparison : comparisons) {
            sum += comparison.trueNegatives();
        }
        return sum;
    }

    private int falsePositives() {
        int sum = 0;
        for(SetComparison comparison : comparisons) {
            sum += comparison.falsePositives();
        }
        return sum;
    }

    private int falseNegatives() {
        int sum = 0;
        for(SetComparison comparison : comparisons) {
            sum += comparison.falseNegatives();
        }
        return sum;
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
        return "MultiLabelFlowBenchResult{comparisons=" + comparisons + '}';
    }

    private static class SetComparison {
        private final boolean exactMatch;
        private final int predictionSetMagnitude;
        private final int expectedSetMagnitude;
        private final int unionMagnitude;
        private final int intersectionMagnitude;

        SetComparison(boolean exactMatch, int expectedSetMagnitude, int predictionSetMagnitude, int unionMagnitude,
                      int intersectionMagnitude) {
            if(predictionSetMagnitude < 0 || expectedSetMagnitude < 0 || unionMagnitude < 0 || intersectionMagnitude < 0) {
                throw new IllegalArgumentException("Set magnitudes must be non-negative");
            }
            this.exactMatch = exactMatch;
            this.predictionSetMagnitude = predictionSetMagnitude;
            this.expectedSetMagnitude = expectedSetMagnitude;
            this.unionMagnitude = unionMagnitude;
            this.intersectionMagnitude = intersectionMagnitude;
        }

        private double jaccardSimilarity() {
            return unionMagnitude == 0 ? 1 : (1.0 * intersectionMagnitude)/unionMagnitude;
        }

        private int subsetAccuracy() {
            return exactMatch ? 1 : 0;
        }

        private int truePositives() {
            return expectedSetMagnitude > 0 && predictionSetMagnitude > 0 ? 1 : 0;
        }

        private int trueNegatives() {
            return expectedSetMagnitude == 0 && predictionSetMagnitude == 0 ? 1 : 0;
        }

        private int falsePositives() {
            return expectedSetMagnitude == 0 && predictionSetMagnitude > 0 ? 1 : 0;
        }

        private int falseNegatives() {
            return expectedSetMagnitude > 0 && predictionSetMagnitude == 0 ? 1 : 0;
        }

        @Override
        public String toString() {
            return "SetComparison{" +
                    "exactMatch=" + exactMatch +
                    ", predictionSetMagnitude=" + predictionSetMagnitude +
                    ", expectedSetMagnitude=" + expectedSetMagnitude +
                    ", unionMagnitude=" + unionMagnitude +
                    ", intersectionMagnitude=" + intersectionMagnitude +
                    '}';
        }
    }
}
