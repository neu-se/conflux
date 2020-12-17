package edu.neu.cs.conflux.study;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import edu.gmu.swe.phosphor.FlowBench;
import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.LogManager;

import static edu.gmu.swe.phosphor.FlowBenchUtil.taintWithIndices;

public class ClosureFlowStudy {

    static {
        LogManager.getLogManager().reset();
    }

    //@FlowExperiment(project="closure", issue=652)
    @FlowBench(group = "-", project = "-", implementation = "-")
    public void example(FlowBenchResultImpl benchResult, int ignored) {
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

