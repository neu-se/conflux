package edu.neu.ccs.conflux.internal;

import java.lang.reflect.Method;

public final class StudyInfo extends TestInfo {

    private final String project;
    private final String issue;

    StudyInfo(Class<?> testClass, Method testMethod) {
        super(testClass, testMethod, FlowStudy.class);
        if (!testMethod.isAnnotationPresent(FlowStudy.class)) {
            throw new IllegalArgumentException();
        }
        FlowStudy annotation = testMethod.getAnnotation(FlowStudy.class);
        this.project = annotation.project();
        this.issue = annotation.issue();
    }

    public String getProject() {
        return project;
    }

    public String getIssue() {
        return issue;
    }
}
