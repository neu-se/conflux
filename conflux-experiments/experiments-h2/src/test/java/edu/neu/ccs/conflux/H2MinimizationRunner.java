package edu.neu.ccs.conflux;

import org.h2.tools.RunScript;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;

public class H2MinimizationRunner extends MinimizationRunner {

    public H2MinimizationRunner() {
        super(NullPointerException.class, new StackTraceElement(
                "org.h2.expression.ExpressionColumn",
                "getValue",
                "ExpressionColumn.java",
                189
        ), 0);
    }

    @Override
    protected void test(String input) throws Throwable {
        try {
            Connection connection = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "", "");
            RunScript.execute(connection, new StringReader(input));
        } catch (Throwable t) {
            Throwable cause = t.getCause();
            if (!(cause instanceof NullPointerException)) {
                throw new AssertionError();
            } else {
                throw t.getCause();
            }
        }
    }

    public static void main(String[] arguments) throws IOException {
        new H2MinimizationRunner().run(new File(arguments[0]));
        System.exit(0);
    }
}
