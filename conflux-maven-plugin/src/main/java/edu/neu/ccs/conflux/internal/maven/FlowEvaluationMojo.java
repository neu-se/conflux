package edu.neu.ccs.conflux.internal.maven;

import edu.columbia.cs.psl.phosphor.PhosphorOption;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;
import edu.gmu.swe.phosphor.ignored.maven.PhosphorInstrumentUtil;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static edu.gmu.swe.phosphor.ignored.maven.PhosphorInstrumentUtil.createPhosphorAgentArgument;
import static edu.gmu.swe.phosphor.ignored.maven.PhosphorInstrumentUtil.getPhosphorJarFile;

/**
 * Runs benchmarks and studies with different Phosphor configurations and reports the results.
 */
@Mojo(name = "evaluate", defaultPhase = LifecyclePhase.INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class FlowEvaluationMojo extends AbstractMojo {

    /**
     * Name of the directory used to store results from the different configurations
     */
    private static final String REPORT_DIRECTORY = "flow-reports";
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
     * The name of the Phosphor configuration to be run or null if all of the configurations should be run
     */
    private static final String selectedConfig = System.getProperty("flow.config", null);
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
     * True if execution times should be reported in tables
     */
    @Parameter(property = "reportExecutionTime", readonly = true, defaultValue = "false")
    private boolean reportExecutionTime;
    /**
     * Lengths of tainted inputs to be used in the generated plots
     */
    @Parameter(property = "plotNumbersOfEntities", readonly = true)
    private Set<Integer> plotNumbersOfEntities;
    /**
     * Length of tainted inputs to be used in the generated table
     */
    @Parameter(property = "tableNumberOfEntities", readonly = true)
    private int tableNumberOfEntities;

    /**
     * Runs flow benchmarks with different Phosphor configurations and reports the results to standard out.
     *
     * @throws MojoFailureException if benchmarks fail to run
     */
    @Override
    public void execute() throws MojoFailureException {
        if(selectedConfig != null) {
            // Remove configurations that were not selected
            phosphorConfigurations.removeIf(phosphorConfig -> !selectedConfig.equals(phosphorConfig.name));
        }
        validateNumberOfEntities();
        validatePhosphorConfigurations();
        try {
            File reportDirectory = new File(buildDir, REPORT_DIRECTORY);
            PhosphorInstrumentUtil.createOrCleanDirectory(reportDirectory);
            List<File> reportFiles = runBenchmarks(reportDirectory);
            FlowBenchmarkFullReport fullReport = new FlowBenchmarkFullReport(getConfigurationNames(), reportFiles,
                    reportExecutionTime, plotNumbersOfEntities, tableNumberOfEntities);
            fullReport.printResultsTable();
            fullReport.writeLatexResults(buildDir);
        } catch (InterruptedException | IOException e) {
            throw new MojoFailureException("Failed to benchmark configurations", e);
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoFailureException("A required dependency could not be resolved", e);
        }
    }

    /**
     * Validates plotNumberOfEntities and tableNumberOfEntities to ensure that lengths are non-negative.
     *
     * @throws MojoFailureException if a length is negative
     */
    private void validateNumberOfEntities() throws MojoFailureException {
        if(plotNumbersOfEntities == null) {
            plotNumbersOfEntities = Collections.emptySet();
        }
        for(int NumberOfEntities : plotNumbersOfEntities) {
            if(NumberOfEntities < 0) {
                throw new MojoFailureException("Plot input length cannot be less than 0");
            }
        }
        if(tableNumberOfEntities < 0) {
            throw new MojoFailureException("Table input length cannot be less than 0");
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
                    return desiredProperties.equals(existingProperties);
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
     * @throws IOException                           if an I/O error occurs
     * @throws InterruptedException                  if a thread interrupts this thread while it is waiting for a benchmark process to
     *                                               finish
     * @throws DependencyResolutionRequiredException if a required dependency could not be resolved
     */
    private List<File> runBenchmarks(File reportDirectory) throws IOException, InterruptedException,
            DependencyResolutionRequiredException {
        List<File> reportFiles = new LinkedList<>();
        for (PhosphorConfig config : phosphorConfigurations) {
            File cacheDir = getCacheDir(config);
            if (!isExistingCacheDirectory(cacheDir, config.options)) {
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
     * @throws InterruptedException                  if a thread interrupts this thread while it is waiting for the benchmark process to finish
     * @throws IOException                           if an I/O error occurs
     * @throws DependencyResolutionRequiredException if a required dependency could not be resolved
     */
    private void forkBenchmarkRunner(File instrumentedJVM, Properties properties, File reportFile)
            throws InterruptedException, IOException, DependencyResolutionRequiredException {
        String javaCommand = new File(instrumentedJVM, "bin" + File.separator + "java").getAbsolutePath();
        List<String> commands = new LinkedList<>();
        commands.add(javaCommand);
        commands.add("-cp");
        commands.add(String.join(File.pathSeparator, project.getTestClasspathElements()));
        String phosphorJarPath = getPhosphorJarFile().getAbsolutePath();
        StringBuilder bootClassPathBuilder = new StringBuilder("-Xbootclasspath/p:").append(phosphorJarPath);
        if (bootClasspathJars != null && !bootClasspathJars.isEmpty()) {
            for (String s : bootClasspathJars) {
                bootClassPathBuilder.append(':').append(s);
            }
        }
        commands.add(bootClassPathBuilder.toString());
        commands.add("-javaagent:" + phosphorJarPath + createPhosphorAgentArgument(properties));
        if (debugForks) {
            commands.add(DEBUG_ARG);
        }
        commands.add(ForkedFlowBenchmarkRunner.class.getName());
        commands.add(testClassesDirectory.getAbsolutePath());
        commands.add(reportFile.getAbsolutePath());
        Set<Integer> allNumberOfEntities = new HashSet<>(plotNumbersOfEntities);
        allNumberOfEntities.add(tableNumberOfEntities);
        for (int NumberOfEntities : allNumberOfEntities) {
            commands.add(Integer.toString(NumberOfEntities));
        }
        Process process = new ProcessBuilder(commands).inheritIO().start();
        if (process.waitFor() != 0) {
            getLog().error("Error in flow benchmark process");
        }
    }
}
