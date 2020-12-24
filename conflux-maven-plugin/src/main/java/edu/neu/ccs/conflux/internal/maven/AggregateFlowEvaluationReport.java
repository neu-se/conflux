package edu.neu.ccs.conflux.internal.maven;

import edu.neu.ccs.conflux.internal.PlotStat;
import edu.neu.ccs.conflux.internal.RunResult;
import edu.neu.ccs.conflux.internal.TableStat;
import edu.neu.ccs.conflux.internal.report.BenchInfo;
import edu.neu.ccs.conflux.internal.report.FlowEvaluationReport;
import edu.neu.ccs.conflux.internal.report.StudyInfo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AggregateFlowEvaluationReport {

    private static final SortedMap<TableStat, Method> tableStatMap = Arrays.stream(RunResult.class.getDeclaredMethods())
            .filter(m -> m.isAnnotationPresent(TableStat.class))
            .collect(Collectors.toMap(m -> m.getAnnotation(TableStat.class), Function.identity(), (e, r) -> e,
                    () -> new TreeMap<>(Comparator.comparing(TableStat::name))));
    private static final SortedMap<PlotStat, Method> plotStatMap = Arrays.stream(RunResult.class.getDeclaredMethods())
            .filter(m -> m.isAnnotationPresent(PlotStat.class))
            .collect(Collectors.toMap(m -> m.getAnnotation(PlotStat.class), Function.identity(), (e, r) -> e,
                    () -> new TreeMap<>(Comparator.comparing(PlotStat::name))));
    /**
     * The names of the configurations that were evaluated.
     */
    private final List<String> configurationNames;
    /**
     * Maps the names of the configurations to their evaluation reports.
     */
    private final Map<String, FlowEvaluationReport> configurationReportMap = new HashMap<>();
    /**
     * Lengths of tainted inputs to be used in the generated plots for benchmarks.
     */
    private final SortedSet<Integer> plotNumbersOfEntities;
    /**
     * Length of tainted inputs to be used in the generated table for benchmarks.
     */
    private final int tableNumberOfEntities;

    AggregateFlowEvaluationReport(List<String> configurationNames, List<File> reportFiles,
                                  Collection<Integer> plotNumbersOfEntities,
                                  int tableNumberOfEntities) throws IOException {
        this.plotNumbersOfEntities = Collections.unmodifiableSortedSet(new TreeSet<>(plotNumbersOfEntities));
        this.tableNumberOfEntities = tableNumberOfEntities;
        if (configurationNames.size() != reportFiles.size()) {
            throw new IllegalArgumentException();
        }
        this.configurationNames = Collections.unmodifiableList(new ArrayList<>(configurationNames));
        Iterator<String> itr1 = this.configurationNames.iterator();
        Iterator<File> itr2 = reportFiles.iterator();
        while (itr1.hasNext()) {
            configurationReportMap.put(itr1.next(),
                    FlowEvaluationReport.readFromFile(itr2.next()));
        }
    }

    public List<String> getConfigurationNames() {
        return configurationNames;
    }

    public SortedSet<Integer> getPlotNumbersOfEntities() {
        return plotNumbersOfEntities;
    }

    public int getTableNumberOfEntities() {
        return tableNumberOfEntities;
    }

    public Set<StudyInfo> getStudies() {
        return configurationReportMap.values()
                .stream()
                .map(FlowEvaluationReport::getStudyReports)
                .map(Map::keySet)
                .collect(HashSet::new, Set::addAll, Set::addAll);
    }

    public Set<BenchInfo> getBenchmarks() {
        return configurationReportMap.values()
                .stream()
                .map(FlowEvaluationReport::getBenchReports)
                .map(Map::keySet)
                .collect(HashSet::new, Set::addAll, Set::addAll);
    }

    public SortedSet<TableStat> getTableStatistics() {
        SortedSet<TableStat> set = new TreeSet<>(Comparator.comparing(TableStat::name));
        set.addAll(tableStatMap.keySet());
        return set;
    }

    public SortedSet<PlotStat> getPlotStatistics() {
        SortedSet<PlotStat> set = new TreeSet<>(Comparator.comparing(PlotStat::name));
        set.addAll(plotStatMap.keySet());
        return set;
    }

    public Optional<Number> getValue(String configurationName, StudyInfo study, TableStat stat) {
        return getResult(configurationName, study)
                .map(r -> getValue(r, stat));
    }

    public Optional<Number> getValue(String configurationName, BenchInfo bench, int numberOfEntities,
                                     TableStat stat) {
        return getResult(configurationName, bench, numberOfEntities)
                .map(r -> getValue(r, stat));

    }

    public Optional<Number> getValue(String configurationName, BenchInfo bench, int numberOfEntities,
                                     PlotStat stat) {
        return getResult(configurationName, bench, numberOfEntities)
                .map(r -> getValue(r, stat));
    }

    private Optional<RunResult> getResult(String configurationName, StudyInfo study) {
        return Optional.ofNullable(configurationReportMap.get(configurationName).getStudyReports().get(study));
    }

    private Optional<RunResult> getResult(String configurationName, BenchInfo bench, int numberOfEntities) {
        return Optional.ofNullable(configurationReportMap.get(configurationName).getBenchReports().get(bench))
                .map(m -> m.get(numberOfEntities));
    }

    public boolean shouldEmphasizeTableStat(String configurationName, BenchInfo bench,
                                            int numberOfEntities, TableStat stat) {
        if (!stat.emphasizeMax()) {
            return false;
        }
        Optional<Number> maybeValue = getValue(configurationName, bench, numberOfEntities, stat);
        if (!maybeValue.isPresent()) {
            return false;
        }
        Number value = maybeValue.get();
        for (String c : getConfigurationNames()) {
            if (!c.equals(configurationName)) {
                if (getValue(c, bench, numberOfEntities, stat)
                        .map(v -> v.doubleValue() > value.doubleValue() || v.longValue() > value.longValue())
                        .orElse(false)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Number getValue(RunResult result, TableStat stat) {
        try {
            return (Number) tableStatMap.get(stat).invoke(result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException();
        }
    }

    private static Number getValue(RunResult result, PlotStat stat) {
        try {
            return (Number) plotStatMap.get(stat).invoke(result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException();
        }
    }
}
