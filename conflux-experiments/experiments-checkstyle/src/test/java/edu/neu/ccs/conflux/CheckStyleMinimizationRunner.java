package edu.neu.ccs.conflux;

import com.puppycrawl.tools.checkstyle.api.FileText;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static edu.neu.ccs.conflux.CheckstyleFlowStudy.check;
import static edu.neu.ccs.conflux.CheckstyleFlowStudy.copyResourceToTempFile;

public class CheckStyleMinimizationRunner extends MinimizationRunner {

    public CheckStyleMinimizationRunner() {
        super(NullPointerException.class, new StackTraceElement(
                "com.puppycrawl.tools.checkstyle.checks.coding.FinalLocalVariableCheck",
                "updateUninitializedVariables",
                "FinalLocalVariableCheck.java",
                482
        ), 0);
    }

    @Override
    protected void test(String input) throws Throwable {
        File inputFile = copyResourceToTempFile(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        FileText text = new FileText(inputFile.getAbsoluteFile(), Arrays.asList(input.split("\n")));
        check(inputFile, text);
    }

    public static void main(String[] arguments) throws IOException {
        new CheckStyleMinimizationRunner().run(new File(arguments[0]));
        System.exit(0);
    }
}
