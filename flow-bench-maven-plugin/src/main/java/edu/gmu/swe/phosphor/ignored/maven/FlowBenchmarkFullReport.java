package edu.gmu.swe.phosphor.ignored.maven;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResult;
import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import edu.gmu.swe.phosphor.ignored.runtime.PlotStat;
import edu.gmu.swe.phosphor.ignored.runtime.TableStat;
import edu.gmu.swe.util.GroupedTable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.*;

import static edu.gmu.swe.phosphor.ignored.maven.PhosphorInstrumentUtil.createOrCleanDirectory;

class FlowBenchmarkFullReport {

    /**
     * Name of directory used to store generated plots
     */
    private static final String PLOT_DIRECTORY_NAME = "flow-plots";

    /**
     * Mapping from annotation's name to methods annotated with the TableStat annotation from the FlowBenchResultImpl
     * class
     */
    private static final TreeMap<String, Method> tableStatMethods = getTableStatMethods();

    /**
     * Mapping from annotation's name to methods annotated with the PlotStat annotation from the FlowBenchResultImpl
     * class
     */
    private static final TreeMap<String, Method> plotStatMethods = getPlotStatMethods();

    /**
     * Names of the configurations that were benchmarked
     */
    private final List<String> configurationNames;

    /**
     * True if execution times should be reported in tables
     */
    private final boolean reportExecutionTime;

    /**
     * Lengths of tainted inputs to be used in the generated plots
     */
    private final TreeSet<Integer> plotNumbersOfEntities;

    /**
     * Length of tainted inputs to be used in the generated table
     */
    private final int tableNumberOfEntities;

    private final Set<TestReport> tests;

    FlowBenchmarkFullReport(List<String> configurationNames, List<File> reportFiles, boolean reportExecutionTime,
                            Set<Integer> plotNumbersOfEntities, int tableNumberOfEntities) throws IOException {
        this.configurationNames = configurationNames;
        this.reportExecutionTime = reportExecutionTime;
        this.plotNumbersOfEntities = new TreeSet<>(plotNumbersOfEntities);
        this.tableNumberOfEntities = tableNumberOfEntities;
        List<? extends List<FlowBenchReport>> reportLists = deserializeReports(reportFiles);
        tests = regroupByTest(reportLists);
        tests.forEach(TestReport::calculateTableEmphasis);
    }

    private Set<TestReport> regroupByTest(List<? extends List<FlowBenchReport>> reportLists) {
        Map<TestReport, TestReport> testMap = new TreeMap<>();
        Iterator<? extends List<FlowBenchReport>> reportsIt = reportLists.iterator();
        for(String name : configurationNames) {
            List<FlowBenchReport> reports = reportsIt.next();
            for(FlowBenchReport report : reports) {
                TestReport base = new TestReport(report);
                if(report.getResult() instanceof FlowBenchResultImpl) {
                    testMap.putIfAbsent(base, base);
                    testMap.get(base).add(new ConfigurationResult(name, report));
                }
            }
        }
        for(TestReport test : testMap.keySet()) {
            for(String name : configurationNames) {
                if(!test.results.containsKey(name)) {
                    // Missing results
                    test.add(new ConfigurationResult(name));
                }
            }
        }

        return testMap.keySet();
    }

    /**
     * Prints a table of benchmark results of the specified type for the various Phosphor configurations.
     */
    void printResultsTable() {
        if(!tests.isEmpty()) {
            List<String> statsPerConfig = new LinkedList<>();
            if(reportExecutionTime) {
                statsPerConfig.add("Time (ms)");
            }
            statsPerConfig.addAll(tableStatMethods.keySet());
            GroupedTable table = new GroupedTable("Flow Benchmark Results")
                    .addGroup("", "Benchmark", "Test");
            for(String name : configurationNames) {
                table.addGroup(name, statsPerConfig.toArray(new String[0]));
            }
            for(TestReport test : tests) {
                Object[][] row = new Object[configurationNames.size() + 1][];
                row[0] = new String[]{test.getShortClassName(), test.methodName};
                int i = 1;
                for(String name : configurationNames) {
                    List<String> rowData = new LinkedList<>();
                    ConfigurationResult result = test.results.get(name);
                    if(reportExecutionTime) {
                        rowData.add(result.timeElapsed.toString());
                    }
                    for(Object stat : result.tableStats) {
                        if(stat instanceof Float) {
                            rowData.add(String.format("%.4f", stat));
                        } else if(stat instanceof Double) {
                            rowData.add(String.format("%.4f", stat));
                        } else {
                            rowData.add(stat.toString());
                        }
                    }
                    row[i++] = rowData.toArray();
                }
                table.addRow(row);
            }
            table.printToStream(System.out);
            System.out.println("\n");
        }
    }

