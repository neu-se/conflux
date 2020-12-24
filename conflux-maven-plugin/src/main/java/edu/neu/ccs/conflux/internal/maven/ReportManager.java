package edu.neu.ccs.conflux.internal.maven;

import edu.neu.ccs.conflux.internal.PlotStat;
import edu.neu.ccs.conflux.internal.TableStat;
import edu.neu.ccs.conflux.internal.report.BenchInfo;
import edu.neu.ccs.conflux.internal.report.StudyInfo;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static edu.gmu.swe.phosphor.ignored.maven.PhosphorInstrumentUtil.createOrCleanDirectory;

public class ReportManager {

    /**
     * Name of the directory used to store generated plots
     */
    private static final String PLOT_DIRECTORY_NAME = "flow-plots";
    private static final Comparator<StudyInfo> studyComparator = Comparator.comparing(StudyInfo::getProject)
            .thenComparing(StudyInfo::getIssue)
            .thenComparing(StudyInfo::getClassName)
            .thenComparing(StudyInfo::getMethodName);
    private static final Comparator<BenchInfo> benchComparator = Comparator.comparing(
            BenchInfo::getGroup)
            .thenComparing(BenchInfo::getProject)
            .thenComparing(BenchInfo::getImplementation)
            .thenComparing(BenchInfo::getClassName)
            .thenComparing(BenchInfo::getMethodName);
    private final AggregateFlowEvaluationReport report;

    ReportManager(List<String> configurationNames, List<File> reportFiles,
                  Collection<Integer> plotNumbersOfEntities, int tableNumberOfEntities) throws IOException {
        this.report = new AggregateFlowEvaluationReport(configurationNames, reportFiles,
                plotNumbersOfEntities, tableNumberOfEntities);
    }

    void writeLatexResults(File outputDir) throws IOException {
        File reportFile = new File(outputDir, "flow-report.tex");
        File tableFile = new File(outputDir, "flow-table.tex");
        File plotDir = new File(outputDir, PLOT_DIRECTORY_NAME);
        try (FileWriter writer = new FileWriter(tableFile)) {
            writer.write(createLatexTableString());
        }
        List<File> plotFiles = Collections.emptyList();
        if (!report.getPlotNumbersOfEntities().isEmpty()) {
            createOrCleanDirectory(plotDir);
            plotFiles = writePlots(plotDir);
        }
        writeReportFile(reportFile, tableFile, plotFiles);
    }

    private List<File> writePlots(File plotDir) {
        List<File> plotFiles = new LinkedList<>();
        for (PlotStat stat : report.getPlotStatistics()) {
            report.getBenchmarks()
                    .stream()
                    .sorted(benchComparator)
                    .map(bench -> writePlot(plotDir, stat, bench))
                    .forEach(plotFiles::add);
        }
        return plotFiles;
    }

    private File writePlot(File plotDir, PlotStat stat, BenchInfo bench) {
        String fileName = String.format("%s-%s-%s-%s-plot.tex",
                stat.name().replace(" ", ""),
                bench.getProject().replace(" ", ""),
                bench.getGroup().replace(" ", ""),
                bench.getImplementation().replace(" ", ""));
        File plotFile = new File(plotDir, fileName);
        try (FileWriter writer = new FileWriter(plotFile)) {
            writer.write("\\begin{tikzpicture}\n");
            writer.write("\t\\begin{axis}[title={" +
                    String.format("%s - %s (%s)", bench.getGroup(),
                            bench.getProject(), bench.getImplementation()) +
                    "}, legend pos=outer north east, " +
                    "xlabel={Number of Abstract Entities}, " +
                    "ylabel={" + stat.name() + "}]\n");
            for (String configurationName : report.getConfigurationNames()) {
                writePlotResult(writer, stat, configurationName, bench);
            }
            writer.write("\t\\end{axis}\n");
            writer.write("\\end{tikzpicture}");
        } catch (IOException e) {
            throw new RuntimeException("Failed to write plot to file", e);
        }
        return plotFile;
    }

