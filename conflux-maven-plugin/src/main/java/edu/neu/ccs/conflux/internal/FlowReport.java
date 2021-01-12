package edu.neu.ccs.conflux.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FlowReport {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .setLenient()
            .enableComplexMapKeySerialization()
            .create();
    private final Map<BenchInfo, Map<Integer, BenchRunResult>> benchReports = new HashMap<>();
    private final Map<StudyInfo, StudyRunResult> studyReports = new HashMap<>();

    public Map<BenchInfo, Map<Integer, BenchRunResult>> getBenchReports() {
        return Collections.unmodifiableMap(benchReports);
    }

    public Map<StudyInfo, StudyRunResult> getStudyReports() {
        return Collections.unmodifiableMap(studyReports);
    }

    public void addBenchReport(Class<?> testClass, Method testMethod, Map<Integer, BenchRunResult> result) {
        if (result == null) {
            throw new NullPointerException();
        }
        benchReports.put(new BenchInfo(testClass, testMethod), result);
    }

    public void addStudyReport(Class<?> testClass, Method testMethod, StudyRunResult result) {
        if (result == null) {
            throw new NullPointerException();
        }
        studyReports.put(new StudyInfo(testClass, testMethod), result);
    }

    public void addAll(FlowReport other) {
        for (BenchInfo bench : other.benchReports.keySet()) {
            if (this.benchReports.containsKey(bench)) {
                throw new IllegalArgumentException();
            }
        }
        for (StudyInfo study : other.studyReports.keySet()) {
            if (this.studyReports.containsKey(study)) {
                throw new IllegalArgumentException();
            }
        }
        for (BenchInfo bench : other.benchReports.keySet()) {
            this.benchReports.put(bench, other.benchReports.get(bench));
        }
        for (StudyInfo study : other.studyReports.keySet()) {
            this.studyReports.put(study, other.studyReports.get(study));
        }
    }

    public void writeToFile(File reportFile) throws FileNotFoundException {
        String json = gson.toJson(this);
        try (PrintWriter out = new PrintWriter(reportFile)) {
            out.println(json);
        }
    }

    public static FlowReport readFromFile(File reportFile) throws IOException {
        try (JsonReader reader = new JsonReader(new FileReader(reportFile))) {
            reader.setLenient(true);
            return gson.fromJson(reader, FlowReport.class);
        }
    }
}
