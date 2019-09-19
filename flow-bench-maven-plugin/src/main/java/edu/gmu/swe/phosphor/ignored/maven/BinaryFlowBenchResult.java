package edu.gmu.swe.phosphor.ignored.maven;

import java.util.Collection;
import java.util.Set;

public class BinaryFlowBenchResult extends FlowBenchResult {

    private int truePositives = 0;
    private int trueNegatives = 0;
    private int falsePositives = 0;
    private int falseNegatives = 0;

    protected int getTruePositives() {
        return truePositives;
    }

    protected int getTrueNegatives() {
        return trueNegatives;
    }

    protected int getFalsePositives() {
        return falsePositives;
    }

    protected int getFalseNegatives() {
        return falseNegatives;
    }

    protected double precision() {
        if(truePositives + falsePositives == 0) {
            return 1;
        } else {
            return (1.0 * truePositives)/(truePositives + falsePositives);
        }
    }

    protected double recall() {
        if(truePositives + falseNegatives == 0) {
            return 1;
        } else {
            return (1.0 * truePositives)/(truePositives + falseNegatives);
        }
    }
    
    protected double f1Score() {
        double precision = precision();
        double recall = recall();
        if(precision + recall == 0) {
            return 0;
        } else {
            return 2 * (precision * recall)/(precision + recall);
        }
    }
    
    protected static double macroAveragePrecision(Collection<BinaryFlowBenchResult> results) {
        if(results.size() == 0) {
            throw new IllegalArgumentException("Cannot calculate the average precision of an empty list");
        }
        double sum = 0;
        for(BinaryFlowBenchResult result : results) {
            sum += result.precision();
        }
        return sum/results.size();
    }

    protected static double macroAverageRecall(Collection<BinaryFlowBenchResult> results) {
        if(results.size() == 0) {
            throw new IllegalArgumentException("Cannot calculate the average recall of an empty list");
        }
        double sum = 0;
        for(BinaryFlowBenchResult result : results) {
            sum += result.recall();
        }
        return sum/results.size();
    }

    protected static double macroAverageF1Score(Collection<BinaryFlowBenchResult> results) {
        double precision = macroAveragePrecision(results);
        double recall = macroAverageRecall(results);
        if(precision + recall == 0) {
            return 0;
        } else {
            return 2 * (precision * recall)/(precision + recall);
        }
    }

    @Override
    public void check(Set<?> expected, Set<?> predicted) {
        if(expected.isEmpty()) {
            if(predicted.isEmpty()) {
                trueNegatives++;
            } else {
                falsePositives++;
            }
        } else {
            if(predicted.isEmpty()) {
                falseNegatives++;
            } else {
                truePositives++;
            }
        }
    }

    @Override
    public String toString() {
        return "BinaryFlowBenchResult{" +
                "truePositives=" + truePositives +
                ", trueNegatives=" + trueNegatives +
                ", falsePositives=" + falsePositives +
                ", falseNegatives=" + falseNegatives +
                '}';
    }
}
