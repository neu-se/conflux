package edu.gmu.swe.phosphor;

public class BinaryFlowBenchResult implements FlowBenchResult {

    private final int truePositives;
    private final int trueNegatives;
    private final int falsePositives;
    private final int falseNegatives;

    public BinaryFlowBenchResult(int truePositives, int trueNegatives, int falsePositives, int falseNegatives) {
        this.truePositives = truePositives;
        this.trueNegatives = trueNegatives;
        this.falsePositives = falsePositives;
        this.falseNegatives = falseNegatives;
    }

    public int getTruePositives() {
        return truePositives;
    }

    public int getTrueNegatives() {
        return trueNegatives;
    }

    public int getFalsePositives() {
        return falsePositives;
    }

    public int getFalseNegatives() {
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
}
