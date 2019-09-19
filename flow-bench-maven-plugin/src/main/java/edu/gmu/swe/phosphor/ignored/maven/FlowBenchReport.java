package edu.gmu.swe.phosphor.ignored.maven;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

public class FlowBenchReport {

    private final String className;
    private final String methodName;
    private final long timeElapsed;
    private final FlowBenchResult result;

    public FlowBenchReport(String className, String methodName, long timeElapsed, FlowBenchResult result) {
        this.className = className;
        this.methodName = methodName;
        this.timeElapsed = timeElapsed;
        this.result = result;
    }

    public FlowBenchReport(Method benchMethod, long timeElapsed, FlowBenchResult result) {
        this(benchMethod.getDeclaringClass().getName(), benchMethod.getName(), timeElapsed, result);
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
        } catch (FileNotFoundException e) {
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
        Type listOfFlowBenchReport = new TypeToken<List<FlowBenchReport>>(){}.getType();
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
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        }
    }
}

