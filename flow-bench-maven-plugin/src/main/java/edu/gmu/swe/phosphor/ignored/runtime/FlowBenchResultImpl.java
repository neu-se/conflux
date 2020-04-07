package edu.gmu.swe.phosphor.ignored.runtime;

import java.util.*;

public class FlowBenchResultImpl extends FlowBenchResult {

    private final List<SetComparison> comparisons = new LinkedList<>();
    ConfusionMatrix globalMatrix = new ConfusionMatrix();
    private Map<Object, ConfusionMatrix> confusionMatrices = null;

    @Override
    public String getBenchmarkTypeDesc() {
        return "Flow Benchmark";
    }

    public double precision() {
        initializeConfusionMatrices();
        if(globalMatrix.truePositives + globalMatrix.falsePositives == 0) {
            return 0; // undefined, no labels were predicted
        } else {
            return (1.0 * globalMatrix.truePositives) / (globalMatrix.truePositives + globalMatrix.falsePositives);
        }
    }

    public double recall() {
        initializeConfusionMatrices();
        if(globalMatrix.truePositives + globalMatrix.falseNegatives == 0) {
            return 0; // undefined, no labels were expected
        } else {
            return (1.0 * globalMatrix.truePositives) / (globalMatrix.truePositives + globalMatrix.falseNegatives);
        }
    }

    @TableStat(name = "F1")
    public double f1Score() {
        initializeConfusionMatrices();
        if(globalMatrix.truePositives == 0) {
            return 0;
        }
        double denominator = (2.0 * globalMatrix.truePositives + globalMatrix.falsePositives + globalMatrix.falseNegatives);
        return (2.0 * globalMatrix.truePositives) / denominator;
    }

    @TableStat(name = "TP")
    public int truePositives() {
        initializeConfusionMatrices();
        return globalMatrix.truePositives;
    }

    @TableStat(name = "FP")
    public int falsePositives() {
        initializeConfusionMatrices();
        return globalMatrix.falsePositives;
    }

    @TableStat(name = "FN")
    public int falseNegatives() {
        initializeConfusionMatrices();
        return globalMatrix.falseNegatives;
    }

    @Override
    public void check(Set<?> expected, Set<?> predicted) {
        comparisons.add(new SetComparison(expected, predicted));
    }

    private void initializeConfusionMatrices() {
        if(confusionMatrices == null) {
            confusionMatrices = new HashMap<>();
            for(SetComparison comparison : comparisons) {
                for(Object label : comparison.expected) {
                    confusionMatrices.putIfAbsent(label, new ConfusionMatrix());
                }
                for(Object label : comparison.predicted) {
                    confusionMatrices.putIfAbsent(label, new ConfusionMatrix());
                }
            }
            for(SetComparison comparison : comparisons) {
                for(Object label : confusionMatrices.keySet()) {
                    ConfusionMatrix confusionMatrix = confusionMatrices.get(label);
                    if(comparison.expected.contains(label)) {
                        if(comparison.predicted.contains(label)) {
                            confusionMatrix.truePositives++;
                            globalMatrix.truePositives++;
                        } else {
                            confusionMatrix.falseNegatives++;
                            globalMatrix.falseNegatives++;
                        }
                    } else {
                        if(comparison.predicted.contains(label)) {
                            confusionMatrix.falsePositives++;
                            globalMatrix.falsePositives++;
                        } else {
                            confusionMatrix.trueNegatives++;
                            globalMatrix.trueNegatives++;
                        }
                    }
                }
            }
        }
    }

    private static class SetComparison {

        private final Set<Object> expected;
        private final Set<Object> predicted;

        private SetComparison(Set<?> expected, Set<?> predicted) {
            this.expected = new HashSet<>(expected);
            this.predicted = new HashSet<>(predicted);
        }
    }

    private static class ConfusionMatrix {
        private int truePositives = 0;
        private int falsePositives = 0;
        private int trueNegatives = 0;
        private int falseNegatives = 0;
    }
}
