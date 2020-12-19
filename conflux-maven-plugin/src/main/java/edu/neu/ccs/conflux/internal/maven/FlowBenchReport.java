package edu.neu.ccs.conflux.internal.maven;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import edu.neu.ccs.conflux.internal.runtime.ErrorFlowBenchResult;
import edu.neu.ccs.conflux.internal.runtime.FlowBenchResult;
import edu.neu.ccs.conflux.FlowBench;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

public final class FlowBenchReport {

    private final String className;
    private final String methodName;
    private final long timeElapsed;
    private final FlowBenchResult result;
    private final String implementationDesc;
    private final String project;
    private final String group;

    public FlowBenchReport(String className, String methodName, long timeElapsed, ErrorFlowBenchResult result) {
        if(className == null || methodName == null || result == null) {
            throw new NullPointerException();
        }
        this.className = className;
        this.methodName = methodName;
        this.timeElapsed = timeElapsed;
        this.result = result;
        implementationDesc = "";
        project = "";
        group = "";
    }

    public FlowBenchReport(Method benchMethod, long timeElapsed, FlowBenchResult result) {
        if(!benchMethod.isAnnotationPresent(FlowBench.class)) {
            throw new IllegalArgumentException("Cannot make report for a method lacking a FlowBench annotation");
        }
        if(result == null) {
            throw new NullPointerException();
        }
        FlowBench annotation = benchMethod.getAnnotation(FlowBench.class);
        implementationDesc = annotation.implementation();
        project = annotation.project();
        group = annotation.group();
        className = benchMethod.getDeclaringClass().getName();
        methodName = benchMethod.getName();
        this.timeElapsed = timeElapsed;
        this.result = result;
    }

    public String getImplementationDesc() {
        return implementationDesc;
    }

    public String getProject() {
        return project;
    }

    public String getGroup() {
        return group;
    }

    public String getSimpleClassName() {
        if(className.contains(".")) {
            String[] split = className.split("[.]");
            return split[split.length - 1];
        } else {
            return className;
        }
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public long getTimeElapsed() {
        return timeElapsed;
    }

    public FlowBenchResult getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "FlowBenchReport{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", timeElapsed=" + timeElapsed +
                ", result=" + result +
                ", implementationDesc='" + implementationDesc + '\'' +
                ", project='" + project + '\'' +
                ", testName='" + group + '\'' +
                '}';
    }

    protected static void writeJsonToFile(List<FlowBenchReport> reports, File reportFile) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .setLenient()
                .registerTypeAdapter(FlowBenchResult.class, new SubclassMarshaller<FlowBenchResult>())
                .create();
        String json = gson.toJson(reports);
        try {
            PrintWriter out = new PrintWriter(reportFile);
            out.println(json);
            out.close();
        } catch(FileNotFoundException e) {
            System.out.println("Failed to write benchmark report to: " + reportFile);
            e.printStackTrace();
        }
    }

    protected static List<FlowBenchReport> readJsonFromFile(File reportFile) throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .setLenient()
                .registerTypeAdapter(FlowBenchResult.class, new SubclassMarshaller<FlowBenchResult>())
                .create();
        JsonReader reader = new JsonReader(new FileReader(reportFile));
        reader.setLenient(true);
        Type listOfFlowBenchReport = new TypeToken<List<FlowBenchReport>>() {
        }.getType();
        List<FlowBenchReport> reports = gson.fromJson(reader, listOfFlowBenchReport);
        reader.close();
        return reports;
    }

    protected static class SubclassMarshaller<T> implements JsonSerializer<T>, JsonDeserializer<T> {

        private static final String CLASS_META_KEY = "_class";

        @Override
        public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject;
            jsonObject = context.serialize(src, src.getClass()).getAsJsonObject();
            jsonObject.addProperty(CLASS_META_KEY, src.getClass().getCanonicalName());
            return jsonObject;
        }

        @Override
        public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String className = jsonObject.get(CLASS_META_KEY).getAsString();
            try {
                Class<?> clz = Class.forName(className);
                return context.deserialize(json, clz);
            } catch(ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        }
    }
}

