package edu.columbia.cs.psl.phosphor.maven;

import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.ControlTaintTagStack;

import java.util.Arrays;
import java.util.HashSet;

public interface FlowBenchResult {

    static BinaryFlowBenchResult calculateBinaryResult(Taint[] expected, Taint[] actual) {
        if(expected == null) {
            throw new IllegalArgumentException("Expected array must be non-null");
        }
        if(actual == null) {
            actual = new Taint[expected.length];
        }
        if(expected.length != actual.length) {
            throw new IllegalArgumentException("Expected and actual arrays must be same length");
        }
        int truePositives = 0;
        int trueNegatives = 0;
        int falsePositives = 0;
        int falseNegatives = 0;
        for(int i = 0; i < expected.length; i++) {
            Taint e = expected[i];
            Taint a = actual[i];
            if(e == null || e.isEmpty()) {
                if(a == null || a.isEmpty()) {
                    trueNegatives++;
                } else {
                    falsePositives++;
                }
            } else {
                if(a == null || a.isEmpty()) {
                    falseNegatives++;
                } else {
                    truePositives++;
                }
            }
        }
        return new BinaryFlowBenchResult(truePositives, trueNegatives, falsePositives, falseNegatives);
    }

    static MultiLabelFlowBenchResult calculateMultiLabelResult(Taint[] expected, Taint[] actual) {
        if(expected == null) {
            throw new IllegalArgumentException("Expected array must be non-null");
        }
        if(actual == null) {
            actual = new Taint[expected.length];
        }
        if(expected.length != actual.length) {
            throw new IllegalArgumentException("Expected and actual arrays must be same length");
        }
        int[] correctLabels = new int[expected.length];
        int[] predictedLabels = new int[expected.length];
        int[] expectedLabels = new int[expected.length];
        for(int i = 0; i < expected.length; i++) {
            Object[] e = expected[i] == null ? new Object[0] : expected[i].getLabels();
            Object[] a = actual[i] == null ? new Object[0] : actual[i].getLabels();
            expectedLabels[i] = e.length;
            predictedLabels[i] = a.length;
            HashSet<Object> expectedSet = new HashSet<>(Arrays.asList(a));
            for(Object o : a) {
                if(expectedSet.contains(o)) {
                    correctLabels[i]++;
                }
            }
        }
        return new MultiLabelFlowBenchResult(correctLabels, predictedLabels, expectedLabels);
    }

    @SuppressWarnings("unused")
    static BinaryFlowBenchResult calculateBinaryResult$$PHOSPHORTAGGED(Taint[] expected, Taint[] actual, ControlTaintTagStack ctrl) {
        return calculateBinaryResult(expected, actual);
    }

    @SuppressWarnings("unused")
    static MultiLabelFlowBenchResult calculateMultiLabelResult$$PHOSPHORTAGGED(Taint[] expected, Taint[] actual, ControlTaintTagStack ctrl) {
        return calculateMultiLabelResult(expected, actual);
    }
}