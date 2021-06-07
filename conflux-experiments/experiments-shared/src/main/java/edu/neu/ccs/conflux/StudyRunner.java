package edu.neu.ccs.conflux;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static edu.neu.ccs.conflux.TestResult.*;

public abstract class StudyRunner {

    private static final Function<List<CharSequence>, String> LINE_JOINER = (l) -> String.join("\n", l);
    private static final Function<List<CharSequence>, String> CHAR_JOINER = (l) -> String.join("", l);
    private static final Function<String, List<CharSequence>> LINE_SPLITTER = (i) -> new BufferedReader(new StringReader(i))
            .lines()
            .collect(Collectors.toList());
    private static final Function<String, List<CharSequence>> CHAR_SPLITTER = (i) -> i.chars()
            .mapToObj(c -> (char) c)
            .map(c -> "" + c)
            .collect(Collectors.toList());
    private final Class<? extends Throwable> targetException;
    private final StackTraceElement targetElement;
    private final String initialInputResourceName;

    public StudyRunner(Class<? extends Throwable> targetException, StackTraceElement targetElement,
                       String initialInputResourceName) {
        this.targetException = targetException;
        this.targetElement = targetElement;
        this.initialInputResourceName = initialInputResourceName;
    }

    protected abstract void run(String input) throws Throwable;

    public final DeltaDebuggingReducer<CharSequence> createReducer(Function<List<CharSequence>, String> joiner) {
        return new DeltaDebuggingReducer<>((l) -> {
            try {
                run(joiner.apply(l));
            } catch (Throwable t) {
                StackTraceElement e = t.getStackTrace()[0];
                return t.getClass().equals(targetException)
                        && e.equals(targetElement) ? FAIL : UNRESOLVED;
            }
            return PASS;
        });
    }

    public final String getInitial() {
        return FlowEvalUtil.readResource(getClass(), initialInputResourceName);
    }

    public static void main(String[] arguments) throws ReflectiveOperationException {
        StudyRunner runner = (StudyRunner) Class.forName(arguments[0]).newInstance();
        String coarse = reduce(runner, LINE_JOINER, LINE_SPLITTER, runner.getInitial());
        String fine = reduce(runner, CHAR_JOINER, CHAR_SPLITTER, coarse);
        System.out.println("Length: " + fine.length());
        System.out.println(fine);
    }

    private static String reduce(StudyRunner runner, Function<List<CharSequence>, String> joiner,
                                 Function<String, List<CharSequence>> splitter, String input) {
        return joiner.apply(runner.createReducer(joiner).reduce(splitter.apply(input)));
    }
}
