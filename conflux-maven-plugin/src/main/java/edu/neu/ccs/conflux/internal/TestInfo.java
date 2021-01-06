package edu.neu.ccs.conflux.internal;

import java.lang.reflect.Method;

abstract class TestInfo {
    private final String className;
    private final String shortenedClassName;
    private final String methodName;

    TestInfo(Class<?> testClass, Method testMethod, Class<?> annotationType) {
        this.className = testClass.getName();
        this.shortenedClassName = shortenName(testClass, annotationType);
        this.methodName = testMethod.getName();
    }

    public String getClassName() {
        return className;
    }

    public String getShortenedClassName() {
        return shortenedClassName;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof TestInfo)) {
            return false;
        }
        TestInfo testInfo = (TestInfo) o;
        return className.equals(testInfo.className) && methodName.equals(testInfo.methodName);
    }

    @Override
    public int hashCode() {
        int result = className.hashCode();
        result = 31 * result + methodName.hashCode();
        return result;
    }

    private static String shortenName(Class<?> clazz, Class<?> annotationType) {
        String pattern = annotationType.getSimpleName();
        String result = clazz.getSimpleName();
        if (result.startsWith(pattern)) {
            result = result.substring(pattern.length());
        }
        if (result.endsWith(pattern)) {
            result = result.substring(0, result.length() - pattern.length());
        }
        return result;
    }
}
