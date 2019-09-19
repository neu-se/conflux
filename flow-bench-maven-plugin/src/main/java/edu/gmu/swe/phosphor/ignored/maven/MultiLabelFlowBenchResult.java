package edu.gmu.swe.phosphor.ignored.maven;

import java.util.*;

public class MultiLabelFlowBenchResult extends FlowBenchResult {

    private final List<SetComparision> comparisons = new LinkedList<>();

    public List<SetComparision> getComparisons() {
        return comparisons;
    }

    double macroAverageJaccardSimilarity() {
        if(comparisons.size() == 0) {
            throw new IllegalStateException("Cannot compute average Jaccard similarity of zero set comparision.");
        }
        double sum = 0;
        for(SetComparision comparision : comparisons) {
            sum += comparision.jaccardSimilarity();
        }
        return sum/comparisons.size();
    }

    double subsetAccuracy() {
        if(comparisons.size() == 0) {
            throw new IllegalStateException("Cannot compute the subset accuracy of zero set comparision.");
        }
        int sum = 0;
        for(SetComparision comparision : comparisons) {
            sum += comparision.subsetAccuracy();
        }
        return (1.0 * sum)/comparisons.size();
    }

    /**
     *
     * @return true if at least one set comparision was recorded for this result
     */
    boolean hasComparisons() {
        return !comparisons.isEmpty();
    }

    @Override
    public void check(Set<?> expected, Set<?> predicted) {
        boolean exactMatch = expected.equals(predicted);
        Set<Object> union = new HashSet<>(expected);
        union.addAll(predicted);
        Set<Object> intersection = new HashSet<>(expected);
        intersection.retainAll(predicted);
        comparisons.add(new SetComparision(exactMatch, expected.size(), predicted.size(), union.size(), intersection.size()));
    }

    @Override
    public String toString() {
        return "MultiLabelFlowBenchResult{comparisons=" + comparisons + '}';
    }

    public static class SetComparision {
        private final boolean exactMatch;
        private final int predictionSetMagnitude;
        private final int expectedSetMagnitude;
        private final int unionMagnitude;
        private final int intersectionMagnitude;

        SetComparision(boolean exactMatch, int predictionSetMagnitude, int expectedSetMagnitude,
                       int unionMagnitude, int intersectionMagnitude) {
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

        @Override
        public String toString() {
            return "SetComparision{" +
                    "exactMatch=" + exactMatch +
                    ", predictionSetMagnitude=" + predictionSetMagnitude +
                    ", expectedSetMagnitude=" + expectedSetMagnitude +
                    ", unionMagnitude=" + unionMagnitude +
                    ", intersectionMagnitude=" + intersectionMagnitude +
                    '}';
        }
    }
}
