package edu.neu.ccs.conflux;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Based on https://www.fuzzingbook.org/html/Reducer.html
 */
public final class DeltaDebuggingReducer<T> {

    private final boolean logTest = Boolean.getBoolean("dd.log.test");
    private final Map<List<? extends T>, TestResult> cache = new HashMap<>();
    private final Function<? super List<? extends T>, TestResult> runner;
    private int tests = 0;

    public DeltaDebuggingReducer(Function<? super List<? extends T>, TestResult> runner) {
        this.runner = runner;
    }

    private TestResult test(List<? extends T> input) {
        if (!cache.containsKey(input)) {
            TestResult result = runner.apply(input);
            tests++;
            if (logTest) {
                System.out.printf("Test #%d %s %d %s\n", tests, input, input.size(), result);
            }
            cache.put(input, result);
        }
        return cache.get(input);
    }

    public <U extends T> List<U> reduce(List<U> input) {
        tests = 0;
        cache.clear();
        if (test(input) == TestResult.PASS) {
            throw new IllegalArgumentException("Initial input must not pass");
        }
        int granularity = 2;
        while (input.size() >= 2) {
            double start = 0;
            double subsetLength = (1.0 * input.size()) / granularity;
            boolean someComplementIsFailing = false;
            while (start < input.size()) {
                // TODO check rounding and other issues
                List<U> complement = complement(input, (int) start, (int) subsetLength);
                if (test(complement) == TestResult.FAIL) {
                    input = complement;
                    granularity = Math.max(granularity - 1, 2);
                    someComplementIsFailing = true;
                }
                start += subsetLength;
            }
            if (!someComplementIsFailing) {
                if (granularity == input.size()) {
                    break;
                }
                granularity = Math.min(granularity * 2, input.size());
            }
        }
        return input;
    }

    private static <R> List<R> complement(List<R> input, int start, int subsetLength) {
        List<R> result = new LinkedList<>(input.subList(0, start));
        if (start + subsetLength <= input.size()) {
            result.addAll(input.subList(start + subsetLength, input.size()));
        }
        return result;
    }
}