package edu.neu.ccs.conflux.study;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import edu.neu.ccs.conflux.internal.FlowStudy;
import edu.neu.ccs.conflux.internal.runtime.TaintTagChecker;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static edu.neu.ccs.conflux.FlowTestUtil.taintWithIndices;

public class ClosureFlowStudy {

    static {
        LogManager.getLogManager().reset();
    }

    @FlowStudy(project = "closure", issue = "652")
    public void example(TaintTagChecker checker) {
        String code = readResource(ClosureFlowStudy.class, "/closure-652/observe.js");
        int start = code.indexOf("throw");
        Set<Integer> expected = IntStream.range(start, start + "throw".length())
                .boxed()
                .collect(Collectors.toSet());
        Compiler compiler = new Compiler(new PrintStream(new ByteArrayOutputStream(), false));
        SourceFile extern = SourceFile.fromCode("extern", "");
        CompilerOptions options = new CompilerOptions();
        compiler.disableThreads();
        CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
        SourceFile input = SourceFile.fromCode("input", code);
        try {
            compiler.compile(extern, input, options);
        } catch (Throwable t) {
            checker.check(expected, t);
            return;
        }
        throw new AssertionError("Expected exception to be thrown");
    }

    private static String readResource(Class<?> clazz, String name) {
        return taintWithIndices(new Scanner(clazz.getResourceAsStream(name), "UTF-8")
                .useDelimiter("\\A").next());
    }
}

