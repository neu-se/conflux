package edu.gmu.swe.phosphor.ignored.maven;

import edu.columbia.cs.psl.phosphor.Instrumenter;
import edu.columbia.cs.psl.phosphor.PhosphorOption;
import edu.columbia.cs.psl.phosphor.org.apache.commons.cli.Option;
import edu.columbia.cs.psl.phosphor.org.apache.commons.cli.Options;
import edu.columbia.cs.psl.phosphor.struct.SinglyLinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashMap;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

/**
 * Creates a Phosphor-instrumented JVM at a target location. If a directory already exists at the target location,
 * checks to see if it contains a phosphor-properties file. If it does and the phosphor-properties file's properties match
 * the desired properties for the Phosphor-instrumented JVM to be created, this directory is assumed to contain a properly
 * instrumented JVM. Otherwise, deletes any existing files or directories at the target location and creates a
 * Phosphor-instrumented JVM there.
 */
@SuppressWarnings("unused")
@Mojo(name = "instrument", defaultPhase = LifecyclePhase.COMPILE)
public class PhosphorInstrumentingMojo extends AbstractMojo {

    /**
     * Constant name of the file used to store information about the configuration options that were used by Phosphor
     * when instrumenting a JVM.
     */
    public static final String PROPERTIES_FILE_NAME = "phosphor-properties";

    /**
     * Constant name of the file used to store the checksum of the Phosphor jar used to instrument a JVM.
     */
    public static final String CHECKSUM_FILE_NAME = "phosphor-checksum.md5sum";

    /**
     * Path to a directory in which the target location for the instrumented JVM should be placed.
     */
    @Parameter(property = "targetBaseDir", defaultValue = "${project.build.directory}", readonly = true)
    private File targetBaseDir;

    /**
     * Phosphor configuration options that should be set while instrumenting the JVM.
     */
    @Parameter(property = "options", readonly = true, required = true)
    private Properties options;

    /**
     * Name of the target location directory for the instrumented JVM.
     */
    @Parameter(property = "name", readonly = true, required = true)
    private String name;

    /**
     * Path to directory where the JDK or JRE installation to be instrumented is installed.
     */
    @Parameter(property = "baseJVMDir", readonly = true)
    private String baseJVMDir;

    @Parameter(property = "invalidateBasedOnChecksum", readonly = true, defaultValue = "true")
    private boolean invalidateBasedOnChecksum;

    /**
     * MD5 MessageDigest instance used for generating checksums.
     */
    private MessageDigest md5Inst;

    public PhosphorInstrumentingMojo() {
        try {
            md5Inst = MessageDigest.getInstance("MD5");
        } catch(Exception e) {
            System.err.println("Failed to create MD5 MessageDigest.");
            e.printStackTrace();
        }
    }

    /**
     * Creates a Phosphor-instrumented JVM at a target location
     *
     * @throws MojoFailureException if the Phosphor-instrumented JVM cannot be created at the target location
     */
    @Override
    public void execute() throws MojoFailureException {
        Properties canonicalOptions = canonicalizeProperties(options, false);
        File jvmDir = getJVMDir();
        File instJVMDir = new File(targetBaseDir, name);
        byte[] checksum;
        try {
            checksum = generateChecksumForPhosphorJar();
        } catch(IOException e) {
            throw new MojoFailureException("Failed to generate checksum for Phosphor jar");
        }
        if(!checkForExistingInstrumentedJVM(instJVMDir, canonicalOptions, checksum)) {
            getLog().info(String.format("Generating Phosphor-instrumented JVM %s with options: %s", instJVMDir, canonicalOptions));
            try {
                generateInstrumentedJVM(jvmDir, instJVMDir, canonicalOptions, checksum);
            } catch(IOException e) {
                throw new MojoFailureException("Failed to create instrumented JVM", e);
            }
        } else {
            getLog().info(String.format("No generation necessary: existing Phosphor-instrumented JVM %s with correct " +
                    "properties(%s) and checksum found", instJVMDir, canonicalOptions));
        }
    }

