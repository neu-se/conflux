package edu.neu.ccs.conflux;

import org.mozilla.javascript.Context;

public class RhinoRunner extends StudyRunner {

    public RhinoRunner() {
        super(IllegalArgumentException.class, new StackTraceElement(
                "org.mozilla.classfile.SuperBlock",
                "merge",
                "SuperBlock.java",
                101
        ), "/rhino-539.js");
    }

    @Override
    protected void run(String input) {
        Context context = Context.enter();
        context.setLanguageVersion(Context.VERSION_1_8);
        try {
            context.compileString(input, "input", 0, null);
        } finally {
            Context.exit();
        }
    }
}
