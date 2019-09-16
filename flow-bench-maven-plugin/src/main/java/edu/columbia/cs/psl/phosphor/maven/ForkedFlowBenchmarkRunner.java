package edu.columbia.cs.psl.phosphor.maven;

import edu.gmu.swe.phosphor.*;
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
        try {
            File benchmarkOutputDir = new File(args[0]);
            File reportFile = new File(args[1]);
            List<FlowBenchReport> reports = new LinkedList<>();
            for(Class<?> clazz : scanForBenchmarks(benchmarkOutputDir)) {
                // TODO Print what's running
                reports.addAll(runTests(clazz));
            }
            for(FlowBenchReport report : reports) {
                System.out.println(report);
            }
            // TODO serialize reports
        } catch(Throwable t) {
            t.printStackTrace();
            // TODO report error to report file
        }
    }

    private static List<FlowBenchReport> runTests(Class<?> testClass) {
        Object receiver;
        try {
            receiver = testClass.newInstance();
        } catch(IllegalAccessException | InstantiationException e) {
            receiver = null;
        }
        List<FlowBenchReport> reports = new LinkedList<>();
        for(Method method : testClass.getDeclaredMethods()) {
            if(method.isAnnotationPresent(FlowBench.class)) {
                // Check to see if return type if FlowBenchResult
                if(FlowBenchResult.class.isAssignableFrom(method.getReturnType()) &&
                        method.getParameterCount() == 0) {
                    reports.add(runTest(receiver, method));
                } else {
                    reports.add(new FlowBenchReport(method, -1, new ErrorFlowBenchResult(
                            new IllegalStateException("Invalid signature for FlowBench annotated method. Method must take " +
                                    "no arguments and return an instance of FlowBenchResult"))));
                }
            }
        }
        return reports;
    }

    private static FlowBenchReport runTest(Object receiver, Method method) {
        if(!Modifier.isStatic(method.getModifiers()) && receiver == null) {
            return new FlowBenchReport(method, -1, new ErrorFlowBenchResult(
                    new InstantiationException("Unable to create benchmark class instance with zero-arg constructor")));
        }
        try {
            Instant start = Instant.now();
            FlowBenchResult result = (FlowBenchResult)method.invoke(receiver);
            Instant finish = Instant.now();
            long timeElapsed = Duration.between(start, finish).toMillis();
            return new FlowBenchReport(method, timeElapsed, result);
        } catch(Throwable t) {
            return new FlowBenchReport(method, -1, new ErrorFlowBenchResult(t));
        }
    }

    private static TestsToRun scanForBenchmarks(File benchmarkOutputDir) {
        DirectoryScanner scanner = new DirectoryScanner( benchmarkOutputDir,
                new TestListResolver(Arrays.asList("**/FlowBench*.java", "**/*FlowBench.java")));
        DefaultScanResult result = scanner.scan();
        return result.applyFilter(new IdentityFilter(), ClassLoader.getSystemClassLoader());
    }
}
