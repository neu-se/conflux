package edu.neu.ccs.conflux;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import edu.neu.ccs.conflux.internal.FlowStudy;
import edu.neu.ccs.conflux.internal.runtime.TaintTagChecker;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.stream.IntStream;

import static edu.neu.ccs.conflux.FlowTestUtil.taintWithIndices;

public class ClosureFlowStudy {

    static {
        LogManager.getLogManager().reset();
    }

    /**
     * https://github.com/google/closure-compiler/issues/652
     */
    @FlowStudy(project = "closure", issue = "652")
    public void issue652(TaintTagChecker checker) {
        String input = readAndTaintResource("/closure-652.js");
        Set<Integer> expected = createExpectedSet(input, "throw");
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

    private static Set<Integer> createExpectedSet(String input, String... targets) {
        Set<Integer> expected = new HashSet<>();
        for (String target : targets) {
            int start = input.indexOf(target);
            IntStream.range(start, start + target.length()).boxed().forEach(expected::add);
        }
        return expected;
    }

    private static String readAndTaintResource(String name) {
        return taintWithIndices(new Scanner(ClosureFlowStudy.class.getResourceAsStream(name), "UTF-8")
                .useDelimiter("\\A").next());
    }
}

