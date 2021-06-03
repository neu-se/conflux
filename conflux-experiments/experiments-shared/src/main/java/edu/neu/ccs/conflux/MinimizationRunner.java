package edu.neu.ccs.conflux;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public abstract class MinimizationRunner {

    private final Class<? extends Throwable> targetException;
    private final StackTraceElement targetElement;
    private final int targetIndex;

    public MinimizationRunner(Class<? extends Throwable> targetException, StackTraceElement targetElement, int targetIndex) {
        this.targetException = targetException;
        this.targetElement = targetElement;
        this.targetIndex = targetIndex;
    }

    public void run(File file) throws IOException {
        String result = "PASS";
        try {
            String input = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            test(input);
        } catch (Throwable t) {
            StackTraceElement e = t.getStackTrace()[targetIndex];
            result = t.getClass().equals(targetException) && e.equals(targetElement) ? "FAIL" : "UNRESOLVED";
        }
        writeResult(file, result);
    }

    protected abstract void test(String input) throws Throwable;

    private static void writeResult(File file, String result) throws IOException {
        Files.write(file.toPath(), result.getBytes(StandardCharsets.UTF_8));
    }
}