    void writeLatexResults(File outputDir) throws IOException {
        File reportFile = new File(outputDir, "flow-report.tex");
        File tableFile = new File(outputDir, "flow-table.tex");
        File plotDir = new File(outputDir, PLOT_DIRECTORY_NAME);
        writeLatexTable(tableFile);
        List<File> plotFiles = Collections.emptyList();
        if(!plotNumbersOfEntities.isEmpty()) {
            createOrCleanDirectory(plotDir);
            plotFiles = writePlots(plotDir);
        }
        writeReportFile(reportFile, tableFile, plotFiles);
    }

    private void writeReportFile(File file, File tableFile, List<File> plotFiles) throws IOException {
        try(FileWriter writer = new FileWriter(file)) {
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
            writer.write("\\title{Generated Results for Control Flow Benchmark}\n");
            writer.write("\\maketitle\n");
            writer.write("\\begin{table}[!htb]\n");
            writer.write("\t\\resizebox{\\textwidth}{!}{\\input{" + tableFile.getName() + "}}\n");
            writer.write("\\end{table}\n");
            for(File plotFile : plotFiles) {
                writer.write(plotFile.getName() + "\n\n");
                writer.write("\\input{" + PLOT_DIRECTORY_NAME + "/" + plotFile.getName() + "}\n\n");
            }
            writer.write("\\end{document}\n");
        }
    }

    private void writeLatexTable(File file) throws IOException {
        TreeMap<String, List<TestReport>> groupMap = groupTestReports();
        try(FileWriter writer = new FileWriter(file)) {
            writeLatexTableHeader(writer);
            boolean grey = false;
            for(String groupName : groupMap.keySet()) {
                List<TestReport> groupTests = groupMap.get(groupName);
                for(int i = 0; i < groupTests.size(); i++) {
                    if(grey) {
                        writer.write("\\rowcolor{lightgrey}\n");
                    }
                    if(i == groupTests.size() - 1) {
                        writer.write(String.format("\\multirow{-%d}{*}{%s}", groupTests.size(), groupName));
                    } else {
                        writer.write(" ");
                    }
                    writeLatexRowData(writer, groupTests.get(i));
                    writer.write(" \\\\\n");
                }
                grey = !grey;
            }
            writer.write("\\bottomrule\n");
            writer.write("\\end{tabular}\n");
        }
    }

    private void writeLatexRowData(Writer writer, TestReport data) throws IOException {
        writer.write(String.format(" & %s & %s", data.project, data.implementationDesc));
        for(String configurationName : configurationNames) {
            ConfigurationResult result = data.results.get(configurationName);
            if(reportExecutionTime) {
                writer.write(String.format(" & \\(%s\\)", result.timeElapsed));
            }
            for(int i = 0; i < result.tableStats.size(); i++) {
                Object stat = result.tableStats.get(i);
                boolean emphasize = result.emphasizeTableStat.get(i);
                String value;
                if(stat instanceof Integer) {
                    value = String.format("%,d", stat);
                } else if(stat instanceof Long) {
                    value = String.format("%,d", stat);
                } else if(stat instanceof Float) {
                    value = String.format("%,.2f", stat);
                } else if(stat instanceof Double) {
                    value = String.format("%,.2f", stat);
                } else {
                    value = stat.toString();
                }
                if(emphasize) {
                    writer.write(String.format(" & \\(\\color{purple}%s\\)", value));
                } else {
                    writer.write(String.format(" & \\(%s\\)", value));
                }
            }
        }
    }

