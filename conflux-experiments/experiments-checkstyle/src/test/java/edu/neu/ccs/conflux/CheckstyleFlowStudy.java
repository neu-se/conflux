package edu.neu.ccs.conflux;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.PackageObjectFactory;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.checks.coding.FinalLocalVariableCheck;
import edu.neu.ccs.conflux.internal.FlowStudy;
import edu.neu.ccs.conflux.internal.runtime.StudyTaintTagChecker;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

public class CheckstyleFlowStudy {

    /**
     * Issue: https://github.com/checkstyle/checkstyle/issues/8934
     * <p>
     * Fix: https://github.com/checkstyle/checkstyle/commit/70c7ae0e1866074530a49c983d015936a0c2c10f
     */
    @FlowStudy(project = "checkstyle", issue = "8934")
    public void issue8934(StudyTaintTagChecker checker) throws IOException, CheckstyleException {
        String input = FlowEvalUtil.readAndTaintResource(getClass(), "/checkstyle-8934.java");
        checker.recordInput(input);
        try {
            run(input);
        } catch (NullPointerException e) {
            checker.check(e);
            return;
        }
        throw new AssertionError("Expected exception to be thrown");
    }

    static void run(String input) throws IOException, CheckstyleException {
        File inputFile = copyResourceToTempFile(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        FileText text = new FileText(inputFile.getAbsoluteFile(), Arrays.asList(input.split("\n")));
        TreeWalker fsc = new TreeWalker();
        ClassLoader moduleClassLoader = Checker.class.getClassLoader();
        fsc.finishLocalSetup();
        fsc.setModuleFactory(new PackageObjectFactory(Checker.class.getPackage().getName(), moduleClassLoader));
        fsc.setupChild(new DefaultConfiguration(FinalLocalVariableCheck.class.getName()));
        fsc.beginProcessing(StandardCharsets.UTF_8.name());
        fsc.process(inputFile, text);
        fsc.finishProcessing();
        fsc.destroy();
    }

    private static File copyResourceToTempFile(InputStream in) throws IOException {
        File tempDir = Files.createTempDirectory(null).toFile();
        tempDir.deleteOnExit();
        File inputFile = new File(tempDir, "ExpressionSwitchBugs.java");
        inputFile.deleteOnExit();
        if (in == null) {
            throw new IllegalArgumentException();
        }
        try (FileOutputStream out = new FileOutputStream(inputFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return inputFile;
    }
}

