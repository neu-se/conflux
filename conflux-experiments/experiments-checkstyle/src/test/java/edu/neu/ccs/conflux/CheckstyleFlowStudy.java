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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

public class CheckstyleFlowStudy {

    /**
     * https://github.com/checkstyle/checkstyle/issues/8934
     */
    @FlowStudy(project = "checkstyle", issue = "8934")
    public void issue8934(StudyTaintTagChecker checker) throws IOException {
        String input = FlowEvalUtil.readAndTaintResource(getClass(), "/checkstyle-8934.java");
        checker.recordInput(input);
        File inputFile = copyResourceToTempFile();
        FileText text = new FileText(inputFile.getAbsoluteFile(), Arrays.asList(input.split("\n")));
        checker.recordInput(input);
        try {
            check(inputFile, text);
        } catch (Throwable t) {
            checker.check(t);
            return;
        }
        throw new AssertionError("Expected exception to be thrown");
    }

    private File copyResourceToTempFile() throws IOException {
        File tempDir = Files.createTempDirectory(null).toFile();
        tempDir.deleteOnExit();
        File inputFile = new File(tempDir, "ExpressionSwitchBugs.java");
        inputFile.deleteOnExit();
        InputStream in = getClass().getResourceAsStream("/checkstyle-8934.java");
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

    private void check(File targetFile, FileText text) throws CheckstyleException {
        TreeWalker fsc = new TreeWalker();
        ClassLoader moduleClassLoader = Checker.class.getClassLoader();
        fsc.finishLocalSetup();
        fsc.setModuleFactory(new PackageObjectFactory(Checker.class.getPackage().getName(), moduleClassLoader));
        fsc.setupChild(new DefaultConfiguration(FinalLocalVariableCheck.class.getName()));
        fsc.beginProcessing(StandardCharsets.UTF_8.name());
        fsc.process(targetFile, text);
        fsc.finishProcessing();
        fsc.destroy();
    }
}