    private void writeLatexTableHeader(Writer writer) throws IOException {
        int statsPerConfig = tableStatMethods.size();
        if(reportExecutionTime) {
            statsPerConfig++;
        }
        writer.write("\\begin{tabular}{lll");
        for(int i = 0; i < configurationNames.size(); i++) {
            writer.write(" ");
            for(int j = 0; j < statsPerConfig; j++) {
                writer.write("r");
            }
            if(i < configurationNames.size() - 1) {
                writer.write(" |");
            }
        }
        writer.write("}\n");
        writer.write("\\toprule\n");
        writer.write("\\multicolumn{1}{c}{\\textbf{Test Group}} & \\multicolumn{1}{c}{\\textbf{Project}} & " +
                "\\multicolumn{1}{c}{\\textbf{Implementation}}");

        for(String configurationName : configurationNames) {
            writer.write(String.format(" & \\multicolumn{%d}{c}{\\textbf{", statsPerConfig));
            writer.write(configurationName);
            writer.write("}} ");
        }
        writer.write("\\\\\n");
        for(int i = 0; i < configurationNames.size(); i++) {
            int start = 4 + statsPerConfig * i;
            int end = start + (statsPerConfig - 1);
            writer.write(String.format("\\cmidrule(lr){%d-%d}", start, end));
        }
        writer.write("\n & &");
        for(String ignored : configurationNames) {
            if(reportExecutionTime) {
                writer.write(" & \\multicolumn{1}{c}{\\textbf{Time (ms)}}");
            }
            for(String stat : tableStatMethods.keySet()) {
                writer.write(String.format(" & \\multicolumn{1}{c}{\\textbf{%s}}", stat));
            }
        }
        writer.write("\\\\\n");
        writer.write("\\midrule\n");
    }

    private TreeMap<String, List<TestReport>> groupTestReports() {
        TreeMap<String, List<TestReport>> groupMap = new TreeMap<>();
        for(TestReport test : tests) {
            groupMap.putIfAbsent(test.group, new ArrayList<>());
            groupMap.get(test.group).add(test);
        }
        return groupMap;
    }

    private List<File> writePlots(File plotDir) throws IOException {
        List<File> plotFiles = new LinkedList<>();
        for(String statName : plotStatMethods.keySet()) {
            String simpleStatName = statName.replace(" ", "");
            for(TestReport test : tests) {
                String fileName = String.format("%s-%s-%s-plot.tex", simpleStatName,
                        test.className.replace(" ", ""), test.group.replace(" ", ""));
                File file = new File(plotDir, fileName);
                plotFiles.add(file);
                try(FileWriter writer = new FileWriter(file)) {
                    writer.write("\\begin{tikzpicture}\n");
                    writer.write("\t\\begin{axis}[title={}, legend pos=outer north east, " +
                            "xlabel={Number of Syntactic Entities}, " +
                            "ylabel={" + statName + "}]\n");
                    for(String configurationName : configurationNames) {
                        writePlotResult(writer, statName, test.results.get(configurationName));
                    }
                    writer.write("\t\\end{axis}\n");
                    writer.write("\\end{tikzpicture}");
                }
            }
        }
        return plotFiles;
    }

    private void writePlotResult(Writer writer, String statName, ConfigurationResult result) throws IOException {
        if(result.plotStats.containsKey(statName)) {
            writer.write("\t\t\\addplot+[smooth] plot coordinates {\n");
            Map<Integer, Object> valueMap = result.plotStats.get(statName);
            for(Integer numberOfEntities : valueMap.keySet()) {
                Object value = valueMap.get(numberOfEntities);
                if(value instanceof Number) {
                    writer.write(String.format("\t\t\t(%s, %s)\n", numberOfEntities, value));
                }
            }
            writer.write("\t\t};\n");
            writer.write("\t\t\\addlegendentry{" + result.configurationName + "})\n");
        }
    }

