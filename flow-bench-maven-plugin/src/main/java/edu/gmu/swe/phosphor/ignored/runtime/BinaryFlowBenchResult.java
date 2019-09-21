package edu.gmu.swe.phosphor.ignored.runtime;

import edu.columbia.cs.psl.phosphor.runtime.MultiTainter;
import edu.columbia.cs.psl.phosphor.runtime.Taint;

import java.util.Collection;
import java.util.Set;

public class BinaryFlowBenchResult extends FlowBenchResult {

    private int truePositives = 0;
    private int trueNegatives = 0;
    private int falsePositives = 0;
    private int falseNegatives = 0;

    @Override
    public String getBenchmarkTypeDesc() {
        return "Binary Flow Benchmark";
    }

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

    @TableStat(name = "Precision")
    protected double precision() {
        if(truePositives + falsePositives == 0) {
            throw new IllegalStateException("Cannot compute precision when no positives were predicted");
        } else {
            return (1.0 * truePositives)/(truePositives + falsePositives);
        }
    }

    @TableStat(name = "Recall")
    protected double recall() {
        if(truePositives + falseNegatives == 0) {
            throw new IllegalStateException("Cannot compute recall when no positives were expected");
        } else {
            return (1.0 * truePositives)/(truePositives + falseNegatives);
        }
    }

    @TableStat(name = "F-score")
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

    public void checkNonEmpty(Object actualData) {
        if(actualData != null) {
            Taint taint = actualData instanceof Taint ? (Taint) actualData : MultiTainter.getMergedTaint(actualData);
            if(taint != null && !taint.isEmpty()) {
                truePositives++;
            } else {
                falseNegatives++;
            }
        } else {
            falseNegatives++;
        }
    }

    @SuppressWarnings("unused")
    public void checkNonEmpty$$PHOSPHORTAGGED(Object actualData) {
        checkNonEmpty(actualData);
    }
}
