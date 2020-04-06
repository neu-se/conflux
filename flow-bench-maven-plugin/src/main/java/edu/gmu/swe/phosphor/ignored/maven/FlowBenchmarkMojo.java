package edu.gmu.swe.phosphor.ignored.maven;

import edu.columbia.cs.psl.phosphor.PhosphorOption;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;
import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResult;
import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import edu.gmu.swe.phosphor.ignored.runtime.TableStat;
import edu.gmu.swe.util.GroupedTable;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.surefire.shade.booter.org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.maven.surefire.shade.booter.org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

import static edu.gmu.swe.phosphor.ignored.maven.PhosphorInstrumentUtil.createPhosphorAgentArgument;
import static edu.gmu.swe.phosphor.ignored.maven.PhosphorInstrumentUtil.getPhosphorJarFile;

/**
 * Runs benchmarks with different Phosphor configurations and reports the results.
 */
@Mojo(name = "benchmark", defaultPhase = LifecyclePhase.INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class FlowBenchmarkMojo extends AbstractMojo {

    /**
     * Name of the directory used to store results from the different configurations
     */
    private static final String REPORT_DIRECTORY = "flow-benchmark-reports";

    /**
     * Name of the file used to store the Phosphor configuration options used for a particular cache directory
     */
    private static final String PHOSPHOR_CACHE_PROPERTIES_FILE = "phosphor-cache-properties";

    /**
     * String argument used to tell "forked" JVMs to wait for a debugger
     */
    private static final String DEBUG_ARG = "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005";

    /**
     * Configuration option name used by Phosphor to specify a cache directory for instrumented files
     */
    private static final String phosphorCacheDirectoryOptionName = PhosphorOption.CACHE_DIR.createOption().getOpt();

    /**
     * True if "forked" JVMs should wait for a debugger
     */
    private static final boolean debugForks = Boolean.getBoolean("phosphor.flow.bench.debug");

    /**
     * Maven build output directory
     */
    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    private File buildDir;

    /**
     * The project being benchmarked
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * The directory containing generated test classes of the project being benchmarked
     */
    @Parameter(defaultValue = "${project.build.testOutputDirectory}", readonly = true)
    private File testClassesDirectory;

    /**
     * The directory containing generated classes of the project being benchmarked
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
    private File classesDirectory;

    /**
     * Phosphor configurations to be benchmarked
     */
    @Parameter(property = "phosphorConfigurations", readonly = true, required = true)
    private List<PhosphorConfig> phosphorConfigurations;

    /**
     * List of paths to any JAR files, other than the JAR file for Phosphor, that need to be added to the bootclasspath
     */
    @Parameter(property = "bootClasspathJars", readonly = true)
    private List<String> bootClasspathJars;

    /**
     * True if execution times should be reported
     */
    @Parameter(property = "reportExecutionTime", readonly = true, defaultValue = "false")
    private boolean reportExecutionTime;

    /**
     * Runs flow benchmarks with different Phosphor configurations and reports the results to standard out.
     *
     * @throws MojoFailureException if benchmarks fail to run
     */
    @Override
    public void execute() throws MojoFailureException {
        validatePhosphorConfigurations();
        try {
            File reportDirectory = new File(buildDir, REPORT_DIRECTORY);
            PhosphorInstrumentUtil.createOrCleanDirectory(reportDirectory);
            List<File> reportFiles = runBenchmarks(reportDirectory);
            printResultsTable(getConfigurationNames(), deserializeReports(reportFiles));
        } catch(InterruptedException | IOException e) {
            throw new MojoFailureException("Failed to benchmark configurations", e);
        }
    }

    /**
     * @return list of names for the Phosphor configurations being benchmarked in the order they are benchmarked
     */
    private List<String> getConfigurationNames() {
        List<String> names = new LinkedList<>();
        for(PhosphorConfig config : phosphorConfigurations) {
            names.add(config.name);
        }
        return names;
    }

    /**
     * Reads flow benchmark reports from the files in the specified list.
     *
     * @param reportFiles list of files contains benchmark reports for the different configurations in the order the
     *                    benchmarks were run
     * @return a list contains the list of benchmark results read from the reportFiles in the order the benchmark lists
     * were run
     * @throws IOException if an I/O error occurs
     */
    private List<? extends List<FlowBenchReport>> deserializeReports(List<File> reportFiles) throws IOException {
        List<List<FlowBenchReport>> reports = new LinkedList<>();
        for(File reportFile : reportFiles) {
            reports.add(FlowBenchReport.readJsonFromFile(reportFile));
        }
        return reports;
    }

    /**
     * Prints a table of benchmark results of the specified type for the various Phosphor configurations.
     *
     * @param configurationNames the names of the configurations that were benchmarked
     * @param reportLists        list of reports for each configuration that was benchmarked
     */
    private void printResultsTable(List<String> configurationNames, List<? extends List<FlowBenchReport>> reportLists) {
        TreeMap<Pair<String, String>, Map<String, FlowBenchReport>> tests = new TreeMap<>();
        Iterator<? extends List<FlowBenchReport>> reportsIt = reportLists.iterator();
        String benchmarkTypeDesc = null;
        for(String name : configurationNames) {
            List<FlowBenchReport> reports = reportsIt.next();
            for(FlowBenchReport report : reports) {
                Pair<String, String> test = new ImmutablePair<>(report.getSimpleClassName(), report.getMethodName());
                if(report.getResult() instanceof FlowBenchResultImpl) {
                    tests.putIfAbsent(test, new HashMap<>());
                    tests.get(test).put(name, report);
                    if(benchmarkTypeDesc == null) {
                        benchmarkTypeDesc = report.getResult().getBenchmarkTypeDesc();
                    }
                }
            }
        }
        if(!tests.isEmpty()) {
            Map<String, Method> tableStatMethods = getTableStatMethods();
            List<String> statsPerConfig = new LinkedList<>();
            if(reportExecutionTime) {
                statsPerConfig.add("Time (ms)");
            }
            statsPerConfig.addAll(tableStatMethods.keySet());
            GroupedTable table = new GroupedTable(benchmarkTypeDesc + " Results")
                    .addGroup("", "Benchmark", "Test");
            for(String name : configurationNames) {
                table.addGroup(name, statsPerConfig.toArray(new String[0]));
            }
            for(Pair<String, String> test : tests.keySet()) {
                Object[][] row = new Object[configurationNames.size() + 1][];
                String testName = test.getLeft();
                if(testName.startsWith("FlowBench")) {
                    testName = testName.substring("FlowBench".length());
                }
                if(testName.endsWith("FlowBench")) {
                    testName = testName.substring(0, testName.length() - "FlowBench".length());
                }
                row[0] = new String[]{testName, test.getRight()};
                Map<String, FlowBenchReport> reports = tests.get(test);
                int i = 1;
                for(String name : configurationNames) {
                    List<String> rowData = new LinkedList<>();
                    if(reports.containsKey(name)) {
                        FlowBenchReport report = reports.get(name);
                        FlowBenchResult result = report.getResult();
                        if(reportExecutionTime) {
                            rowData.add(String.format("%d", report.getTimeElapsed()));
                        }
                        tableStatMethods.forEach((k, v) -> {
                            v.setAccessible(true);
                            try {
                                Object value = v.invoke(result);
                                if(value instanceof Float || value instanceof Double) {
                                    rowData.add(String.format("%.4f", value));
                                } else {
                                    rowData.add(value.toString());
                                }
                            } catch(Exception e) {
                                rowData.add("------");
                            }
                        });
                    } else {
                        if(reportExecutionTime) {
                            rowData.add("Error"); // Time column
                        }
                        tableStatMethods.forEach((k, v) -> rowData.add("Error"));
                    }
                    row[i++] = rowData.toArray();
                }
                table.addRow(row);
            }
            table.printToStream(System.out);
            System.out.println("\n");
        }
    }

    /**
     * Gathers methods annotated with the TableStat annotation from the specified class.
     *
     * @return mapping from TableStat annotation names to the methods they annotate
     */
    private Map<String, Method> getTableStatMethods() {
        Map<String, Method> statMethods = new TreeMap<>();
        for(Class<?> clazz = FlowBenchResultImpl.class; clazz != FlowBenchResult.class; clazz = clazz.getSuperclass()) {
            for(Method method : FlowBenchResultImpl.class.getDeclaredMethods()) {
                if(method.isAnnotationPresent(TableStat.class)) {
                    String statName = method.getAnnotation(TableStat.class).name();
                    statMethods.put(statName, method);
                }
            }
        }
        return statMethods;
    }

    /**
     * Validates phosphorConfigurations to ensure that configurations have valid names and instrumented JVM directories.
     * Canonicalizes the phosphorConfigurations' properties.
     *
     * @throws MojoFailureException if a PhosphorConfig in phosphorConfigurations has an invalid name or instrumentedJVM
     *                              value
     */
    private void validatePhosphorConfigurations() throws MojoFailureException {
        Set<String> names = new HashSet<>();
        for(PhosphorConfig config : phosphorConfigurations) {
            if(config.name == null || config.name.length() == 0) {
                throw new MojoFailureException("Phosphor configurations must have non-null, non-empty names");
            }
            if(config.instrumentedJVM == null || !config.instrumentedJVM.isDirectory()
                    || !new File(config.instrumentedJVM, "bin" + File.separator + "java").isFile()) {
                throw new MojoFailureException("Phosphor configurations must specify an instrumented JVM directory");
            }
            if(!names.add(config.name)) {
                throw new MojoFailureException("Phosphor configurations must unique names: " + config.name);
            }
            config.options = PhosphorInstrumentUtil.canonicalizeProperties(config.options, true);
        }
    }

    /**
     * If the specified configuration's properties contains a non-null, non-empty cache directory property returns a
     * file object created from it.
     * Otherwise returns a default cache directory based on the name of the configuration located in the project
     * build directory.
     *
     * @param phosphorConfiguration configuration whose cache directory is to be returned
     * @return a cache directory for phosphorConfiguration
     */
    private File getCacheDir(PhosphorConfig phosphorConfiguration) {
        String cacheDirProperty = phosphorConfiguration.options.getProperty(phosphorCacheDirectoryOptionName);
        if(cacheDirProperty != null && cacheDirProperty.length() > 0) {
            return new File(cacheDirProperty);
        } else {
            String cacheDirName = phosphorConfiguration.name + "-cache";
            phosphorConfiguration.options.setProperty(phosphorCacheDirectoryOptionName, cacheDirName);
            return new File(buildDir, cacheDirName);
        }
    }

    /**
     * Checked the specified directory to determine whether it was a Phosphor cache directory for a Phosphor agent that
     * used the same properties as the specified desired properties.
     *
     * @param cacheDir          the directory to be checked
     * @param desiredProperties the properties an existing cache must have to be valid
     * @return true if the specified directory contains a phosphor-cache-properties that contains properties matching
     * the specified desired properties
     */
    private boolean isExistingCacheDirectory(File cacheDir, Properties desiredProperties) {
        if(cacheDir.isDirectory()) {
            File propFile = new File(cacheDir, PHOSPHOR_CACHE_PROPERTIES_FILE);
            if(propFile.isFile()) {
                try {
                    Properties existingProperties = new Properties();
                    existingProperties.load(new FileReader(propFile));
                    if(!desiredProperties.equals(existingProperties)) {
                        return false;
                    }
                } catch(IOException e) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Runs flow benchmarks in isolated Phosphor-instrumented JVMs.
     *
     * @param reportDirectory directory to which .json benchmarks reports should be written
     * @return a list of the files to which benchmark results were written
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if a thread interrupts this thread while it is waiting for a benchmark process to finish
     */
    private List<File> runBenchmarks(File reportDirectory) throws IOException, InterruptedException {
        List<File> reportFiles = new LinkedList<>();
        for(PhosphorConfig config : phosphorConfigurations) {
            File cacheDir = getCacheDir(config);
            if(!isExistingCacheDirectory(cacheDir, config.options)) {
                getLog().info("Creating new Phosphor cache directory: " + cacheDir);
                PhosphorInstrumentUtil.createOrCleanDirectory(cacheDir);
                // Add phosphor-cache-properties file to directory
                File propsFile = new File(cacheDir, PHOSPHOR_CACHE_PROPERTIES_FILE);
                config.options.store(new FileWriter(propsFile), null);
            } else {
                getLog().info("Using existing Phosphor cache directory: " + cacheDir);
            }
            File reportFile = new File(reportDirectory, config.name + ".json");
            getLog().info("Running flow benchmarks for Phosphor configuration: " + config.name);
            forkBenchmarkRunner(config.instrumentedJVM, config.options, reportFile);
            reportFiles.add(reportFile);
        }
        return reportFiles;
    }

    /**
     * Runs benchmarks for a particular Phosphor configuration is a separate process.
     *
     * @param instrumentedJVM directory of the instrumented JVM that should be used to run the benchmark
     * @param properties      canonicalized properties that specify the Phosphor configuration options that should be used
     *                        in the fork
     * @param reportFile      file to which created process should write its json report
     * @throws InterruptedException if a thread interrupts this thread while it is waiting for the benchmark process to finish
     * @throws IOException          if an I/O error occurs
     */
    private void forkBenchmarkRunner(File instrumentedJVM, Properties properties, File reportFile) throws InterruptedException, IOException {
        String javaCommand = new File(instrumentedJVM, "bin" + File.separator + "java").getAbsolutePath();
        List<String> commands = new LinkedList<>();
        commands.add(javaCommand);
        commands.add("-cp");
        commands.add(String.join(File.pathSeparator, getClassPath()));
        String phosphorJarPath = getPhosphorJarFile().getAbsolutePath();
        StringBuilder bootClassPathBuilder = new StringBuilder("-Xbootclasspath/p:").append(phosphorJarPath);
        if(bootClasspathJars != null && !bootClasspathJars.isEmpty()) {
            for(String s : bootClasspathJars) {
                bootClassPathBuilder.append(':').append(s);
            }
        }
        commands.add(bootClassPathBuilder.toString());
        commands.add("-javaagent:" + phosphorJarPath + createPhosphorAgentArgument(properties));
        if(debugForks) {
            commands.add(DEBUG_ARG);
        }
        commands.add(ForkedFlowBenchmarkRunner.class.getName());
        commands.add(classesDirectory.getAbsolutePath());
        commands.add(reportFile.getAbsolutePath());
        Process process = new ProcessBuilder(commands).inheritIO().start();
        if(process.waitFor() != 0) {
            getLog().error("Error in flow benchmark process");
        }
    }

    /**
     * Returns a list of class path dependencies for the project being benchmarked
     *
     * @return a list of class path dependencies for the project being benchmarked
     */
    private List<String> getClassPath() {
        List<String> classpath = new ArrayList<>();
        classpath.add(testClassesDirectory.getAbsolutePath());
        classpath.add(classesDirectory.getAbsolutePath());
        @SuppressWarnings("unchecked")
        Iterable<Artifact> artifacts = (Iterable<Artifact>) project.getArtifacts();
        for(Artifact artifact : artifacts) {
            if(artifact.getArtifactHandler().isAddedToClasspath()) {
                File file = artifact.getFile();
                if(file != null) {
                    classpath.add(file.getAbsolutePath());
                }
            }
        }
        classpath.add(System.getProperty("java.class.path"));
        return classpath;
    }
}
