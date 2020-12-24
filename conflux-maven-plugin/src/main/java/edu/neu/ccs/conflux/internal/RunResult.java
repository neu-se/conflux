package edu.neu.ccs.conflux.internal;

public final class RunResult {
    private final int truePositives;
    private final int falsePositives;
    private final int falseNegatives;

    public RunResult(int truePositives, int falsePositives, int falseNegatives) {
        this.truePositives = truePositives;
        this.falsePositives = falsePositives;
        this.falseNegatives = falseNegatives;
    }

    public double precision() {
        if (truePositives() + falsePositives() == 0) {
            return 0; // undefined, no labels were predicted
        } else {
            return (1.0 * truePositives()) / (truePositives() + falsePositives());
        }
    }

    public double recall() {
        if (truePositives() + falseNegatives() == 0) {
            return 0; // undefined, no labels were expected
        } else {
            return (1.0 * truePositives()) / (truePositives() + falseNegatives());
        }
    }

    @TableStat(name = "F1", emphasizeMax = true)
    @PlotStat(name = "F1 Score")
    public double f1Score() {
        if (truePositives() == 0) {
            return 0;
        }
        double denominator = (2.0 * truePositives() + falsePositives() + falseNegatives());
        return (2.0 * truePositives()) / denominator;
    }

    @TableStat(name = "TP")
    public int truePositives() {
        return truePositives;
    }

    @TableStat(name = "FP")
    public int falsePositives() {
        return falsePositives;
    }

    @TableStat(name = "FN")
    public int falseNegatives() {
        return falseNegatives;
    }

    @Override
    public String toString() {
        return "RunResult{TP=" + truePositives() + ", FP=" + falsePositives() + ", FN=" + falseNegatives() + '}';
    }
}