    private void writePlotResult(Writer writer, PlotStat stat, String configurationName,
                                 BenchInfo bench) throws IOException {
        writer.write("\t\t\\addplot+[smooth] plot coordinates {\n");
        for (Integer numberOfEntities : report.getPlotNumbersOfEntities()) {
            Number value = report.getValue(configurationName, bench, numberOfEntities, stat)
                    .orElseThrow(RuntimeException::new);
            writer.write(String.format("\t\t\t(%s, %s)\n", numberOfEntities, value));
        }
        writer.write("\t\t};\n");
        writer.write("\t\t\\addlegendentry{" + configurationName + "}\n");
    }

    private String createLatexTableString() {
        StringWriter writer = new StringWriter();
        writeLatexTableHeader(writer);
        AtomicBoolean grey = new AtomicBoolean(false);
        report.getBenchmarks()
                .stream()
                .sequential()
                .map(BenchInfo::getGroup)
                .sorted()
                .forEach(group -> writeLatexTableGroup(grey, group, writer));
        writer.write("\\bottomrule\n");
        writer.write("\\end{tabular}\n");
        return writer.toString();
    }

    private void writeLatexTableGroup(AtomicBoolean grey, String group, StringWriter writer) {
        List<BenchInfo> benchmarks = report.getBenchmarks()
                .stream()
                .filter(b -> b.getGroup().equals(group))
                .sorted(benchComparator)
                .collect(Collectors.toList());
        for (int i = 0; i < benchmarks.size(); i++) {
            if (grey.get()) {
                writer.write("\\rowcolor{lightgrey}\n");
            }
            if (i == benchmarks.size() - 1) {
                writer.write(String.format("\\multirow{-%d}{*}{%s}", benchmarks.size(), group));
            } else {
                writer.write(" ");
            }
            writeLatexRowData(writer, benchmarks.get(i));
            writer.write(" \\\\\n");
        }
        grey.set(!grey.get());
    }

    private void writeLatexRowData(StringWriter writer, BenchInfo bench) {
        writer.write(String.format(" & %s & %s", bench.getProject(),
                bench.getImplementation()));
        for (String configurationName : report.getConfigurationNames()) {
            for (TableStat stat : report.getTableStatistics()) {
                String value = formatValue(report.getValue(configurationName, bench, report.getTableNumberOfEntities(),
                        stat));
                boolean emphasize = report.shouldEmphasizeTableStat(configurationName, bench, report.getTableNumberOfEntities(), stat);
                if (emphasize) {
                    writer.write(String.format(" & \\(\\color{purple}%s\\)", value));
                } else {
                    writer.write(String.format(" & \\(%s\\)", value));
                }
            }
        }
    }

    private void writeLatexTableHeader(StringWriter writer) {
        int statsPerConfig = report.getTableStatistics().size();
        writer.write("\\begin{tabular}{lll");
        for (int i = 0; i < report.getConfigurationNames().size(); i++) {
            writer.write(" ");
            for (int j = 0; j < statsPerConfig; j++) {
                writer.write("r");
            }
            if (i < report.getConfigurationNames().size() - 1) {
                writer.write(" |");
            }
        }
        writer.write("}\n");
        writer.write("\\toprule\n");
        writer.write("\\multicolumn{1}{c}{\\textbf{Test Group}} & \\multicolumn{1}{c}{\\textbf{Project}} & " +
                "\\multicolumn{1}{c}{\\textbf{Implementation}}");
        for (String configurationName : report.getConfigurationNames()) {
            writer.write(String.format(" & \\multicolumn{%d}{c}{\\textbf{", statsPerConfig));
            writer.write(configurationName);
            writer.write("}} ");
        }
        writer.write("\\\\\n");
        for (int i = 0; i < report.getConfigurationNames().size(); i++) {
            int start = 4 + statsPerConfig * i;
            int end = start + (statsPerConfig - 1);
            writer.write(String.format("\\cmidrule(lr){%d-%d}", start, end));
        }
        writer.write("\n & &");
        for (String ignored : report.getConfigurationNames()) {
            for (TableStat stat : report.getTableStatistics()) {
                writer.write(String.format(" & \\multicolumn{1}{c}{\\textbf{%s}}", stat.name()));
            }
        }
        writer.write("\\\\\n");
        writer.write("\\midrule\n");
    }

