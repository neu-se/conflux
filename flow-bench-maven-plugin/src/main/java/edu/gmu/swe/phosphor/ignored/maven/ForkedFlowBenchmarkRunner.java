package edu.gmu.swe.phosphor.ignored.maven;

import edu.columbia.cs.psl.phosphor.TaintUtils;
import edu.gmu.swe.phosphor.*;
import edu.gmu.swe.phosphor.ignored.runtime.BinaryFlowBenchResult;
import edu.gmu.swe.phosphor.ignored.runtime.ErrorFlowBenchResult;
import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResult;
import edu.gmu.swe.phosphor.ignored.runtime.MultiLabelFlowBenchResult;
import org.apache.maven.plugin.surefire.util.DirectoryScanner;
import org.apache.maven.surefire.testset.TestListResolver;
import org.apache.maven.surefire.util.DefaultScanResult;
import org.apache.maven.surefire.util.TestsToRun;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ForkedFlowBenchmarkRunner {

    public static void main(String[] args) {
        File benchmarkOutputDir = new File(args[0]);
        File reportFile = new File(args[1]);
        List<FlowBenchReport> reports = new LinkedList<>();
        List<String> testErrors = new LinkedList<>();
        printHeader();
        for(Class<?> clazz : scanForBenchmarks(benchmarkOutputDir)) {
            if(!Modifier.isAbstract(clazz.getModifiers()) && !clazz.isInterface()) {
                runBenchmarkClass(clazz, reports, testErrors);
            }
        }
        printFooter(reports.size(), testErrors);
        reports.sort(new FlowBenchReportComparator());
        FlowBenchReport.writeJsonToFile(reports, reportFile);
    }

    private static void runBenchmarkClass(Class<?> benchClass, List<FlowBenchReport> allReports, List<String> allTestErrors) {
        List<Method> tests = gatherTests(benchClass);
        if(!tests.isEmpty()) {
            List<FlowBenchReport> reports = new LinkedList<>();
            List<String[]> errorMessages = new LinkedList<>();
            LinkedList<Throwable> errors = new LinkedList<>();
            System.out.println("Running " + benchClass.getName());
            Instant start = Instant.now();
            Object receiver;
            try {
                receiver = benchClass.newInstance();
                for(Method test : tests) {
                    for(FlowBenchReport report : runTest(receiver, test, errors)) {
                        reports.add(report);
                        if(report.getResult() instanceof ErrorFlowBenchResult) {
                            long timeElapsed = Duration.between(start, Instant.now()).toMillis();
                            errorMessages.add(getErrorMessages(test.getName(), benchClass.getName(),
                                    benchClass.getSimpleName(), timeElapsed, errors.getLast()));
                        }
                    }
                }
            } catch (InstantiationException | IllegalAccessException e) {
                Throwable t = new Exception("Benchmark class should have exactly one public zero-argument constructor");
                long timeElapsed = Duration.between(start, Instant.now()).toMillis();
                errorMessages.add(getErrorMessages("initializationError", benchClass.getName(),
                        benchClass.getSimpleName(), timeElapsed, t));
                errors.add(t);
                reports.add(new FlowBenchReport("initializationError", benchClass.getName(), timeElapsed,
                        new ErrorFlowBenchResult()));
            }
            long timeElapsed = Duration.between(start, Instant.now()).toMillis();
            System.out.printf("Tests run: %d, Errors: %d, Time elapsed: %.3f sec\n", reports.size(), errors.size(),
                    timeElapsed * 0.001);
            for(int i = 0; i < errorMessages.size(); i++) {
                System.out.println(errorMessages.get(i)[0]);
                errors.get(i).printStackTrace();
                allTestErrors.add(errorMessages.get(i)[1]);
            }
            allReports.addAll(reports);
        }
    }

    private static List<Method> gatherTests(Class<?> benchClass) {
        List<Method> tests = new LinkedList<>();
        for(Method method : benchClass.getMethods()) {
            if(!method.getName().endsWith(TaintUtils.METHOD_SUFFIX) && method.isAnnotationPresent(FlowBench.class)) {
                tests.add(method);
            }
        }
        return tests;
    }

    private static void validateTestSignature(Method test) throws Exception {
        if(Modifier.isStatic(test.getModifiers())) {
            throw new Exception("Flow benchmark test method should not be static");
        }
        if(!Void.TYPE.equals(test.getReturnType())) {
            throw new Exception("Flow benchmark test method should have void return type.");
        }
        boolean validParams = false;
        if(test.getParameterCount() == 1 || test.getParameterCount() == 2) {
            Class<?> firstParam = test.getParameterTypes()[0];
            if(firstParam.equals(BinaryFlowBenchResult.class) || firstParam.equals(MultiLabelFlowBenchResult.class)) {
                if(test.getParameterCount() == 1 || test.getParameterTypes()[1].equals(TaintedPortionPolicy.class)) {
                    validParams = true;
                }
            }
        }
        if(!validParams) {
            throw new Exception("Flow benchmark test method should have one parameter of type BinaryFlowBenchResult " +
                    "or MultiLabelFlowBenchResult and may optionally have a second  parameter of type TaintedPortionPolicy");
        }
    }

    private static List<FlowBenchReport> runTest(Object receiver, Method test, List<Throwable> errors) {
        List<FlowBenchReport> reports = new LinkedList<>();
        try {
            validateTestSignature(test);
            if(test.getParameterCount() == 2) {
                for(TaintedPortionPolicy portion : TaintedPortionPolicy.values()) {
                    FlowBenchResult result = (FlowBenchResult) test.getParameterTypes()[0].newInstance();
                    Instant start = Instant.now();
                    test.invoke(receiver, result, portion);
                    Instant finish = Instant.now();
                    long timeElapsed = Duration.between(start, finish).toMillis();
                    reports.add(new FlowBenchReport(test, portion, timeElapsed, result));
                }
            } else {
                FlowBenchResult result = (FlowBenchResult) test.getParameterTypes()[0].newInstance();
                Instant start = Instant.now();
                test.invoke(receiver, result);
                Instant finish = Instant.now();
                long timeElapsed = Duration.between(start, finish).toMillis();
                reports.add(new FlowBenchReport(test, timeElapsed, result));
            }
        } catch(Throwable t) {
            errors.add(t);
            reports.add(new FlowBenchReport(test, -1, new ErrorFlowBenchResult()));
        }
        return reports;
    }

    private static TestsToRun scanForBenchmarks(File benchmarkOutputDir) {
        DirectoryScanner scanner = new DirectoryScanner(benchmarkOutputDir,
                new TestListResolver(Arrays.asList("**/FlowBench*.java", "**/*FlowBench.java")));
        DefaultScanResult result = scanner.scan();
        return result.applyFilter(new IdentityFilter(), ClassLoader.getSystemClassLoader());
    }

    private static void printHeader() {
        System.out.println("-------------------------------------------------------");
        System.out.println("            RUNNING PHOSPHOR FLOW BENCHMARKS           ");
        System.out.println("-------------------------------------------------------");
    }

    private static String[] getErrorMessages(String testName, String benchClassName, String benchClassSimpleName, long elapsedTime, Throwable error) {
        return new String[]{
                String.format("%s(%s) Time elapsed: %.3f sec <<< ERROR!", testName, benchClassName, elapsedTime * 0.001),
                String.format("%s.%s >>> %s", benchClassSimpleName, testName, error.toString())
        };
    }

    private static void printFooter(int testsRun, List<String> testErrors) {
        System.out.println("\nResults:\n");
        if(!testErrors.isEmpty()) {
            System.out.println("Tests errors:\n");
            for(String errorMessage : testErrors) {
                System.out.printf("\t%s\n", errorMessage);
            }
            System.out.println();
        }
        System.out.printf("Tests run: %d, Errors: %d\n\n", testsRun, testErrors.size());
    }
}
