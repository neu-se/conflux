package edu.neu.ccs.conflux;

import edu.neu.ccs.conflux.internal.FlowStudy;
import edu.neu.ccs.conflux.internal.runtime.StudyTaintTagChecker;

public class OpenRefineFlowStudy {
    /**
     * https://github.com/OpenRefine/OpenRefine/issues/2584
     */
    @FlowStudy(project = "openrefine", issue = "2583")
    public void issue652(StudyTaintTagChecker checker) {
        String input = FlowEvalUtil.readAndTaintResource(getClass(), "/openrefine-2583.csv");
        checker.recordInput(input);
        try {
            String s = null;
            s.length();
        } catch (Throwable t) {
            checker.check(t);
            return;
        }
        throw new AssertionError("Expected exception to be thrown");
    }
}
