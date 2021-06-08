package edu.neu.ccs.conflux;

import java.util.*;
import java.util.function.Function;

import static edu.neu.ccs.conflux.TestResult.FAIL;

public class Pruner {

    public static String randomPrune(StudyRunner runner, List<CharSequence> input,
                                     Function<List<CharSequence>, String> joiner, int iterations) {
        if (input.isEmpty() || runner.run(joiner, input) != FAIL) {
            throw new IllegalArgumentException();
        }
        Random random = new Random(1_749_939);
        List<CharSequence> best = input;
        Map<List<CharSequence>, TestResult> cache = new HashMap<>();
        for (int i = 0; i < iterations; i++) {
            List<CharSequence> sample;
            switch (random.nextInt(3)) {
                case 0:
                    sample = randomSection(best, random);
                    break;
                case 1:
                    sample = removeRandomSection(best, random);
                    break;
                default:
                    sample = randomSublist(best, random);
            }
            if (sample.size() < best.size()) {
                if (!cache.containsKey(sample)) {
                    cache.put(sample, runner.run(joiner, sample));
                }
                if (cache.get(sample) == FAIL) {
                    best = sample;
                }
            }
        }
        return joiner.apply(best);
    }

    private static <T> List<T> randomSublist(List<T> input, Random random) {
        List<T> result = new LinkedList<>();
        for (T item : input) {
            if (random.nextBoolean()) {
                result.add(item);
            }
        }
        return result;
    }

    private static <T> int[] chooseRandomSection(List<T> input, Random random) {
        if (input.isEmpty()) {
            return new int[]{0, 0};
        }
        int start = random.nextInt(input.size());
        int end = random.nextInt(input.size());
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }
        return new int[]{start, end};
    }

    private static <T> List<T> removeRandomSection(List<T> input, Random random) {
        int[] section = chooseRandomSection(input, random);
        List<T> result = new LinkedList<>(input.subList(0, section[0]));
        result.addAll(input.subList(section[1] + 1, input.size()));
        return result;
    }

    private static <T> List<T> randomSection(List<T> input, Random random) {
        int[] section = chooseRandomSection(input, random);
        return input.subList(section[0], section[1] + 1);
    }
}
