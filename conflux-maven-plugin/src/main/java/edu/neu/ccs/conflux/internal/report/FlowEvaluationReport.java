package edu.neu.ccs.conflux.internal.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import edu.neu.ccs.conflux.internal.RunResult;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FlowEvaluationReport {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .setLenient()
            .enableComplexMapKeySerialization()
            .create();
    private final Map<BenchInfo, Map<Integer, RunResult>> benchReports = new HashMap<>();
    private final Map<StudyInfo, RunResult> studyReports = new HashMap<>();

    public Map<BenchInfo, Map<Integer, RunResult>> getBenchReports() {
        return Collections.unmodifiableMap(benchReports);
    }

    public Map<StudyInfo, RunResult> getStudyReports() {
        return Collections.unmodifiableMap(studyReports);
    }

    public void addBenchReport(Class<?> testClass, Method testMethod, Map<Integer, RunResult> result) {
        if (result == null) {
            throw new NullPointerException();
        }
        benchReports.put(new BenchInfo(testClass, testMethod), result);
    }

    public void addStudyReport(Class<?> testClass, Method testMethod, RunResult result) {
        if (result == null) {
            throw new NullPointerException();
        }
        studyReports.put(new StudyInfo(testClass, testMethod), result);
    }

    public void writeToFile(File reportFile) throws FileNotFoundException {
        String json = gson.toJson(this);
        try (PrintWriter out = new PrintWriter(reportFile)) {
            out.println(json);
        }
    }

    public static FlowEvaluationReport readFromFile(File reportFile) throws IOException {
        try (JsonReader reader = new JsonReader(new FileReader(reportFile))) {
            reader.setLenient(true);
            return gson.fromJson(reader, FlowEvaluationReport.class);
        }
    }
}
