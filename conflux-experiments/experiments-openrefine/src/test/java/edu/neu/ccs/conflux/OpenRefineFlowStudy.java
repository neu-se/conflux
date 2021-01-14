package edu.neu.ccs.conflux;

import com.google.refine.importers.SeparatorBasedImporter;
import edu.neu.ccs.conflux.internal.FlowStudy;
import edu.neu.ccs.conflux.internal.runtime.StudyTaintTagChecker;

import java.io.StringReader;
public class OpenRefineFlowStudy {

    /**
     * https://github.com/OpenRefine/OpenRefine/issues/2584
     */
    @FlowStudy(project = "openrefine", issue = "2583")
    public void issue652(StudyTaintTagChecker checker) {
        String input = FlowEvalUtil.readAndTaintResource(getClass(), "/openrefine-2583.csv");
        checker.recordInput(input);
        try {
            prepareOptions();
            parseOneFile(new SeparatorBasedImporter(), new StringReader(input));
        } catch (Throwable t) {
            checker.check(t);
            return;
        }
        throw new AssertionError("Expected exception to be thrown");
    }

    private static void prepareOptions() {
        whenGetStringOption("separator", options, ",");
        whenGetIntegerOption("limit", options, -1);
        whenGetIntegerOption("skipDataLines", options, 0);
        whenGetIntegerOption("ignoreLines", options, 0);
        whenGetIntegerOption("headerLines", options, 0);
        whenGetBooleanOption("guessCellValueTypes", options, true);
        whenGetBooleanOption("processQuotes", options, !false);
        whenGetBooleanOption("trimStrings", options, true);
    }
}
