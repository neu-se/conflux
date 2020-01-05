package edu.gmu.swe.phosphor.ignored.maven;

import edu.columbia.cs.psl.phosphor.Configuration;
import edu.columbia.cs.psl.phosphor.Instrumenter;
import edu.columbia.cs.psl.phosphor.instrumenter.TaintTrackingClassVisitor;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.ClassVisitor;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
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
	 * Constant name of phosphor-properties files used to store information about the configuration options that were
	 * used by Phosphor when instrumenting a JVM.
	 */
	public static final String PROPERTIES_FILE_NAME = "phosphor-properties";

	/**
	 * Options considered by Phosphor that have boolean values.
	 */
	private static final String[] BOOLEAN_VALUED_PHOSPHOR_OPTIONS = new String[]{
			"controlTrack", "lightControlTrack", "controlTrackExceptions", "withoutDataTrack", "withArrayLengthTags",
			"withoutFieldHiding", "withoutPropagation", "withEnumsByValue", "forceUnboxAcmpEq", "disableJumpOptimizations",
			"readAndSaveBCIs", "withArrayIndexTags", "withoutBranchNotTaken", "skipLocals", "alwaysCheckForFrames",
			"implicitHeadersNoTracking", "bindingControlTracking"
	};

	/**
	 * Options considered by Phosphor that have string values.
	 */
	private static final String[] STRING_VALUED_PHOSPHOR_OPTIONS = new String[]{
			"priorClassVisitor"
	};

	/**
	 * Path to a directory in which the target location for the instrumented JVM should be placed.
	 */
	@Parameter(property = "targetBaseDir", defaultValue = "${project.build.directory}", readonly = true)
	private File targetBaseDir;

	/**
	 *  Phosphor configuration options that should be set while instrumenting the JVM.
	 */
	@Parameter(property = "options", readonly = true, required = true)
	private Properties options;

	/**
	 * Name of the target location directory for the instrumented JVM.
	 */
	@Parameter(property = "name", readonly = true, required = true)
	private String name;

	/**
	 *  Path to directory where the JDK or JRE installation to be instrumented is installed.
	 */
	@Parameter(property = "baseJVMDir", readonly = true)
	private String baseJVMDir;

	/**
	 * Creates a Phosphor-instrumented JVM at a target location
	 * @throws MojoFailureException if the Phosphor-instrumented JVM cannot be created at the target location
	 */
	@Override
	public void execute() throws MojoFailureException {
		Properties canonOptions = canonicalizeProperties(options);
		File jvmDir = getJVMDir();
		File instJVMDir = new File(targetBaseDir, name);
		if(!checkForExistingInstrumentedJVM(instJVMDir, canonOptions)) {
			getLog().info(String.format("Generating Phosphor-instrumented JVM %s with options: %s", instJVMDir, canonOptions));
			try{
				generateInstrumentedJVM(jvmDir, instJVMDir, canonOptions);
			} catch(IOException e) {
				throw new MojoFailureException("Failed to create instrumented JVM", e);
			}
		} else {
			getLog().info(String.format("No generation necessary: existing Phosphor-instrumented JVM %s with correct " +
					"properties(%s) found", instJVMDir, canonOptions));
		}
	}

	/**
	 * Creates a standardized copy of the specified properties where each boolean-valued Phosphor option is mapped to
	 * either "true" or not present in the properties, each string-value Phosphor option is either mapped to a non-null
	 * non-empty string or not present in properties, and no other keys are present in the properties.
	 *
	 * @param properties un-standardized properties to be standardized
	 * @return a standardized copy of properties
	 */
	private Properties canonicalizeProperties(Properties properties) {
		Set<String> propNames = properties.stringPropertyNames();
		Properties canonProps = new Properties();
		for(String boolProp : BOOLEAN_VALUED_PHOSPHOR_OPTIONS) {
			if(propNames.contains(boolProp)) {
				if(properties.getProperty(boolProp).length() == 0 || "true".equals(properties.getProperty(boolProp).toLowerCase())) {
					canonProps.setProperty(boolProp, "true");
				}
			}
		}
		for(String strProp : STRING_VALUED_PHOSPHOR_OPTIONS) {
			if(propNames.contains(strProp)) {
				if(properties.getProperty(strProp) != null && properties.getProperty(strProp).length() > 0) {
					canonProps.setProperty(strProp, properties.getProperty(strProp));
				}
			}
		}
		return canonProps;
	}

	/**
	 * Checks whether the specified file is a directory containing a phosphor-properties file whose properties match
	 * the specified desired properties.
	 *
	 * @param instJVMDir path to be checked for an existing instrumented JVM
	 * @param desiredProperties the properties that the existing instrumented JVM must have
	 * @return true if instJVMDir is a directory that contains a phosphor-properties file that matches desiredProperties
	 */
	private boolean checkForExistingInstrumentedJVM(File instJVMDir, Properties desiredProperties) {
		if(instJVMDir.isDirectory()) {
			File propFile = new File(instJVMDir, PROPERTIES_FILE_NAME);
			if(propFile.isFile()) {
				try {
					Properties existingProperties = new Properties();
					existingProperties.load(new FileReader(propFile));
					return desiredProperties.equals(existingProperties);
				} catch(IllegalArgumentException | IOException e) {
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * Determines the correct path for the directory where the JDK or JRE installation to be instrumented is installed
	 * as follows:
	 * Use the value of baseJVM if it provided and is non-null and not the empty string. Else use the value of the
	 * environmental variable INST_HOME if it is set to a non-null, non empty string value. Else use the value of
	 * the environmental variable JAVA_HOME if it is set to a non-null, non empty string value. Otherwise no appropriate
	 * path value was provided.
	 * @ return a file representing the path to the determined JDK or JRE installation directory
	 * @throws MojoFailureException if an appropriate JDK or JRE installation path was not provided or the provided
	 * 	path does not point to a directory
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
	 * Creates a Phosphor-instrumented JVM at the specified directory with the specified Phosphor configuration options.
	 *
	 * @param jvmDir the source directory where the JDK or JRE is installed
	 * @param instJVMDir the target directory where the Phosphor-instrumented JVM should be created
	 * @param options canonicalized properties that specify the Phosphor configuration options that should be used
	 * @throws IOException if an I/O error occurs
	 */
	private void generateInstrumentedJVM(File jvmDir, File instJVMDir, Properties options) throws IOException {
		if(instJVMDir.exists()) {
			// Delete existing directory or file if necessary
			Files.walkFileTree(instJVMDir.toPath(), new DeletingFileVisitor());
		}
		if(!instJVMDir.mkdirs()) {
			throw new IOException("Failed to create target directory for Phosphor-instrumented: " + instJVMDir);
		}
		// Set-up Phosphor's configuration options
		Configuration.BINDING_CONTROL_FLOWS_ONLY = "true".equals(options.getProperty("bindingControlTracking"));
		Configuration.IMPLICIT_TRACKING = "true".equals(options.getProperty("controlTrack")) || Configuration.BINDING_CONTROL_FLOWS_ONLY;
		Configuration.IMPLICIT_LIGHT_TRACKING = "true".equals(options.getProperty("lightControlTrack"));
		Configuration.IMPLICIT_EXCEPTION_FLOW = "true".equals(options.getProperty("controlTrackExceptions"));
		Configuration.DATAFLOW_TRACKING = !"true".equals(options.getProperty("withoutDataTrack"));
		Configuration.WITHOUT_FIELD_HIDING = "true".equals(options.getProperty("withoutFieldHiding"));
		Configuration.WITHOUT_PROPAGATION = "true".equals(options.getProperty("withoutPropAgation"));
		Configuration.WITH_ENUM_BY_VAL = "true".equals(options.getProperty("withEnumsByValue"));
		Configuration.WITH_UNBOX_ACMPEQ = "true".equals(options.getProperty("forceUnboxAcmpEq"));
		Configuration.WITH_TAGS_FOR_JUMPS = "true".equals(options.getProperty("disableJumpOptimizations"));
		Configuration.READ_AND_SAVE_BCI = "true".equals(options.getProperty("readAndSaveBCIs"));
		Configuration.ARRAY_INDEX_TRACKING = "true".equals(options.getProperty("withArrayIndexTags"));
		Configuration.WITHOUT_BRANCH_NOT_TAKEN = "true".equals(options.getProperty("withoutBranchNotTaken"));
		Configuration.SKIP_LOCAL_VARIABLE_TABLE = "true".equals(options.getProperty("skipLocals"));
		Configuration.ALWAYS_CHECK_FOR_FRAMES = "true".equals(options.getProperty("alwaysCheckForFrames"));
		Configuration.IMPLICIT_HEADERS_NO_TRACKING = "true".equals(options.getProperty("implicitHeadersNoTracking"));
		String priorClassVisitorName = options.getProperty("priorClassVisitor");
		if(priorClassVisitorName != null) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends ClassVisitor> temp = (Class<? extends ClassVisitor>)Class.forName(priorClassVisitorName);
				Configuration.PRIOR_CLASS_VISITOR = temp;
			} catch(Exception e) {
				getLog().error("Failed to create specified prior class visitor: " + priorClassVisitorName);
			}
		}
		Configuration.init();
		TaintTrackingClassVisitor.IS_RUNTIME_INST = false;
		Instrumenter.ANALYZE_ONLY = true;
		Instrumenter._main(new String[]{jvmDir.getAbsolutePath(), instJVMDir.getAbsolutePath()});
		Instrumenter.ANALYZE_ONLY = false;
		Instrumenter._main(new String[]{jvmDir.getAbsolutePath(), instJVMDir.getAbsolutePath()});
		// Add phosphor-properties file to directory
		File propsFile = new File(instJVMDir, PROPERTIES_FILE_NAME);
		options.store(new FileWriter(propsFile), null);
		// Set execute permissions
		Files.walkFileTree(new File(instJVMDir, "bin").toPath(), new ExecutePermissionAssigningFileVisitor() );
		Files.walkFileTree(new File(instJVMDir, "lib").toPath(), new ExecutePermissionAssigningFileVisitor() );
		if(new File(instJVMDir,"jre").exists()) {
			Files.walkFileTree(new File(instJVMDir, "jre" + File.separator + "bin").toPath(), new ExecutePermissionAssigningFileVisitor());
			Files.walkFileTree(new File(instJVMDir, "jre" + File.separator + "lib").toPath(), new ExecutePermissionAssigningFileVisitor());
		}
	}
}
