package edu.columbia.cs.psl.phosphor.maven;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import edu.columbia.cs.psl.phosphor.struct.IntSinglyLinkedList;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;

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
                .registerTypeAdapter(FlowBenchResult.class, new FlowBenchResultMarshaller())
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
                .registerTypeAdapter(FlowBenchResult.class, new FlowBenchResultMarshaller())
                .create();
        JsonReader reader = new JsonReader(new FileReader(reportFile));
        reader.setLenient(true);
        Type listOfFlowBenchReport = new TypeToken<List<FlowBenchReport>>(){}.getType();
        List<FlowBenchReport> reports = gson.fromJson(reader, listOfFlowBenchReport);
        reader.close();
        return reports;
    }

    protected static class FlowBenchResultMarshaller implements JsonSerializer<FlowBenchResult>, JsonDeserializer<FlowBenchResult> {

        private static final String CLASS_META_KEY = "_class";

        @Override
        public JsonElement serialize(FlowBenchResult src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject;
            if(src instanceof MultiLabelFlowBenchResult) {
                jsonObject = new JsonObject();
                JsonArray correctArray = new JsonArray();
                JsonArray expectedArray = new JsonArray();
                JsonArray predictedArray = new JsonArray();
                for(int i : ((MultiLabelFlowBenchResult) src).getNumCorrect()) {
                    correctArray.add(i);
                }
                for(int i : ((MultiLabelFlowBenchResult) src).getNumPredicted()) {
                    predictedArray.add(i);
                }
                for(int i : ((MultiLabelFlowBenchResult) src).getNumExpected()) {
                    expectedArray.add(i);
                }
                jsonObject.add("numCorrect", correctArray);
                jsonObject.add("numPredicted", predictedArray);
                jsonObject.add("numExpected", expectedArray);
            } else {
                jsonObject = context.serialize(src, src.getClass()).getAsJsonObject();
            }
            jsonObject.addProperty(CLASS_META_KEY, src.getClass().getCanonicalName());
            return jsonObject;
        }

        @Override
        public FlowBenchResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String className = jsonObject.get(CLASS_META_KEY).getAsString();
            try {
                Class<?> clz = Class.forName(className);
                if(clz.equals(MultiLabelFlowBenchResult.class)) {
                    MultiLabelFlowBenchResult result = new MultiLabelFlowBenchResult();
                    JsonArray correctArray = jsonObject.getAsJsonArray("numCorrect");
                    JsonArray predictedArray = jsonObject.getAsJsonArray("numPredicted");
                    JsonArray expectedArray = jsonObject.getAsJsonArray("numExpected");
                    correctArray.forEach(new JsonIntArrayConsumer(result.getNumCorrect()));
                    predictedArray.forEach(new JsonIntArrayConsumer(result.getNumPredicted()));
                    expectedArray.forEach(new JsonIntArrayConsumer(result.getNumExpected()));
                    return result;
                }
                return context.deserialize(json, clz);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }

        }
    }

    private static class JsonIntArrayConsumer implements Consumer<JsonElement> {

        IntSinglyLinkedList list;

        JsonIntArrayConsumer(IntSinglyLinkedList list) {
            this.list = list;
        }

        @Override
        public void accept(JsonElement jsonElement) {
            list.enqueue(jsonElement.getAsInt());
        }
    }
}

