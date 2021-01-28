package edu.neu.ccs.conflux.internal.maven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.neu.ccs.conflux.internal.StudyInfo;
import edu.neu.ccs.conflux.internal.StudyRunResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class AggregateFlowStudyReport {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .setLenient()
            .enableComplexMapKeySerialization()
            .create();
    private final Map<StudyInfo, AggregateStudyResult> studies;

    AggregateFlowStudyReport(AggregateFlowReport report) {
        // Check that at least one configuration was run
        if (report.getConfigurationNames().isEmpty()) {
            throw new IllegalArgumentException();
        }
        // Check that the input for each study is the same across configurations
        for (StudyInfo study : report.getStudies()) {
            long distinctInputs = report.getConfigurationNames()
                    .stream()
                    .map(n -> getInput(report, n, study))
                    .distinct()
                    .count();
            if (distinctInputs != 1) {
                throw new IllegalArgumentException("The input for a particular study should be the same for all " +
                        "configurations tested");
            }
        }
        this.studies = report.getStudies()
                .stream()
                .sorted(ReportManager.studyComparator)
                .collect(Collectors.toMap(Function.identity(),
                        study -> new AggregateStudyResult(report, study),
                        (o1, o2) -> o1, LinkedHashMap::new));
    }

    public void writeToFile(File reportFile) throws FileNotFoundException {
        String json = gson.toJson(this);
        try (PrintWriter out = new PrintWriter(reportFile)) {
            out.println(json);
        }
    }

    private static String getInput(AggregateFlowReport report, String configurationName, StudyInfo study) {
        return report.getResult(configurationName, study)
                .map(StudyRunResult::getInput)
                .orElseThrow(IllegalArgumentException::new);
    }

    private static final class AggregateStudyResult {
        String input;
        Map<String, boolean[]> configurationPredictionsMap = new LinkedHashMap<>();

        private AggregateStudyResult(AggregateFlowReport report, StudyInfo study) {
            this.input = getInput(report, report.getConfigurationNames().get(0), study);
            for (String configurationName : report.getConfigurationNames()) {
                boolean[] predictions = new boolean[input.length()];
                StudyRunResult result = report.getResult(configurationName, study)
                        .orElseThrow(IllegalArgumentException::new);
                for (int r : result.getPredicted()) {
                    predictions[r] = true;
                }
                configurationPredictionsMap.put(configurationName, predictions);
            }
        }
    }
}
