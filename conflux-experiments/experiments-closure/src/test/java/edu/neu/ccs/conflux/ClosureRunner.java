package edu.neu.ccs.conflux;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ClosureRunner extends StudyRunner {
    public ClosureRunner() {
        super(NullPointerException.class, new StackTraceElement(
                "com.google.javascript.jscomp.parsing.NewIRFactory",
                "shouldAttachJSDocHere",
                "NewIRFactory.java",
                626
        ));
    }

    @Override
    public void run(String input) {
        Compiler compiler = new Compiler(new PrintStream(new ByteArrayOutputStream(), false));
        SourceFile extern = SourceFile.fromCode("extern", "");
        CompilerOptions options = new CompilerOptions();
        compiler.disableThreads();
        CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
        compiler.compile(extern, SourceFile.fromCode("input", input), options);
    }

    @Override
    public String getInitial() {
        return FlowEvalUtil.readResource(getClass(), "/closure-652.js");
    }
}