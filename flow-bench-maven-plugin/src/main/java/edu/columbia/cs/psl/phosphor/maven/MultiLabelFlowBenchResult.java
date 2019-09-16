package edu.columbia.cs.psl.phosphor.maven;

import java.util.Arrays;

public class MultiLabelFlowBenchResult implements FlowBenchResult {

    private final int[] correctLabels;
    private final int[] predictedLabels;
    private final int[] expectedLabels;

    public MultiLabelFlowBenchResult(int[] correctLabels, int[] predictedLabels, int[] expectedLabels) {
        this.correctLabels = correctLabels;
        this.predictedLabels = predictedLabels;
        this.expectedLabels = expectedLabels;
    }

    public int[] getCorrectLabels() {
        return correctLabels;
    }

    public int[] getPredictedLabels() {
        return predictedLabels;
    }

    public int[] getExpectedLabels() {
        return expectedLabels;
    }

    @Override
    public String toString() {
        return "MultiLabelFlowBenchResult{" +
                "correctLabels=" + Arrays.toString(correctLabels) +
                ", predictedLabels=" + Arrays.toString(predictedLabels) +
                ", expectedLabels=" + Arrays.toString(expectedLabels) +
                '}';
    }
}
