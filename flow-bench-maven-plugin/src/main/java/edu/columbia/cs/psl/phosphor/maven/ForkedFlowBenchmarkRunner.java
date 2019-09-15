package edu.columbia.cs.psl.phosphor.maven;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.gmu.swe.phosphor.IdentityFilter;
import org.apache.maven.plugin.surefire.util.DirectoryScanner;
import org.apache.maven.surefire.testset.TestListResolver;
import org.apache.maven.surefire.util.DefaultScanResult;
import org.apache.maven.surefire.util.TestsToRun;

import java.io.File;
import java.util.Arrays;

public class ForkedFlowBenchmarkRunner {

    public static void main(String[] args) {
        try {
            File benchmarkOutputDir = new File(args[0]);
            File reportFile = new File(args[1]);
            TestsToRun benchmarks = scanForBenchmarks(benchmarkOutputDir);
            System.out.println(benchmarks);
            System.out.println(reportFile);
            System.out.println("Multi-tainting?: " + Configuration.MULTI_TAINTING);
            System.out.println("Implicit?: " + Configuration.IMPLICIT_TRACKING);
        } catch(Throwable t) {
            t.printStackTrace();
            // TODO report error to report file
        }

    }

    private static TestsToRun scanForBenchmarks(File benchmarkOutputDir) {
        DirectoryScanner scanner = new DirectoryScanner( benchmarkOutputDir,
                new TestListResolver(Arrays.asList("**/FlowBench*.java", "**/*FlowBench.java")));
        DefaultScanResult result = scanner.scan();
        return result.applyFilter(new IdentityFilter(), ClassLoader.getSystemClassLoader());
    }
}