    private static TreeMap<String, Method> getTableStatMethods() {
        TreeMap<String, Method> statMethods = new TreeMap<>();
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

    private static TreeMap<String, Method> getPlotStatMethods() {
        TreeMap<String, Method> statMethods = new TreeMap<>();
        for(Class<?> clazz = FlowBenchResultImpl.class; clazz != FlowBenchResult.class; clazz = clazz.getSuperclass()) {
            for(Method method : FlowBenchResultImpl.class.getDeclaredMethods()) {
                if(method.isAnnotationPresent(PlotStat.class)) {
                    String statName = method.getAnnotation(PlotStat.class).name();
                    statMethods.put(statName, method);
                }
            }
        }
        return statMethods;
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
    private static List<? extends List<FlowBenchReport>> deserializeReports(List<File> reportFiles) throws IOException {
        List<List<FlowBenchReport>> reports = new LinkedList<>();
        for(File reportFile : reportFiles) {
            reports.add(FlowBenchReport.readJsonFromFile(reportFile));
        }
        return reports;
    }

    private final class ConfigurationResult {
        private final String configurationName;
        private final Object timeElapsed;
        private final List<Object> tableStats = new LinkedList<>();
        private final List<Boolean> emphasizeTableStat = new LinkedList<>();
        private final Map<String, TreeMap<Integer, Object>> plotStats = new HashMap<>();

        private ConfigurationResult(String configurationName) {
            this.configurationName = configurationName;
            timeElapsed = "Error";
            tableStatMethods.forEach((k, v) -> tableStats.add("Error"));
            tableStats.forEach(s -> emphasizeTableStat.add(false));
        }

        private ConfigurationResult(String configurationName, FlowBenchReport report) {
            if(!(report.getResult() instanceof FlowBenchResultImpl)) {
                throw new IllegalArgumentException();
            }
            this.configurationName = configurationName;
            timeElapsed = report.getTimeElapsed();
            tableStatMethods.forEach((k, v) -> {
                v.setAccessible(true);
                try {
                    Object value = v.invoke(report.getResult(), tableNumberOfEntities);
                    tableStats.add(value);
                } catch(Exception e) {
                    tableStats.add("------");
                }
            });
            tableStats.forEach(s -> emphasizeTableStat.add(false));
            plotStatMethods.forEach((k, v) -> {
                v.setAccessible(true);
                TreeMap<Integer, Object> valueMap = new TreeMap<>();
                plotStats.put(k, valueMap);
                for(int plotNumberOfEntities : plotNumbersOfEntities) {
                    try {
                        Object value = v.invoke(report.getResult(), plotNumberOfEntities);
                        valueMap.put(plotNumberOfEntities, value);
                    } catch(Exception e) {
                        valueMap.put(plotNumberOfEntities, "------");
                    }
                }
            });
        }
    }

    private static final class TestReport implements Comparable<TestReport> {
        private final String className;
        private final String methodName;
        private final String implementationDesc;
        private final String project;
        private final String group;
        private final Map<String, ConfigurationResult> results = new HashMap<>();

        private TestReport(FlowBenchReport report) {
            if(!(report.getResult() instanceof FlowBenchResultImpl)) {
                throw new IllegalArgumentException();
            }
            className = report.getSimpleClassName();
            methodName = report.getMethodName();
            implementationDesc = report.getImplementationDesc();
            project = report.getProject();
            group = report.getGroup();
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) {
                return true;
            } else if(!(o instanceof TestReport)) {
                return false;
            }
            TestReport that = (TestReport) o;
            if(!className.equals(that.className)) {
                return false;
            }
            if(!methodName.equals(that.methodName)) {
                return false;
            }
            if(!implementationDesc.equals(that.implementationDesc)) {
                return false;
            }
            if(!project.equals(that.project)) {
                return false;
            }
            return group.equals(that.group);
        }

        @Override
        public int hashCode() {
            int result = className.hashCode();
            result = 31 * result + methodName.hashCode();
            result = 31 * result + implementationDesc.hashCode();
            result = 31 * result + project.hashCode();
            result = 31 * result + group.hashCode();
            return result;
        }

        private void add(ConfigurationResult configurationResult) {
            results.put(configurationResult.configurationName, configurationResult);
        }

        private String getShortClassName() {
            String result = className;
            if(result.startsWith("FlowBench")) {
                result = result.substring("FlowBench".length());
            }
            if(result.endsWith("FlowBench")) {
                result = result.substring(0, result.length() - "FlowBench".length());
            }
            return result;
        }

        @Override
        public int compareTo(TestReport o) {
            int c = group.compareTo(o.group);
            if(c != 0) {
                return c;
            }
            c = project.compareTo(o.project);
            if(c != 0) {
                return c;
            }
            c = implementationDesc.compareTo(o.implementationDesc);
            if(c != 0) {
                return c;
            }
            c = className.compareTo(o.className);
            if(c != 0) {
                return c;
            }
            return methodName.compareTo(o.methodName);
        }

        private void calculateTableEmphasis() {
            int i = 0;
            for(Method m : tableStatMethods.values()) {
                TableStat annotation = m.getAnnotation(TableStat.class);
                if(annotation.emphasizeMax()) {
                    Double max = null;
                    for(ConfigurationResult result : results.values()) {
                        Object stat = result.tableStats.get(i);
                        if(stat instanceof Number) {
                            Double value = ((Number) stat).doubleValue();
                            if(max == null || value.compareTo(max) > 0) {
                                max = value;
                            }
                        }
                    }
                    for(ConfigurationResult result : results.values()) {
                        Object stat = result.tableStats.get(i);
                        if(stat instanceof Number) {
                            Double value = ((Number) stat).doubleValue();
                            if(max != null && value.compareTo(max) == 0) {
                                result.emphasizeTableStat.set(i, true);
                            }
                        }
                    }
                }
                i++;
            }
        }
    }
}
