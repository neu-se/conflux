package edu.neu.ccs.conflux;

import org.mozilla.javascript.Context;

import java.io.File;
import java.io.IOException;

public class RhinoMinimizationRunner extends MinimizationRunner {
    public RhinoMinimizationRunner() {
        super(IllegalArgumentException.class, new StackTraceElement(
                "org.mozilla.classfile.SuperBlock",
                "merge",
                "SuperBlock.java",
                101
        ), 0);
    }

    @Override
    protected void test(String input) {
        Context context = Context.enter();
        context.setLanguageVersion(Context.VERSION_1_8);
        try {
            context.compileString(input, "input", 0, null);
        } finally {
            Context.exit();
        }
    }

    public static void main(String[] arguments) throws IOException {
        new RhinoMinimizationRunner().run(new File(arguments[0]));
        System.exit(0);
    }
}
