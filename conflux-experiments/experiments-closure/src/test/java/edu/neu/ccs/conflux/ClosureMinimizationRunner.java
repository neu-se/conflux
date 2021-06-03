package edu.neu.ccs.conflux;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class ClosureMinimizationRunner extends MinimizationRunner {

    public ClosureMinimizationRunner() {
        super(NullPointerException.class, new StackTraceElement(
                "com.google.javascript.jscomp.parsing.NewIRFactory",
                "shouldAttachJSDocHere",
                "NewIRFactory.java",
                626
        ), 0);
    }

    @Override
    protected void test(String input) {
        Compiler compiler = new Compiler(new PrintStream(new ByteArrayOutputStream(), false));
        SourceFile extern = SourceFile.fromCode("extern", "");
        CompilerOptions options = new CompilerOptions();
        compiler.disableThreads();
        CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
        compiler.compile(extern, SourceFile.fromCode("input", input), options);
    }

    public static void main(String[] arguments) throws IOException {
        new ClosureMinimizationRunner().run(new File(arguments[0]));
        System.exit(0);
    }
}