    public void printBenchResultsTable() {
        GroupedTable table = new GroupedTable("Flow Benchmark Results")
                .addGroup("", "Benchmark", "Test");
        String[] stats = report.getTableStatistics().stream().map(TableStat::name).toArray(String[]::new);
        for (String name : report.getConfigurationNames()) {
            table.addGroup(name, stats);
        }
        report.getBenchmarks()
                .stream()
                .sorted(benchComparator)
                .map(this::createBenchRow)
                .forEach(table::addRow);
        table.printToStream(System.out);
        System.out.printf("%n%n");
    }

    public void printStudyResultsTable() {
        GroupedTable table = new GroupedTable("Flow Study Results")
                .addGroup("", "Study", "Test");
        String[] stats = report.getTableStatistics().stream().map(TableStat::name).toArray(String[]::new);
        for (String name : report.getConfigurationNames()) {
            table.addGroup(name, stats);
        }
        report.getStudies()
                .stream()
                .sorted(studyComparator)
                .map(this::createStudyRow)
                .forEach(table::addRow);
        table.printToStream(System.out);
        System.out.printf("%n%n");
    }

    private Object[][] createBenchRow(BenchInfo bench) {
        Object[][] row = new Object[report.getConfigurationNames().size() + 1][];
        row[0] = new String[]{bench.getShortenedClassName(), bench.getMethodName()};
        int i = 1;
        for (String name : report.getConfigurationNames()) {
            row[i++] = report.getTableStatistics()
                    .stream()
                    .map(stat -> report.getValue(name, bench, report.getTableNumberOfEntities(), stat))
                    .map(ReportManager::formatValue)
                    .toArray();
        }
        return row;
    }

    private Object[][] createStudyRow(StudyInfo study) {
        Object[][] row = new Object[report.getConfigurationNames().size() + 1][];
        row[0] = new String[]{study.getShortenedClassName(), study.getMethodName()};
        int i = 1;
        for (String name : report.getConfigurationNames()) {
            row[i++] = report.getTableStatistics()
                    .stream()
                    .map(stat -> report.getValue(name, study, stat))
                    .map(ReportManager::formatValue)
                    .toArray();
        }
        return row;
    }

    private static void writeReportFile(File file, File tableFile, List<File> plotFiles) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("\\documentclass{article}\n");
            writer.write("\\usepackage{multirow}\n");
            writer.write("\\usepackage{multicol}\n");
            writer.write("\\usepackage{colortbl}\n");
            writer.write("\\usepackage{xcolor}\n");
            writer.write("\\usepackage{booktabs}\n");
            writer.write("\\usepackage{fullpage}\n");
            writer.write("\\definecolor{lightgrey}{rgb}{0.91,0.91,0.91}\n");
            writer.write("\\usepackage{pgfplots}\n");
            writer.write("\\pgfplotsset{compat=newest}\n");
            writer.write("\\usetikzlibrary{plotmarks}\n");
            writer.write("\\begin{document}\n");
            writer.write("\\title{Generated Results for Control Flow Evaluation}\n");
            writer.write("\\maketitle\n");
            writer.write("\\begin{table}[!htb]\n");
            writer.write("\t\\resizebox{\\textwidth}{!}{\\input{" + tableFile.getName() + "}}\n");
            writer.write("\\end{table}\n");
            for (File plotFile : plotFiles) {
                writer.write("\\input{" + PLOT_DIRECTORY_NAME + "/" + plotFile.getName() + "}\n\n");
            }
            writer.write("\\end{document}\n");
        }
    }

    private static String formatValue(@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                              Optional<Number> maybeValue) {
        if (maybeValue.isPresent()) {
            Number value = maybeValue.get();
            if (value instanceof Float) {
                return String.format("%,.4f", value);
            } else if (value instanceof Double) {
                return String.format("%,.4f", value);
            } else {
                return String.format("%,d", value.longValue());
            }
        } else {
            return "------";
        }
    }
}
