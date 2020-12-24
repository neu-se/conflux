package edu.neu.ccs.conflux.study;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import edu.neu.ccs.conflux.internal.runtime.TaintTagChecker;
import edu.neu.ccs.conflux.internal.FlowStudy;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.LogManager;

import static edu.neu.ccs.conflux.FlowTestUtil.taintWithIndices;

public class ClosureFlowStudy {

    static {
        LogManager.getLogManager().reset();
    }

    @FlowStudy(project = "closure", issue = "652")
    public void example(TaintTagChecker checker) {
        String code = taintWithIndices("%48%69%21");
        Compiler compiler = new Compiler(new PrintStream(new ByteArrayOutputStream(), false));
        SourceFile extern = SourceFile.fromCode("extern", "");
        CompilerOptions options = new CompilerOptions();
        compiler.disableThreads();
        CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
        SourceFile input = SourceFile.fromCode("input", code);
        compiler.compile(extern, input, options);
    }
}

