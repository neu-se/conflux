package edu.gmu.swe.phosphor.ignored.maven;

import edu.columbia.cs.psl.phosphor.TaintUtils;
import edu.columbia.cs.psl.phosphor.struct.PowerSetTree;
import edu.gmu.swe.phosphor.FlowBench;
import edu.gmu.swe.phosphor.FlowBenchReportComparator;
import edu.gmu.swe.phosphor.IdentityFilter;
import edu.gmu.swe.phosphor.ignored.runtime.ErrorFlowBenchResult;
import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResult;
import org.apache.maven.plugin.surefire.util.DirectoryScanner;
import org.apache.maven.surefire.api.testset.TestListResolver;
import org.apache.maven.surefire.api.util.TestsToRun;

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
        int[] numbersOfEntities = new int[args.length - 2];
        for(int i = 2; i < args.length; i++) {
            numbersOfEntities[i - 2] = Integer.parseInt(args[i]);
        }
        if(numbersOfEntities.length == 0) {
            throw new IllegalArgumentException("At least one input length must be specified");
        }
        List<FlowBenchReport> reports = new LinkedList<>();
        List<String> testErrors = new LinkedList<>();
        printHeader();
        for(Class<?> clazz : scanForBenchmarks(benchmarkOutputDir)) {
            if(!Modifier.isAbstract(clazz.getModifiers()) && !clazz.isInterface()) {
                runBenchmarkClass(clazz, reports, testErrors, numbersOfEntities);
            }
        }
        printFooter(reports.size(), testErrors);
        reports.sort(new FlowBenchReportComparator());
        FlowBenchReport.writeJsonToFile(reports, reportFile);
    }

    private static void runBenchmarkClass(Class<?> benchClass, List<FlowBenchReport> allReports,
                                          List<String> allTestErrors, int[] numbersOfEntities) {
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
                    for(FlowBenchReport report : runTest(receiver, test, errors, numbersOfEntities)) {
                        reports.add(report);
                        if(report.getResult() instanceof ErrorFlowBenchResult) {
                            long timeElapsed = Duration.between(start, Instant.now()).toMillis();
                            errorMessages.add(getErrorMessages(test.getName(), benchClass.getName(),
                                    benchClass.getSimpleName(), timeElapsed, errors.getLast()));
                        }
                    }
                }
            } catch(InstantiationException | IllegalAccessException e) {
                Throwable t = new Exception("Benchmark class should have exactly one public zero-argument constructor");
                long timeElapsed = Duration.between(start, Instant.now()).toMillis();
                errorMessages.add(getErrorMessages(benchClass.getName(), "initializationError",
                        benchClass.getSimpleName(), timeElapsed, t));
                errors.add(t);
                reports.add(new FlowBenchReport(benchClass.getName(), "initializationError", timeElapsed,
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
        } else if(!Void.TYPE.equals(test.getReturnType())) {
            throw new Exception("Flow benchmark test method should have void return type");
        } else if(test.getParameterCount() != 2
                || !FlowBenchResult.class.isAssignableFrom(test.getParameterTypes()[0])
                || !int.class.equals(test.getParameterTypes()[1])) {
            throw new Exception("Flow benchmark test method should have two parameters: one of some subclass of " +
                    "FlowBenchResult and one of type int");
        }
    }

    private static List<FlowBenchReport> runTest(Object receiver, Method test, List<Throwable> errors, int[] numbersOfEntities) {
        List<FlowBenchReport> reports = new LinkedList<>();
        try {
            validateTestSignature(test);
            FlowBenchResult result = (FlowBenchResult) test.getParameterTypes()[0].newInstance();
            long timeElapsed = 0;
            for(int numberOfEntities : numbersOfEntities) {
                PowerSetTree.getInstance().reset();
                result.startingRun(numberOfEntities);
                Instant start = Instant.now();
                test.invoke(receiver, result, numberOfEntities);
                Instant finish = Instant.now();
                timeElapsed += Duration.between(start, finish).toMillis();
            }
            reports.add(new FlowBenchReport(test, timeElapsed, result));
        } catch(Throwable t) {
            errors.add(t);
            reports.add(new FlowBenchReport(test, -1, new ErrorFlowBenchResult()));
        }
        return reports;
    }

    private static TestsToRun scanForBenchmarks(File benchmarkOutputDir) {
        DirectoryScanner scanner = new DirectoryScanner(benchmarkOutputDir,
                new TestListResolver(Arrays.asList("**/FlowBench*.java", "**/*FlowBench.java")));
        return scanner.scan().applyFilter(new IdentityFilter(), ClassLoader.getSystemClassLoader());
    }

    private static void printHeader() {
        System.out.println("-------------------------------------------------------");
        System.out.println("            RUNNING PHOSPHOR FLOW BENCHMARKS           ");
        System.out.println("-------------------------------------------------------");
    }

    private static String[] getErrorMessages(String testName, String benchClassName, String benchClassSimpleName,
                                             long elapsedTime, Throwable error) {
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
