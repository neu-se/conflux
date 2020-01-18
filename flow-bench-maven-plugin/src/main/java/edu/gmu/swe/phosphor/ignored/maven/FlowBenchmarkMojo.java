package edu.gmu.swe.phosphor.ignored.maven;

import edu.columbia.cs.psl.phosphor.PhosphorOption;
import edu.gmu.swe.phosphor.ignored.runtime.BinaryFlowBenchResult;
import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResult;
import edu.gmu.swe.phosphor.ignored.runtime.MultiLabelFlowBenchResult;
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
import org.apache.maven.surefire.shade.common.org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;

import static edu.gmu.swe.phosphor.ignored.maven.PhosphorInstrumentingMojo.PROPERTIES_FILE_NAME;

/**
 * Runs benchmarks with different Phosphor configurations and reports the results.
 */
@SuppressWarnings("unused")
@Mojo(name = "benchmark", defaultPhase = LifecyclePhase.INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class FlowBenchmarkMojo extends AbstractMojo {

    /**
     * Option name used by Phosphor to specify a cache directory for instrumented files
     */
    private static final String phosphorCacheDirectoryOptionName = PhosphorOption.CACHE_DIR.createOption().getOpt();

    /**
     * Name of the directory used to store results from the different configurations
     */
    private static final String REPORT_DIRECTORY = "flow-benchmark-reports";

    /**
     * Name of the file used to store the Phosphor arguments used for a particular cache directory
     */
    private static final String PHOSPHOR_ARGS_CACHE_FILE = "phosphor-cache-args";

    /**
     * String argument used to tell "forked" JVMs to wait for a debugger
     */
    private static final String DEBUG_ARG = "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005";

    /**
     * True if "forked" JVMs should wait for a debugger
     */
    private static final boolean debugForks = Boolean.getBoolean("phosphor.flow.bench.debug");

    /**
     * Version of Phosphor dependency
     */
    @Parameter(property = "phosphorVersion", readonly = true, required = true)
    private String phosphorVersion;


    /**
     * Maven build output directory
     */
    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    private File buildDir;

    /**
     * Directory of the build system's local repository
     */
    @Parameter(defaultValue = "${settings.localRepository}", readonly = true)
    private File localRepository;

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
     * Runs flow benchmarks with different Phosphor configurations and reports the results to standard out.
     *
     * @throws MojoFailureException if benchmarks fail to run
     */
    @Override
    public void execute() throws MojoFailureException {
        String phosphorJarPath = localRepository.getAbsolutePath() + "/edu/gmu/swe/phosphor/Phosphor/" +
                phosphorVersion + "/Phosphor-" + phosphorVersion + ".jar";
        if(!new File(phosphorJarPath).isFile()) {
            throw new MojoFailureException("Failed to find Phosphor jar: " + phosphorJarPath);
        } else {
            getLog().info("Using Phosphor version: " + phosphorVersion);
        }
        validatePhosphorConfigurations();
        try {
            File reportDirectory = new File(buildDir, REPORT_DIRECTORY);
            createOrCleanDirectory(reportDirectory);
            List<File> reportFiles = runBenchmarks(reportDirectory, phosphorJarPath);
            printResultsTable(getConfigurationNames(), deserializeReports(reportFiles), BinaryFlowBenchResult.class);
            printResultsTable(getConfigurationNames(), deserializeReports(reportFiles), MultiLabelFlowBenchResult.class);
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
     * @param resultType         type of results that should be reported
     */
    private void printResultsTable(List<String> configurationNames, List<? extends List<FlowBenchReport>> reportLists,
                                   Class<? extends FlowBenchResult> resultType) {
        TreeMap<Pair<String, String>, Map<String, FlowBenchReport>> tests = new TreeMap<>();
        Iterator<? extends List<FlowBenchReport>> reportsIt = reportLists.iterator();
        String benchmarkTypeDesc = null;
        for(String name : configurationNames) {
            List<FlowBenchReport> reports = reportsIt.next();
            for(FlowBenchReport report : reports) {
                Pair<String, String> test = new ImmutablePair<>(report.getSimpleClassName(), report.getMethodName());
                if(resultType.isInstance(report.getResult())) {
                    tests.putIfAbsent(test, new HashMap<>());
                    tests.get(test).put(name, report);
                    if(benchmarkTypeDesc == null) {
                        benchmarkTypeDesc = report.getResult().getBenchmarkTypeDesc();
                    }
                }
            }
        }
        if(!tests.isEmpty()) {
            Map<String, Method> tableStatMethods = getTableStatMethods(resultType);
            List<String> statsPerConfig = new LinkedList<>();
            statsPerConfig.add("Time (ms)");
            statsPerConfig.addAll(tableStatMethods.keySet());
            GroupedTable table = new GroupedTable(benchmarkTypeDesc + " Results")
                    .addGroup("", "Benchmark", "Test");
            for(String name : configurationNames) {
                table.addGroup(name, statsPerConfig.toArray(new String[0]));
            }
            for(Pair<String, String> test : tests.keySet()) {
                Object[][] row = new Object[configurationNames.size() + 1][];
                row[0] = new String[]{test.getLeft(), test.getRight()};
                Map<String, FlowBenchReport> reports = tests.get(test);
                int i = 1;
                for(String name : configurationNames) {
                    List<String> rowData = new LinkedList<>();
                    if(reports.containsKey(name)) {
                        FlowBenchReport report = reports.get(name);
                        FlowBenchResult result = report.getResult();
                        rowData.add(String.format("%d", report.getTimeElapsed()));
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
                        rowData.add("Error"); // Time column
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
     * Gathers method annotated with the TableStat annotation from the specified class.
     *
     * @param resultType class whose methods are checked
     * @return mapping from TableStat annotation names to the methods they annotate
     */
    private Map<String, Method> getTableStatMethods(Class<? extends FlowBenchResult> resultType) {
        Map<String, Method> statMethods = new TreeMap<>();
        for(Class<?> clazz = resultType; clazz != FlowBenchResult.class; clazz = clazz.getSuperclass()) {
            for(Method method : resultType.getDeclaredMethods()) {
                if(method.isAnnotationPresent(TableStat.class)) {
                    String statName = method.getAnnotation(TableStat.class).name();
                    statMethods.put(statName, method);
                }
            }
        }
        return statMethods;
    }

    /**
     * Validates phosphorConfigurations to ensure that configurations have valid names and specified valid instrumented
     * JVM directories. Canonicalizes the phosphorConfigurations properties.
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
            config.options = PhosphorInstrumentingMojo.canonicalizeProperties(config.options, true);
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
     * Converts the specified properties into an argument line for Phosphor's premain.
     *
     * @param properties properties to be converted
     * @param cacheDir   value that should be used for Phosphor's cacheDir option
     * @return string argument line that represents properties in a format that is accepted by Phosphor
     */
    private String createPhosphorArgLine(Properties properties, File cacheDir) {
        StringBuilder builder = new StringBuilder().append("=").append(phosphorCacheDirectoryOptionName).append("=")
                .append(cacheDir.getAbsolutePath());
        Set<String> propNames = properties.stringPropertyNames();
        for(String propName : propNames) {
            builder.append(',').append(propName);
            if(!"true".equals(properties.getProperty(propName))) {
                builder.append('=').append(properties.getProperty(propName));
            }
        }
        return builder.toString();
    }

    /**
     * Ensures that the specified directory exists and is empty.
     *
     * @param dir the directory to be created or cleaned
     * @throws IOException if dir could not be created or cleaned
     */
    private void createOrCleanDirectory(File dir) throws IOException {
        if(dir.isDirectory()) {
            FileUtils.cleanDirectory(dir);
        } else {
            if(dir.isFile()) {
                if(!dir.delete()) {
                    throw new IOException("Failed to delete: " + dir);
                }
            }
            if(!dir.mkdirs()) {
                throw new IOException("Failed to create directory: " + dir);
            }
        }
    }

    /**
     * Checked the specified directory to determine whether it was a Phosphor cache directory for a Phosphor agent that
     * used the same arguments as the specified argument line and used an instrumented JVM with the same properties
     * as the specified properties.
     *
     * @param cacheDir                  the directory to be checked
     * @param phosphorArgLine           the expected arguments
     * @param instrumentedJVMProperties instrumented JVM properties that should be matched
     * @return true if cacheDir contains a Phosphor arguments file whose contents match phosphorArgLine and a Phosphor
     * JVM properties file whose contents match instrumentedJVMProperties
     */
    private boolean isExistingCacheDirectory(File cacheDir, String phosphorArgLine, Properties instrumentedJVMProperties) {
        try {
            if(cacheDir.isDirectory()) {
                File argsFile = new File(cacheDir, PHOSPHOR_ARGS_CACHE_FILE);
                File jvmPropertiesFile = new File(cacheDir, PROPERTIES_FILE_NAME);
                if(argsFile.isFile() && jvmPropertiesFile.isFile()) {
                    String existingArgLine = new String(Files.readAllBytes(argsFile.toPath())).trim();
                    Properties existingProperties = new Properties();
                    existingProperties.load(new FileReader(jvmPropertiesFile));
                    return phosphorArgLine.equals(existingArgLine) && instrumentedJVMProperties.equals(existingProperties);
                }
            }
        } catch(IOException e) {
            // False should be returned in this situation as well
        }
        return false;
    }

    /**
     * Returns the properties used when instrumenting the specified configuration's JVM
     *
     * @param config the configuration whose JVM's properties are being checked
     * @return null if a property file cannot be found and read for config's JVM or config's JVM's properties
     */
    private Properties getJVMProperties(PhosphorConfig config) {
        try {
            File jvmPropertiesFile = new File(config.instrumentedJVM, PROPERTIES_FILE_NAME);
            Properties jvmProperties = new Properties();
            jvmProperties.load(new FileReader(jvmPropertiesFile));
            return jvmProperties;
        } catch(IOException e) {
            return null;
        }
    }

    /**
     * Runs flow benchmarks in isolated Phosphor-instrumented JVMs.
     *
     * @param reportDirectory directory to which .json benchmarks reports should be written
     * @param phosphorJarPath path to a jar file for Phosphor
     * @return a list of the files to which benchmark results were written
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if a thread interrupts this thread while it is waiting for a benchmark process to finish
     */
    private List<File> runBenchmarks(File reportDirectory, String phosphorJarPath) throws IOException, InterruptedException {
        List<File> reportFiles = new LinkedList<>();
        for(PhosphorConfig config : phosphorConfigurations) {
            File cacheDir = getCacheDir(config);
            String phosphorArgLine = createPhosphorArgLine(config.options, cacheDir);
            Properties jvmProperties = getJVMProperties(config);
            if(jvmProperties == null || !isExistingCacheDirectory(cacheDir, phosphorArgLine, jvmProperties)) {
                getLog().info("Creating new Phosphor cache directory: " + cacheDir);
                createOrCleanDirectory(cacheDir);
                if(jvmProperties != null) {
                    // Add file to indicate the arguments and JVM properties used for the cache
                    File argsFile = new File(cacheDir, PHOSPHOR_ARGS_CACHE_FILE);
                    Files.write(argsFile.toPath(), phosphorArgLine.getBytes());
                    File jvmPropertiesFile = new File(cacheDir, PROPERTIES_FILE_NAME);
                    jvmProperties.store(new FileWriter(jvmPropertiesFile), null);
                }
            } else {
                getLog().info("Using existing Phosphor cache directory: " + cacheDir);
            }
            File reportFile = new File(reportDirectory, config.name + ".json");
            getLog().info("Running flow benchmarks for Phosphor configurations: " + config.name);
            forkBenchmarkRunner(config.instrumentedJVM, phosphorArgLine, reportFile, phosphorJarPath);
            reportFiles.add(reportFile);
        }
        return reportFiles;
    }

    /**
     * Runs benchmarks for a particular Phosphor configuration is a separate process.
     *
     * @param instrumentedJVM directory of the instrumented JVM that should be used to run the benchmark
     * @param phosphorArgLine argument line for the Phosphor agent
     * @param reportFile      file to which created process should write its json report
     * @param phosphorJarPath path the jat file for Phosphor
     * @throws InterruptedException if a thread interrupts this thread while it is waiting for the benchmark process to finish
     * @throws IOException          if an I/O error occurs
     */
    private void forkBenchmarkRunner(File instrumentedJVM, String phosphorArgLine, File reportFile, String phosphorJarPath)
            throws InterruptedException, IOException {
        String javaCommand = new File(instrumentedJVM, "bin" + File.separator + "java").getAbsolutePath();
        List<String> commands = new LinkedList<>();
        commands.add(javaCommand);
        commands.add("-cp");
        commands.add(String.join(File.pathSeparator, getClassPath()));
        commands.add("-Xbootclasspath/p:" + phosphorJarPath);
        commands.add("-javaagent:" + phosphorJarPath + phosphorArgLine);
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
