package edu.neu.ccs.conflux.internal;

import edu.columbia.cs.psl.phosphor.TaintUtils;
import edu.columbia.cs.psl.phosphor.struct.PowerSetTree;
import edu.neu.ccs.conflux.IdentityFilter;
import edu.neu.ccs.conflux.internal.runtime.BenchTaintTagChecker;
import edu.neu.ccs.conflux.internal.runtime.StudyTaintTagChecker;
import org.apache.maven.plugin.surefire.util.DirectoryScanner;
import org.apache.maven.surefire.api.testset.TestListResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class FlowEvaluationRunner {

    public static void main(String[] args) {
        File testClassesDirectory = new File(args[0]);
        File reportFile = new File(args[1]);
        int[] numbersOfEntities = new int[args.length - 2];
        if (numbersOfEntities.length == 0) {
            throw new IllegalArgumentException("At least one input length must be specified");
        }
        for (int i = 2; i < args.length; i++) {
            numbersOfEntities[i - 2] = Integer.parseInt(args[i]);
        }
        FlowReport report = new FlowReport();
        List<TestError> errors = new ArrayList<>();
        printHeader();
        int testsRun = 0;
        for (TestType type : TestType.values()) {
            for (Class<?> clazz : type.scanForTestClasses(testClassesDirectory)) {
                if (!Modifier.isAbstract(clazz.getModifiers()) && !clazz.isInterface()) {
                    testsRun += runTestClass(type, clazz, report, errors, numbersOfEntities);
                }
            }
        }
        printFooter(errors, testsRun);
        try {
            report.writeToFile(reportFile);
        } catch (FileNotFoundException e) {
            System.err.println("Failed to write to report file: " + reportFile);
            System.exit(-1);
        }
        if (!errors.isEmpty()) {
            System.exit(-1);
        } else {
            System.exit(0);
        }
    }

    private static boolean validateTestClass(Class<?> testClass) {
        try {
            return Modifier.isPublic(testClass.getConstructor().getModifiers());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static int runTestClass(TestType type, Class<?> testClass, FlowReport report,
                                    List<TestError> errors, int[] numbersOfEntities) {
        List<Method> tests = type.gatherTestMethods(testClass);
        if (!tests.isEmpty()) {
            System.out.println("Running " + testClass.getName());
            Instant start = Instant.now();
            int testsRun;
            List<TestError> newErrors = new LinkedList<>();
            if (validateTestClass(testClass)) {
                for (Method testMethod : tests) {
                    Throwable error = type.runTest(testClass, testMethod, numbersOfEntities, report);
                    if (error != null) {
                        newErrors.add(new TestError(testClass, testMethod, start, error));
                    }
                }
                testsRun = tests.size();
            } else {
                TestError error = new TestError(testClass, start, new Exception(type.getAnnotationType().getSimpleName()
                        + " class should have a public zero-argument constructor"));
                newErrors.add(error);
                testsRun = 1;
            }
            long timeElapsed = Duration.between(start, Instant.now()).toMillis();
            System.out.printf("Tests run: %d, Errors: %d, Time elapsed: %.3f sec%n", testsRun,
                    newErrors.size(),
                    timeElapsed * 0.001);
            for (TestError error : newErrors) {
                error.printMessage();
            }
            errors.addAll(newErrors);
            return testsRun;
        }
        return 0;
    }

    private static void printHeader() {
        System.out.println("-------------------------------------------------------");
        System.out.println("             RUNNING TAINT TRACKING TESTS              ");
        System.out.println("-------------------------------------------------------");
    }

    private static void printFooter(List<TestError> errors, int testsRun) {
        System.out.printf("%nResults:%n%n");
        if (!errors.isEmpty()) {
            System.out.printf("Test errors:%n");
            System.out.println();
            for (TestError error : errors) {
                System.out.printf("\t%s", error.getSummary());
                System.out.println();
            }
            System.out.println();
        }
        System.out.printf("Tests run: %d, Errors: %d%n%n", testsRun, errors.size());
    }

    public enum TestType {
        BENCH(FlowBench.class, BenchTaintTagChecker.class, int.class) {
            @Override
            public Throwable runTest(Class<?> testClass, Method testMethod, int[] numbersOfEntities,
                                     FlowReport report) {
                try {
                    validateTestMethod(testMethod);
                    Map<Integer, BenchRunResult> result = new HashMap<>();
                    for (int numberOfEntities : numbersOfEntities) {
                        Object receiver = testClass.newInstance();
                        BenchTaintTagChecker checker = new BenchTaintTagChecker();
                        PowerSetTree.getInstance().reset();
                        testMethod.invoke(receiver, checker, numberOfEntities);
                        result.put(numberOfEntities, checker.toRunResult());
                    }
                    report.addBenchReport(testClass, testMethod, result);
                    return null;
                } catch (Throwable t) {
                    return t;
                }
            }
        },
        STUDY(FlowStudy.class, StudyTaintTagChecker.class) {
            @Override
            public Throwable runTest(Class<?> testClass, Method testMethod, int[] numbersOfEntities,
                                     FlowReport report) {
                try {
                    validateTestMethod(testMethod);
                    Object receiver = testClass.newInstance();
                    StudyTaintTagChecker checker = new StudyTaintTagChecker();
                    PowerSetTree.getInstance().reset();
                    try {
                        testMethod.invoke(receiver, checker);
                    } catch (InvocationTargetException e) {
                        return e.getCause();
                    }
                    if (checker.inputRecorded()) {
                        report.addStudyReport(testClass, testMethod, checker.toRunResult());
                    } else {
                        return new IllegalStateException("FlowStudy must record the input value used in the study");
                    }
                    return null;
                } catch (Throwable t) {
                    return t;
                }
            }
        };
        private final Class<?>[] expectedParameterTypes;
        private final Class<? extends Annotation> annotationType;

        TestType(Class<? extends Annotation> annotationType, Class<?>... expectedParameterTypes) {
            this.expectedParameterTypes = expectedParameterTypes;
            this.annotationType = annotationType;
        }

        public Class<? extends Annotation> getAnnotationType() {
            return annotationType;
        }

        public Iterable<Class<?>> scanForTestClasses(File testClassesDirectory) {
            List<String> includedPatterns = Arrays.asList(String.format("**/%s*.java", annotationType.getSimpleName()),
                    String.format("**/*%s.java", annotationType.getSimpleName())
            );
            return new DirectoryScanner(testClassesDirectory, new TestListResolver(includedPatterns))
                    .scan()
                    .applyFilter(new IdentityFilter(), ClassLoader.getSystemClassLoader());
        }

        public List<Method> gatherTestMethods(Class<?> testClass) {
            List<Method> tests = new LinkedList<>();
            for (Method method : testClass.getMethods()) {
                if (!method.getName().endsWith(TaintUtils.METHOD_SUFFIX)
                        && method.isAnnotationPresent(annotationType)) {
                    tests.add(method);
                }
            }
            return tests;
        }

        public void validateTestMethod(Method test) throws Exception {
            if (Modifier.isStatic(test.getModifiers()) && !Void.TYPE.equals(test.getReturnType())) {
                throw new Exception(annotationType.getSimpleName() + " method should not be static and must be void");
            }
            if (!Arrays.equals(test.getParameterTypes(), expectedParameterTypes)) {
                String[] types = new String[expectedParameterTypes.length];
                for (int i = 0; i < expectedParameterTypes.length; i++) {
                    types[i] = expectedParameterTypes[i].getSimpleName();
                }
                String expected = String.join(", ", types);
                throw new Exception(annotationType.getSimpleName() + " method should have parameters of types: " +
                        expected);
            }
        }

        public abstract Throwable runTest(Class<?> testClass, Method testMethod, int[] numbersOfEntities,
                                          FlowReport report);
    }

    private static final class TestError {

        private final String message;
        private final Throwable error;
        private final String summary;

        private TestError(Class<?> testClass, String testMethodName, Instant start, Throwable error) {
            long elapsedTime = Duration.between(start, Instant.now()).toMillis();
            this.error = error;
            this.message = String.format("%s(%s) Time elapsed: %.3f sec <<< ERROR!", testMethodName,
                    testClass.getName(), elapsedTime * 0.001);
            this.summary = String.format("%s.%s >>> %s", testClass.getSimpleName(), testMethodName, error.toString());
        }

        private TestError(Class<?> testClass, Method testMethod, Instant start, Throwable error) {
            this(testClass, testMethod.getName(), start, error);
        }

        private TestError(Class<?> testClass, Instant start, Throwable error) {
            this(testClass, "initializationError", start, error);
        }

        private void printMessage() {
            System.out.println(message);
            error.printStackTrace();
        }

        public String getSummary() {
            return summary;
        }
    }
}
