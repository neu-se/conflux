package edu.neu.ccs.conflux;

public class CheckStyleRunner extends StudyRunner {
    public CheckStyleRunner() {
        super(NullPointerException.class, new StackTraceElement(
                "com.puppycrawl.tools.checkstyle.checks.coding.FinalLocalVariableCheck",
                "updateUninitializedVariables",
                "FinalLocalVariableCheck.java",
                482
        ), "/checkstyle-8934.java");
    }

    @Override
    protected void run(String input) throws Throwable {
        CheckstyleFlowStudy.run(input);
    }
}
