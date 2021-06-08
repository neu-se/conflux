package edu.neu.ccs.conflux;

public class CheckStyleRunner extends StudyRunner {
    public CheckStyleRunner() {
        super(NullPointerException.class, new StackTraceElement(
                "com.puppycrawl.tools.checkstyle.checks.coding.FinalLocalVariableCheck",
                "updateUninitializedVariables",
                "FinalLocalVariableCheck.java",
                482));
    }

    @Override
    public void run(String input) throws Throwable {
        CheckstyleFlowStudy.run(input);
    }

    @Override
    public String getInitial() {
        return FlowEvalUtil.readResource(getClass(), "/checkstyle-8934.java");
    }
}