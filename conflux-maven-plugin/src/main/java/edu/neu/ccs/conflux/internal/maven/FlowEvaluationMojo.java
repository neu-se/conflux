package edu.neu.ccs.conflux.internal.maven;

import edu.columbia.cs.psl.phosphor.PhosphorOption;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;
import edu.gmu.swe.phosphor.ignored.maven.PhosphorInstrumentUtil;
import edu.neu.ccs.conflux.internal.FlowEvaluationRunner;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static edu.gmu.swe.phosphor.ignored.maven.PhosphorInstrumentUtil.createPhosphorAgentArgument;
import static edu.gmu.swe.phosphor.ignored.maven.PhosphorInstrumentUtil.getPhosphorJarFile;

/**
 * Runs benchmarks and studies with different Phosphor configurations and reports the results.
 */
@Mojo(name = "evaluate", defaultPhase = LifecyclePhase.INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.TEST)
public class FlowEvaluationMojo extends AbstractMojo {

    /**
     * Name of the directory used to store results for the different configurations.
     */
    private static final String REPORT_DIRECTORY = "flow-reports";
    /**
     * Name of the file used to store the Phosphor configuration options used for a particular cache directory.
     */
    private static final String PHOSPHOR_CACHE_PROPERTIES_FILE = "phosphor-cache-properties";
    /**
     * String argument used to tell forked JVMs to wait for a debugger.
     */
    private static final String DEBUG_ARG = "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005";
    /**
     * Configuration option name used by Phosphor to specify a cache directory for instrumented files.
     */
    private static final String PHOSPHOR_CACHE_DIRECTORY_OPTION_NAME = PhosphorOption.CACHE_DIR.createOption().getOpt();
    /**
     * True if forked JVMs should wait for a debugger.
     */
    private static final boolean debugForks = Boolean.getBoolean("flow.debug");
    /**
     * The name of the Phosphor configuration to be run or null if all of the configurations should be run.
     */
    private static final String selectedConfig = System.getProperty("flow.config", null);
    /**
     * The project being evaluated.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    /**
     * Phosphor configurations to be evaluated.
     */
    @Parameter(property = "phosphorConfigurations", readonly = true, required = true)
    private List<PhosphorConfig> phosphorConfigurations;
    /**
     * List of paths to any JAR files, other than the JAR file for Phosphor, that need to be added to the bootclasspath.
     */
    @Parameter(property = "bootClasspathJars", readonly = true)
    private List<String> bootClasspathJars;
    /**
     * Lengths of tainted inputs to be used in the generated plots.
     */
    @Parameter(property = "plotNumbersOfEntities", readonly = true)
    private Set<Integer> plotNumbersOfEntities;
    /**
     * Length of tainted inputs to be used in the generated table.
     */
    @Parameter(property = "tableNumberOfEntities", readonly = true)
    private int tableNumberOfEntities;

