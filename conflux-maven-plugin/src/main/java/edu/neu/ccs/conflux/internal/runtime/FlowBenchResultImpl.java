package edu.neu.ccs.conflux.internal.runtime;

import java.util.*;

public final class FlowBenchResultImpl extends FlowBenchResult {

    private final Map<Integer, RunResult> runResults = new HashMap<>();
    private RunResult currentRun;

    public double precision(int numberOfEntities) {
        return runResults.get(numberOfEntities).precision();
    }

    public double recall(int numberOfEntities) {
        return runResults.get(numberOfEntities).recall();
    }

    @TableStat(name = "F1", emphasizeMax = true)
    @PlotStat(name = "F1 Score")
    public double f1Score(int numberOfEntities) {
        return runResults.get(numberOfEntities).f1Score();
    }

    @TableStat(name = "TP")
    public int truePositives(int numberOfEntities) {
        return runResults.get(numberOfEntities).truePositives();
    }

    @TableStat(name = "FP")
    public int falsePositives(int numberOfEntities) {
        return runResults.get(numberOfEntities).falsePositives();
    }

    @TableStat(name = "FN")
    public int falseNegatives(int numberOfEntities) {
        return runResults.get(numberOfEntities).falseNegatives();
    }

    @Override
    public void startingRun(int numberOfEntities) {
        currentRun = new RunResult();
        runResults.put(numberOfEntities, currentRun);
    }

    @Override
    public void check(Set<?> expected, Set<?> predicted) {
        currentRun.check(expected, predicted);
    }
}
