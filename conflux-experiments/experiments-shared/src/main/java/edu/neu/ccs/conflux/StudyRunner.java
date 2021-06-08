package edu.neu.ccs.conflux;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static edu.neu.ccs.conflux.TestResult.*;

public abstract class StudyRunner {

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

    public final TestResult run(Function<List<CharSequence>, String> joiner, List<CharSequence> elements) {
        try {
            run(joiner.apply(elements));
        } catch (Throwable t) {
            if (t.getStackTrace().length > 0) {
                StackTraceElement e = t.getStackTrace()[0];
                return t.getClass().equals(targetException)
                        && e.equals(targetElement) ? FAIL : UNRESOLVED;
            }
        }
        return PASS;
    }

    public final String getInitial() {
        return FlowEvalUtil.readResource(getClass(), initialInputResourceName);
    }

    public static void main(String[] arguments) throws ReflectiveOperationException {
        StudyRunner runner = (StudyRunner) Class.forName(arguments[0]).newInstance();
        String result = Pruner.randomPrune(
                runner,
                split(runner.getInitial()),
                StudyRunner::join,
                100_000
        );
        DeltaDebuggingReducer<CharSequence> reducer = new DeltaDebuggingReducer<>((l) ->
                runner.run(StudyRunner::join, l)
        );
        result = join(reducer.reduce(split(result)));
        System.out.println("Length: " + result.length());
        System.out.println(result);
    }

    private static String join(Iterable<? extends CharSequence> elements) {
        return String.join("", elements);
    }

    private static List<CharSequence> split(String input) {
        return input.chars()
                .mapToObj(c -> "" + (char) c)
                .collect(Collectors.toList());
    }
}