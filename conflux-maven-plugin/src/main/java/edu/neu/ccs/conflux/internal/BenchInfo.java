package edu.neu.ccs.conflux.internal;

import java.lang.reflect.Method;

public final class BenchInfo extends TestInfo {
    private final String group;
    private final String implementation;
    private final String project;

    BenchInfo(Class<?> testClass, Method testMethod) {
        super(testClass, testMethod, FlowBench.class);
        if (!testMethod.isAnnotationPresent(FlowBench.class)) {
            throw new IllegalArgumentException();
        }
        FlowBench annotation = testMethod.getAnnotation(FlowBench.class);
        this.group = annotation.group();
        this.implementation = annotation.implementation();
        this.project = annotation.project();
    }

    public String getGroup() {
        return group;
    }

    public String getImplementation() {
        return implementation;
    }

    public String getProject() {
        return project;
    }
}
