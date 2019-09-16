package edu.gmu.swe.phosphor;

public class ErrorFlowBenchResult implements FlowBenchResult {

    private final Throwable error;

    public ErrorFlowBenchResult(Throwable error) {
        this.error = error;
    }

    public Throwable getError() {
        return error;
    }

    @Override
    public String toString() {
        return "ErrorFlowBenchResult{" +
                "error=" + error +
                '}';
    }
}
