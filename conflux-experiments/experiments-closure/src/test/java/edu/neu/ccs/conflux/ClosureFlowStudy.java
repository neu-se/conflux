package edu.neu.ccs.conflux;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import edu.neu.ccs.conflux.internal.FlowStudy;
import edu.neu.ccs.conflux.internal.runtime.StudyTaintTagChecker;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ClosureFlowStudy {

    /**
     * Issue: https://github.com/google/closure-compiler/issues/652
     * <p>
     * Fix: https://github.com/google/closure-compiler/commit/aac5d11480a0ed3f37919c23a5d3cc210e534bd5
     */
    @FlowStudy(project = "closure", issue = "652")
    public void issue652(StudyTaintTagChecker checker) {
        String input = FlowEvalUtil.readAndTaintResource(getClass(), "/closure-652.js");
        checker.recordInput(input);
        Compiler compiler = new Compiler(new PrintStream(new ByteArrayOutputStream(), false));
        SourceFile extern = SourceFile.fromCode("extern", "");
        CompilerOptions options = new CompilerOptions();
        compiler.disableThreads();
        CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
        try {
            compiler.compile(extern, SourceFile.fromCode("input", input), options);
        } catch (Throwable t) {
            checker.check(t);
            return;
        }
        throw new AssertionError("Expected exception to be thrown");
    }
}

