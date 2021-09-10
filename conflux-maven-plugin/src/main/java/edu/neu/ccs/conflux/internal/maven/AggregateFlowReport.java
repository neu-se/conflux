package edu.neu.ccs.conflux.internal.maven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.neu.ccs.conflux.internal.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

final class AggregateFlowReport {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .setLenient()
            .create();
    private final List<StudyResult> studies;
    private final List<BenchResult> benchmarks;

    public AggregateFlowReport(Map<String, FlowReport> configReportMap) {
        studies = extractStudies(configReportMap);
        studies.sort(Comparator.comparing(StudyResult::getProject).thenComparing(StudyResult::getIssue));
        for (StudyResult study : studies) {
            study.policies.sort(Comparator.comparing(StudyPolicyResult::getPolicy));
        }
        benchmarks = extractBenchmarks(configReportMap);
        benchmarks.sort(Comparator.comparing(BenchResult::getGroup).thenComparing(BenchResult::getProject)
                .thenComparing(BenchResult::getImplementation));
        for (BenchResult benchmark : benchmarks) {
            benchmark.policies.sort(Comparator.comparing(BenchPolicyResult::getPolicy));
        }
    }

    public void writeToFile(File reportFile) throws FileNotFoundException {
        String json = gson.toJson(this);
        try (PrintWriter out = new PrintWriter(reportFile)) {
            out.println(json);
        }
    }

    private static List<BenchResult> extractBenchmarks(Map<String, FlowReport> configReportMap) {
        Map<BenchInfo, BenchResult> benchMap = new HashMap<>();
        for (FlowReport report : configReportMap.values()) {
            for (BenchInfo benchInfo : report.getBenchReports().keySet()) {
                benchMap.put(benchInfo, new BenchResult(benchInfo.getGroup(), benchInfo.getImplementation(),
                        benchInfo.getProject()));
            }
        }
        for (String configuration : configReportMap.keySet()) {
            FlowReport report = configReportMap.get(configuration);
            for (BenchInfo benchInfo : report.getBenchReports().keySet()) {
                Map<Integer, BenchRunResult> runResults = report.getBenchReports().get(benchInfo);
                BenchPolicyResult entry = new BenchPolicyResult(configuration, runResults);
                benchMap.get(benchInfo).policies.add(entry);
            }
        }
        return new ArrayList<>(benchMap.values());
    }

    private static List<StudyResult> extractStudies(Map<String, FlowReport> configReportMap) {
        Map<StudyInfo, StudyResult> studyMap = new HashMap<>();
        for (FlowReport report : configReportMap.values()) {
            for (StudyInfo study : report.getStudyReports().keySet()) {
                String input = report.getStudyReports().get(study).getInput();
                if (!studyMap.containsKey(study)) {
                    studyMap.put(study, new StudyResult(study.getProject(), study.getIssue(), input));
                } else if (!studyMap.get(study).getInput().equals(input)) {
                    throw new IllegalArgumentException("Same study performed with different inputs");
                }
            }
        }
        for (String configuration : configReportMap.keySet()) {
            FlowReport report = configReportMap.get(configuration);
            for (StudyInfo studyInfo : report.getStudyReports().keySet()) {
                StudyRunResult runResult = report.getStudyReports().get(studyInfo);
                StudyPolicyResult entry = new StudyPolicyResult(configuration, runResult.getPredicted());
                studyMap.get(studyInfo).policies.add(entry);
            }
        }
        return new ArrayList<>(studyMap.values());
    }

    private static final class BenchResult {
        private final String group;
        private final String implementation;
        private final String project;
        private final List<BenchPolicyResult> policies = new LinkedList<>();

        private BenchResult(String group, String implementation, String project) {
            this.group = group;
            this.implementation = implementation;
            this.project = project;
        }

        public String getGroup() {
            return group;
        }

        public String getImplementation() {
            return implementation;
        }

        public String getProject() {
            return project;
        }
    }

    private static final class BenchPolicyResult {
        private final String policy;
        private final List<BenchTrialResult> trials = new LinkedList<>();

        private BenchPolicyResult(String policy, Map<Integer, BenchRunResult> runResults) {
            this.policy = policy;
            for (int numberOfEntities : runResults.keySet()) {
                trials.add(new BenchTrialResult(numberOfEntities, runResults.get(numberOfEntities)));
            }
            trials.sort(Comparator.comparing(BenchTrialResult::getNumberOfEntities));
        }

        public String getPolicy() {
            return policy;
        }
    }

    private static final class BenchTrialResult {
        private final int numberOfEntities;
        private final int truePositives;
        private final int falsePositives;
        private final int falseNegatives;

        public BenchTrialResult(int numberOfEntities, BenchRunResult benchRunResult) {
            this.numberOfEntities = numberOfEntities;
            this.truePositives = benchRunResult.truePositives();
            this.falsePositives = benchRunResult.falsePositives();
            this.falseNegatives = benchRunResult.falseNegatives();
        }

        public int getNumberOfEntities() {
            return numberOfEntities;
        }
    }

    private static final class StudyResult {
        private final String project;
        private final String issue;
        private final String input;
        private final List<StudyPolicyResult> policies = new LinkedList<>();

        private StudyResult(String project, String issue, String input) {
            this.project = project;
            this.issue = issue;
            this.input = input;
        }

        public String getProject() {
            return project;
        }

        public String getIssue() {
            return issue;
        }

        public String getInput() {
            return input;
        }
    }

    private static final class StudyPolicyResult {
        private final String policy;
        private final SortedSet<Integer> predictedIndices = new TreeSet<>();

        private StudyPolicyResult(String policy, int[] predictedIndices) {
            this.policy = policy;
            for (int i : predictedIndices) {
                this.predictedIndices.add(i);
            }
        }

        public String getPolicy() {
            return policy;
        }
    }
}