    /**
     * Checks whether the specified file is a directory containing a phosphor-properties file whose properties match
     * the specified desired properties and a phosphor-checksum file whose bytes match the specified checksum
     * (if invalidating invalidateBasedOnChecksum is true).
     *
     * @param instJVMDir        path to be checked for an existing instrumented JVM
     * @param desiredProperties the properties that the existing instrumented JVM must have
     * @param checksum          the checksum that existing instrumented JVM must have associated with it
     * @return true if instJVMDir is a directory that contains a phosphor-properties file that matches desiredProperties
     * and a phosphor-checksum file whose bytes match the specified checksum (if invalidating
     * invalidateBasedOnChecksum is true).
     */
    private boolean checkForExistingInstrumentedJVM(File instJVMDir, Properties desiredProperties, byte[] checksum) {
        if(instJVMDir.isDirectory()) {
            File propFile = new File(instJVMDir, PROPERTIES_FILE_NAME);
            if(propFile.isFile()) {
                try {
                    Properties existingProperties = new Properties();
                    existingProperties.load(new FileReader(propFile));
                    if(!desiredProperties.equals(existingProperties)) {
                        return false;
                    }
                } catch(IllegalArgumentException | IOException e) {
                    return false;
                }
            }
            if(invalidateBasedOnChecksum) {
                try {
                    byte[] existingChecksum = Files.readAllBytes(new File(instJVMDir, CHECKSUM_FILE_NAME).toPath());
                    return Arrays.equals(checksum, existingChecksum);
                } catch(IOException e) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * @return the checksum of the jar file for Phosphor
     */
    private byte[] generateChecksumForPhosphorJar() throws IOException {
        byte[] jarBytes = Files.readAllBytes(getPhosphorJarFile().toPath());
        return md5Inst.digest(jarBytes);
    }

    /**
     * Determines the correct path for the directory where the JDK or JRE installation to be instrumented is installed
     * as follows:
     * Use the value of baseJVM if it provided and is non-null and not the empty string. Else use the value of the
     * environmental variable INST_HOME if it is set to a non-null, non empty string value. Else use the value of
     * the environmental variable JAVA_HOME if it is set to a non-null, non empty string value. Otherwise no appropriate
     * path value was provided.
     *
     * @throws MojoFailureException if an appropriate JDK or JRE installation path was not provided or the provided
     *                              path does not point to a directory
     * @ return a file representing the path to the determined JDK or JRE installation directory
     */
    private File getJVMDir() throws MojoFailureException {
        String path;
        String source;
        if(baseJVMDir != null && baseJVMDir.length() > 0) {
            path = baseJVMDir;
            source = "baseJVM property";
        } else if(System.getenv("INST_HOME") != null && System.getenv("INST_HOME").length() > 0) {
            path = System.getenv("INST_HOME");
            source = "INST_HOME environmental variable";
        } else if(System.getenv("JAVA_HOME") != null && System.getenv("JAVA_HOME").length() > 0) {
            path = System.getenv("JAVA_HOME");
            source = "JAVA_HOME environmental variable";
        } else {
            throw new MojoFailureException("Either baseJVM property or INST_HOME environmental variable or JAVA_HOME " +
                    "environmental variable must be set to a directory where the JDK or JRE is installed");
        }
        File jvmDir = new File(path);
        if(!jvmDir.isDirectory()) {
            throw new MojoFailureException(String.format("Value for %s (%s) must be set to a directory where the " +
                    "JDK or JRE is installed", source, path));
        }
        return jvmDir;
    }

    /**
     * Creates a standardized copy of the specified properties where each Phosphor option without an argument is mapped
     * to either true or not present in the properties, each Phosphor option with an argument is either mapped to a
     * non-null, non-empty string or not present in properties, and no other keys are present in the properties.
     *
     * @param isRuntimeInst true if the options should be standardized against the options available during dynamic
     *                      instrumentation, otherwise standardizes against the options available during static
     *                      instrumentation
     * @param properties    un-standardized properties to be standardized
     * @return a standardized copy of properties
     */
    public static Properties canonicalizeProperties(Properties properties, boolean isRuntimeInst) {
        Set<String> propNames = properties.stringPropertyNames();
        Properties canonicalProps = new Properties();
        Map<String, Option> phosphorOptionMap = createPhosphorOptionMap(isRuntimeInst);
        for(String propName : propNames) {
            if(phosphorOptionMap.containsKey(propName)) {
                Option option = phosphorOptionMap.get(propName);
                if(option.hasArg()) {
                    if(properties.getProperty(propName) != null && properties.getProperty(propName).length() > 0) {
                        canonicalProps.setProperty(option.getOpt(), properties.getProperty(propName));
                    }
                } else {
                    if(properties.getProperty(propName).length() == 0 || "true".equals(properties.getProperty(propName).toLowerCase())) {
                        canonicalProps.setProperty(option.getOpt(), "true");
                    }
                }
            } else {
                System.err.println("Unknown Phosphor option: " + propName);
            }
        }
        return canonicalProps;
    }

    /**
     * @param isRuntimeInst true if a map of options available during dynamic instrumentation should be returned,
     *                      otherwise returns a map of option available during static instrumentation
     * @return a mapping from the names of options available in Phosphor to an option instance that represents that
     * option
     */
    public static Map<String, Option> createPhosphorOptionMap(boolean isRuntimeInst) {
        Map<String, Option> phosphorOptionMap = new HashMap<>();
        Options options = PhosphorOption.createOptions(isRuntimeInst);
        for(Option option : options.getOptions()) {
            phosphorOptionMap.put(option.getOpt(), option);
            if(option.hasLongOpt()) {
                phosphorOptionMap.put(option.getLongOpt(), option);
            }
        }
        return phosphorOptionMap;
    }

    /**
     * @param properties canonicalized properties that specify the Phosphor configuration options that should be used
     * @return a list of option arguments to be passed to the Phosphor main method
     */
    private static SinglyLinkedList<String> createPhosphorOptionArguments(Properties properties) {
        SinglyLinkedList<String> arguments = new SinglyLinkedList<>();
        Set<String> propNames = properties.stringPropertyNames();
        for(String propName : propNames) {
            arguments.addLast("-" + propName);
            if(!"true".equals(properties.getProperty(propName))) {
                arguments.addLast(properties.getProperty(propName));
            }
        }
        return arguments;
    }

    /**
     * Creates a Phosphor-instrumented JVM at the specified directory with the specified Phosphor configuration options.
     *
     * @param jvmDir     the source directory where the JDK or JRE is installed
     * @param instJVMDir the target directory where the Phosphor-instrumented JVM should be created
     * @param properties canonicalized properties that specify the Phosphor configuration options that should be used
     * @param checksum   the checksum of the Phosphor jar that will be used to instrument the JVM
     * @throws IOException if an I/O error occurs
     */
    private static void generateInstrumentedJVM(File jvmDir, File instJVMDir, Properties properties, byte[] checksum) throws IOException {
        if(instJVMDir.exists()) {
            // Delete existing directory or file if necessary
            Files.walkFileTree(instJVMDir.toPath(), new DeletingFileVisitor());
        }
        if(!instJVMDir.mkdirs()) {
            throw new IOException("Failed to create target directory for Phosphor-instrumented files: " + instJVMDir);
        }
        SinglyLinkedList<String> arguments = createPhosphorOptionArguments(properties);
        arguments.addLast(jvmDir.getAbsolutePath());
        arguments.addLast(instJVMDir.getAbsolutePath());
        Instrumenter.main(arguments.toArray(new String[0]));
        // Add phosphor-properties file to directory
        File propsFile = new File(instJVMDir, PROPERTIES_FILE_NAME);
        properties.store(new FileWriter(propsFile), null);
        // Add phosphor-checksum file to directory
        File checksumFile = new File(instJVMDir, CHECKSUM_FILE_NAME);
        Files.write(checksumFile.toPath(), checksum);
        // Set execute permissions
        Files.walkFileTree(new File(instJVMDir, "bin").toPath(), new ExecutePermissionAssigningFileVisitor());
        Files.walkFileTree(new File(instJVMDir, "lib").toPath(), new ExecutePermissionAssigningFileVisitor());
        if(new File(instJVMDir, "jre").exists()) {
            Files.walkFileTree(new File(instJVMDir, "jre" + File.separator + "bin").toPath(), new ExecutePermissionAssigningFileVisitor());
            Files.walkFileTree(new File(instJVMDir, "jre" + File.separator + "lib").toPath(), new ExecutePermissionAssigningFileVisitor());
        }
    }

    /**
     * @return a File object pointing to the jar file for Phosphor
     */
    private static File getPhosphorJarFile() {
        try {
            return new File(Instrumenter.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch(URISyntaxException e) {
            throw new AssertionError();
        }
    }
}
