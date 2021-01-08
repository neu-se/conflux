package edu.neu.ccs.conflux;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import edu.neu.ccs.conflux.internal.FlowStudy;
import edu.neu.ccs.conflux.internal.runtime.TaintTagChecker;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Set;
import java.util.logging.LogManager;

public class Closure20140814FlowStudy {

    static {
        LogManager.getLogManager().reset();
    }

    /**
     * https://github.com/google/closure-compiler/issues/652
     */
    @FlowStudy(project = "closure", issue = "652")
    public void issue652(TaintTagChecker checker) {
        String input = FlowEvalUtil.readAndTaintResource(getClass(), "/closure-652.js");
        Set<Integer> expected = FlowEvalUtil.createExpectedSet(input, "throw");
        compileAndCheckException(checker, input, expected);
    }

    private static void compileAndCheckException(TaintTagChecker checker, String input, Set<Integer> expected) {
        Compiler compiler = new Compiler(new PrintStream(new ByteArrayOutputStream(), false));
        SourceFile extern = SourceFile.fromCode("extern", "");
        CompilerOptions options = new CompilerOptions();
        compiler.disableThreads();
        CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
        try {
            compiler.compile(extern, SourceFile.fromCode("input", input), options);
        } catch (Throwable t) {
            checker.check(expected, t);
            return;
        }
        throw new AssertionError("Expected exception to be thrown");
    }
}

