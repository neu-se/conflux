package edu.neu.ccs.conflux;

import edu.neu.ccs.conflux.internal.FlowStudy;
import edu.neu.ccs.conflux.internal.runtime.StudyTaintTagChecker;
import org.h2.tools.RunScript;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;

public class H2FlowStudy {

    /**
     * Issue: https://github.com/h2database/h2database/issues/2550
     * <p>
     * Fix: https://github.com/h2database/h2database/commit/6c564e63eb6a3c819eaab19f4aece3298db2ab5f
     */
    @FlowStudy(project = "h2", issue = "2550")
    public void issue652(StudyTaintTagChecker checker) {
        String input = FlowEvalUtil.readAndTaintResource(getClass(), "/h2-2550.sql");
        checker.recordInput(input);
        try {
            Connection connection = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "", "");
            RunScript.execute(connection, new StringReader(input));
        } catch (Throwable t) {
            Throwable cause = t.getCause();
            if (!(cause instanceof NullPointerException)) {
                throw new AssertionError("Expected NullPointerException to be thrown");
            }
            checker.check(cause);
            return;
        }
        throw new AssertionError("Expected exception to be thrown");
    }
}