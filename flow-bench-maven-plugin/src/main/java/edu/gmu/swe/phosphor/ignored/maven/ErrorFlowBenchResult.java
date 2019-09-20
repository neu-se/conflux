package edu.gmu.swe.phosphor.ignored.maven;

import java.util.Set;

public class ErrorFlowBenchResult extends FlowBenchResult {

    @Override
    public String getBenchmarkTypeDesc() {
        return "Benchmark Error";
    }

    @Override
    public void check(Set<?> expected, Set<?> actual) {

    }
}
