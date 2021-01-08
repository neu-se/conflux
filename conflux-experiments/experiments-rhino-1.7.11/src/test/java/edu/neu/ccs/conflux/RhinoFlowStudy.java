package edu.neu.ccs.conflux;

import edu.neu.ccs.conflux.internal.FlowStudy;
import edu.neu.ccs.conflux.internal.runtime.TaintTagChecker;
import org.mozilla.javascript.Context;

import java.util.Set;

public class RhinoFlowStudy {

    /**
     * https://github.com/mozilla/rhino/issues/539
     */
    @FlowStudy(project = "rhino", issue = "539")
    public void issue539(TaintTagChecker checker) {
        String input = FlowEvalUtil.readAndTaintResource(getClass(), "/rhino-539.js");
        Set<Integer> expected = FlowEvalUtil.createExpectedSet(input, "try", "finally");
        Context context = Context.enter();
        context.setLanguageVersion(Context.VERSION_1_8);
        try {
            context.compileString(input, "input", 0, null);
        } catch (Throwable t) {
            checker.check(expected, t);
            return;
        } finally {
            Context.exit();
        }
        throw new AssertionError("Expected exception to be thrown");
    }
}
