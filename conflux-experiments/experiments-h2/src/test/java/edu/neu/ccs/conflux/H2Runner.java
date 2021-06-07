package edu.neu.ccs.conflux;

import org.h2.tools.RunScript;

import java.io.StringReader;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2Runner extends StudyRunner {
    public H2Runner() {
        super(NullPointerException.class, new StackTraceElement(
                "org.h2.expression.ExpressionColumn",
                "getValue",
                "ExpressionColumn.java",
                189
        ), "/h2-2550.sql");
    }

    @Override
    protected void run(String input) {
        try {
            RunScript.execute(DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "", ""),
                    new StringReader(input));
        } catch (Throwable t) {
            if (t.getCause() instanceof NullPointerException) {
                throw (NullPointerException) t.getCause();
            }
        } finally {
            try {
                RunScript.execute(DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "", ""),
                        new StringReader("DROP ALL OBJECTS"));
            } catch (SQLException e) {
                //
            }
        }
    }
}
