package edu.neu.ccs.conflux.internal.maven;

import edu.neu.ccs.conflux.internal.BenchInfo;
import edu.neu.ccs.conflux.internal.FlowReport;
import edu.neu.ccs.conflux.internal.RunResult;
import edu.neu.ccs.conflux.internal.StudyInfo;
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
     * Identifier of the evaluated Phosphor configurations.
     */
    @Parameter(property = "configurationNames", readonly = true, required = true)
    private List<String> configurationNames;

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
            validateReports(configReportMap);
            AggregateFlowReport report = new AggregateFlowReport(configurationNames,
                    configurationNames.stream().map(configReportMap::get).collect(Collectors.toList()),
                    plotNumbersOfEntities, tableNumberOfEntities);
            ReportManager reportManager = new ReportManager(report);
            reportManager.printBenchResultsTable();
            reportManager.printStudyResultsTable();
            reportManager.writeLatexResults(finalReportDirectory);
        } catch (IOException e) {
            throw new MojoFailureException("Failed to create or write flow report", e);
        }
    }

    private void validateReports(Map<String, FlowReport> configReportMap) throws MojoFailureException {
        if (!configReportMap.keySet().containsAll(configurationNames)) {
            throw new MojoFailureException("Missing reports for some configurations");
        }
        // Check that there is a report for every configuration for every evaluation
        Set<BenchInfo> benchmarks = configReportMap.values()
                .stream()
                .map(FlowReport::getBenchReports)
                .map(Map::keySet)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        Set<StudyInfo> studies = configReportMap.values()
                .stream()
                .map(FlowReport::getStudyReports)
                .map(Map::keySet)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        for (FlowReport report : configReportMap.values()) {
            if (!report.getBenchReports().keySet().containsAll(benchmarks)) {
                throw new MojoFailureException("Missing results for at least one benchmark");
            }
            if (!report.getStudyReports().keySet().containsAll(studies)) {
                throw new MojoFailureException("Missing results for at least one study");
            }
        }
        // Check that all of the benchmarks have entries for all of the plotNumbersOfEntities and tableNumberOfEntities
        // values
        for (FlowReport report : configReportMap.values()) {
            for (BenchInfo benchmark : benchmarks) {
                Map<Integer, RunResult> results = report.getBenchReports().get(benchmark);
                if (!results.keySet().containsAll(plotNumbersOfEntities)
                        || !results.containsKey(tableNumberOfEntities)) {
                    throw new MojoFailureException("Missing result for at least one required number of entities value");
                }
            }
        }
    }
}
