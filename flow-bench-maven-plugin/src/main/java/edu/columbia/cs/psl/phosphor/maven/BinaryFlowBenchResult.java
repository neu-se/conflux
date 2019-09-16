package edu.columbia.cs.psl.phosphor.maven;

import edu.columbia.cs.psl.phosphor.runtime.Taint;

import java.util.Collection;
import java.util.HashSet;
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

    @Override
    public String toString() {
        return "BinaryFlowBenchResult{" +
                "truePositives=" + truePositives +
                ", trueNegatives=" + trueNegatives +
                ", falsePositives=" + falsePositives +
                ", falseNegatives=" + falseNegatives +
                '}';
    }

    protected double precision() {
        return (1.0 * truePositives)/(truePositives + falsePositives);
    }

    protected double recall() {
        return (1.0 * truePositives)/(truePositives + falseNegatives);
    }
    
    protected double f1Score() {
        return 2 * (precision() * recall())/(precision() + recall());
    }
    
    protected static double macroAveragePrecision(Collection<BinaryFlowBenchResult> results) {
        if(results.size() == 0) {
            throw new IllegalArgumentException();
        }
        double sum = 0;
        for(BinaryFlowBenchResult result : results) {
            sum += result.precision();
        }
        return sum/results.size();
    }
    
    protected static double microAveragePrecision(Collection<BinaryFlowBenchResult> results) {
        if(results.size() == 0) {
            throw new IllegalArgumentException();
        }
        double num = 0;
        double denom = 0;
        for(BinaryFlowBenchResult result : results) {
            num += result.truePositives;
            denom += result.truePositives + result.falsePositives;
        }
        return (denom == 0) ? 0 : num/denom;
    }

    protected static double macroAverageRecall(Collection<BinaryFlowBenchResult> results) {
        if(results.size() == 0) {
            throw new IllegalArgumentException();
        }
        double sum = 0;
        for(BinaryFlowBenchResult result : results) {
            sum += result.recall();
        }
        return sum/results.size();
    }

    protected static double microAverageRecall(Collection<BinaryFlowBenchResult> results) {
        if(results.size() == 0) {
            throw new IllegalArgumentException();
        }
        double num = 0;
        double denom = 0;
        for(BinaryFlowBenchResult result : results) {
            num += result.truePositives;
            denom += result.truePositives + result.falseNegatives;
        }
        return (denom == 0) ? 0 : num/denom;
    }

    protected static double macroAverageF1Score(Collection<BinaryFlowBenchResult> results) {
        return 2 * (macroAveragePrecision(results) * macroAverageRecall(results))/(macroAveragePrecision(results) + macroAverageRecall(results));
    }

    protected static double microAverageF1Score(Collection<BinaryFlowBenchResult> results) {
        return 2 * (microAveragePrecision(results) * microAverageRecall(results))/(microAveragePrecision(results) + microAverageRecall(results));
    }

    @Override
    public void check(Set<?> expected, Set<?> actual) {
        if(expected.isEmpty()) {
            if(actual.isEmpty()) {
                trueNegatives++;
            } else {
                falsePositives++;
            }
        } else {
            if(actual.isEmpty()) {
                falseNegatives++;
            } else {
                truePositives++;
            }
        }
    }
}
