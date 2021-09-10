package edu.neu.ccs.conflux.internal.maven;

import edu.neu.ccs.conflux.internal.FlowReport;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregates the results of flow evaluations to produce a final report.
 */
@Mojo(name = "report", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class FlowReportingMojo extends AbstractMojo {
    /**
     * The project being evaluated.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    /**
     * List of directories containing evaluation results.
     */
    @Parameter(property = "reportDirectories", readonly = true, required = true)
    private List<File> reportDirectories;

    /**
     * Aggregates the results of flow evaluations to produce a final report.
     *
     * @throws MojoFailureException if the report cannot be created or written
     */
    @Override
    public void execute() throws MojoFailureException {
        try {
            File finalReportDirectory = new File(project.getBuild().getDirectory());
            if (!finalReportDirectory.isDirectory() && !finalReportDirectory.mkdirs()) {
                throw new MojoFailureException("Failed to create directory for final reports");
            }
            Set<File> reportFiles = reportDirectories.stream()
                    .map(File::listFiles)
                    .filter(Objects::nonNull)
                    .map(Arrays::asList)
                    .flatMap(List::stream)
                    .collect(Collectors.toSet());
            Map<String, FlowReport> configReportMap = new HashMap<>();
            for (File reportFile : reportFiles) {
                String configName = FlowEvaluationMojo.getConfigurationName(reportFile);
                configReportMap.putIfAbsent(configName, new FlowReport());
                configReportMap.get(configName).addAll(FlowReport.readFromFile(reportFile));
            }
            AggregateFlowReport report = new AggregateFlowReport(configReportMap);
            File reportFile = new File(finalReportDirectory, "flow-report.json");
            getLog().info("Writing flow report to file: " + reportFile);
            report.writeToFile(reportFile);
        } catch (IOException e) {
            throw new MojoFailureException("Failed to create or write flow report", e);
        }
    }
}
