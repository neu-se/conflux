package edu.columbia.cs.psl.phosphor.maven;

import java.lang.reflect.Method;

public class FlowBenchReport {
    private final String className;
    private final String methodName;
    private final long timeElapsed;
    private final FlowBenchResult result;

    public FlowBenchReport(String className, String methodName, long timeElapsed, FlowBenchResult result) {
        this.className = className;
        this.methodName = methodName;
        this.timeElapsed = timeElapsed;
        this.result = result;
    }

    public FlowBenchReport(Method benchMethod, long timeElapsed, FlowBenchResult result) {
        this(benchMethod.getDeclaringClass().getName(), benchMethod.getName(), timeElapsed, result);
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public long getTimeElapsed() {
        return timeElapsed;
    }

    public FlowBenchResult getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "FlowBenchReport{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", timeElapsed=" + timeElapsed +
                ", result=" + result +
                '}';
    }
}

