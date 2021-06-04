package edu.neu.ccs.conflux;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static edu.neu.ccs.conflux.TestResult.*;

public abstract class StudyRunner implements Function<List<? extends CharSequence>, TestResult> {

    private final Class<? extends Throwable> targetException;
    private final StackTraceElement targetElement;
    private final String initialInputResourceName;

    public StudyRunner(Class<? extends Throwable> targetException, StackTraceElement targetElement,
                       String initialInputResourceName) {
        this.targetException = targetException;
        this.targetElement = targetElement;
        this.initialInputResourceName = initialInputResourceName;
    }

    protected String join(Iterable<? extends CharSequence> elements) {
        return String.join("\n", elements);
    }

    protected List<? extends CharSequence> split(String input) {
        return new BufferedReader(new StringReader(input))
                .lines()
                .collect(Collectors.toList());
    }

    protected abstract void run(String input) throws Throwable;

    @Override
    public final TestResult apply(List<? extends CharSequence> input) {
        try {
            run(join(input));
        } catch (Throwable t) {
            StackTraceElement e = t.getStackTrace()[0];
            return t.getClass().equals(targetException) && e.equals(targetElement) ? FAIL : UNRESOLVED;
        }
        return PASS;
    }

    public final List<? extends CharSequence> getInitial() {
        return split(FlowEvalUtil.readResource(getClass(), initialInputResourceName));
    }

    public static void main(String[] arguments) throws ReflectiveOperationException {
        StudyRunner runner = (StudyRunner) Class.forName(arguments[0]).newInstance();
        DeltaDebuggingReducer<CharSequence> reducer = new DeltaDebuggingReducer<>(runner);
        List<? extends CharSequence> result = reducer.reduce(runner.getInitial());
        System.out.println(runner.join(result));
        System.exit(0);
    }
}