    /**
     * Runs evaluations with different Phosphor configurations and reports the results to standard out.
     *
     * @throws MojoFailureException if evaluations fail to run
     */
    @Override
    public void execute() throws MojoFailureException {
        try {
            if (!isNonEmptyDirectory(getEvaluationDirectory())) {
                getLog().info("No flow evaluations detected");
                return;
            }
        } catch (IOException e) {
            throw new MojoFailureException("Failed to check test output directory for evaluations", e);
        }
        if (selectedConfig != null) {
            // Remove configurations that were not selected
            phosphorConfigurations.removeIf(phosphorConfig -> !selectedConfig.equals(phosphorConfig.name));
        }
        validateNumberOfEntities();
        validatePhosphorConfigurations();
        try {
            File reportDirectory = new File(project.getBuild().getDirectory(), REPORT_DIRECTORY);
            PhosphorInstrumentUtil.createOrCleanDirectory(reportDirectory);
            List<File> reportFiles = new LinkedList<>();
            boolean success = runEvaluations(reportDirectory, reportFiles);
            if (success) {
                List<String> configurationNames = new ArrayList<>();
                for (PhosphorConfig config : phosphorConfigurations) {
                    configurationNames.add(config.getName());
                }
                ReportManager reportManager = new ReportManager(configurationNames, reportFiles,
                        plotNumbersOfEntities, tableNumberOfEntities);
                reportManager.printBenchResultsTable();
                reportManager.printStudyResultsTable();
                reportManager.writeLatexResults(new File(project.getBuild().getDirectory()));
            } else {
                throw new MojoFailureException("Failed to evaluation configurations");
            }
        } catch (InterruptedException | IOException e) {
            throw new MojoFailureException("Failed to evaluation configurations", e);
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
     * Validates phosphorConfigurations to ensure that configurations have valid names and instrumented
     * JVM directories.
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
        String cacheDirProperty = phosphorConfiguration.options.getProperty(PHOSPHOR_CACHE_DIRECTORY_OPTION_NAME);
        if(cacheDirProperty != null && cacheDirProperty.length() > 0) {
            return new File(cacheDirProperty);
        } else {
            String cacheDirName = phosphorConfiguration.name + "-cache";
            phosphorConfiguration.options.setProperty(PHOSPHOR_CACHE_DIRECTORY_OPTION_NAME, cacheDirName);
            return new File(project.getBuild().getDirectory(), cacheDirName);
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
     * Runs flow evaluations in isolated Phosphor-instrumented JVMs.
     *
     * @param reportDirectory directory to which evaluation reports should be written
     * @param reportFiles     list to which files containing evaluation reports should be written
     * @return true if all of the evaluations completed successfully
     * @throws IOException                           if an I/O error occurs
     * @throws InterruptedException                  if a thread interrupts this thread while it is waiting for an
     *                                               evaluation process to finish
     * @throws DependencyResolutionRequiredException if a required dependency could not be resolved
     */
    private boolean runEvaluations(File reportDirectory, List<File> reportFiles) throws IOException,
            InterruptedException, DependencyResolutionRequiredException {
        boolean success = true;
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
            getLog().info("Running evaluation for Phosphor configuration: " + config.name);
            success &= runEvaluation(config.instrumentedJVM, config.options, reportFile);
            reportFiles.add(reportFile);
        }
        return success;
    }

    /**
     * Evaluates a Phosphor configuration is a separate process.
     *
     * @param instrumentedJVM directory of the instrumented JVM that should be used to run the evaluation
     * @param properties      canonicalized properties that specify the Phosphor configuration options that should be
     *                        used in the fork
     * @param reportFile      file to which created process should write its json report
     * @return true if the forked process return successfully
     * @throws InterruptedException                  if a thread interrupts this thread while it is waiting for the
     *                                               evaluation process to finish
     * @throws IOException                           if an I/O error occurs
     * @throws DependencyResolutionRequiredException if a required dependency could not be resolved
     */
    private boolean runEvaluation(File instrumentedJVM, Properties properties, File reportFile)
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
        commands.add(FlowEvaluationRunner.class.getName());
        commands.add(getEvaluationDirectory().getAbsolutePath());
        commands.add(reportFile.getAbsolutePath());
        Set<Integer> allNumberOfEntities = new HashSet<>(plotNumbersOfEntities);
        allNumberOfEntities.add(tableNumberOfEntities);
        for (int NumberOfEntities : allNumberOfEntities) {
            commands.add(Integer.toString(NumberOfEntities));
        }
        Process process = new ProcessBuilder(commands).inheritIO().start();
        if (process.waitFor() != 0) {
            getLog().error("Error in flow evaluation process");
            return false;
        }
        return true;
    }

    private File getEvaluationDirectory() {
        return new File(project.getBuild().getTestOutputDirectory());
    }

    public static boolean isNonEmptyDirectory(File file) throws IOException {
        Path path = file.toPath();
        if (Files.isDirectory(path)) {
            try (Stream<Path> entries = Files.list(path)) {
                return entries.findFirst().isPresent();
            }
        }
        return false;
    }
}
