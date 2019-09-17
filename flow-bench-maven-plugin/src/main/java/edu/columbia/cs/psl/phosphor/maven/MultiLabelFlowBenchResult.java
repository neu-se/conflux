package edu.columbia.cs.psl.phosphor.maven;

import edu.columbia.cs.psl.phosphor.struct.IntSinglyLinkedList;

import java.util.HashSet;
import java.util.Set;

public class MultiLabelFlowBenchResult extends FlowBenchResult {

    private final IntSinglyLinkedList numCorrect = new IntSinglyLinkedList();
    private final IntSinglyLinkedList numPredicted = new IntSinglyLinkedList();
    private final IntSinglyLinkedList numExpected = new IntSinglyLinkedList();

    protected IntSinglyLinkedList getNumCorrect() {
        return numCorrect;
    }

    protected IntSinglyLinkedList getNumPredicted() {
        return numPredicted;
    }

    protected IntSinglyLinkedList getNumExpected() {
        return numExpected;
    }

    @Override
    public String toString() {
        return "MultiLabelFlowBenchResult{" +
                "numCorrect=" + numCorrect +
                ", numPredicted=" + numPredicted +
                ", numExpected=" + numExpected +
                '}';
    }

    protected double macroAveragePrecision() {
        if(numCorrect.size() == 0) {
            return 0;
        }
        double sum = 0;
        IntSinglyLinkedList.IntListIterator correctIt = numCorrect.iterator();
        IntSinglyLinkedList.IntListIterator predictedIt = numPredicted.iterator();
        while(correctIt.hasNext() && predictedIt.hasNext()) {
            sum += (1.0 * correctIt.nextInt())/predictedIt.nextInt();
        }
        return sum/numCorrect.size();
    }

    protected double microAveragePrecision() {
        if(numCorrect.size() == 0) {
            return 0;
        }
        int num = 0, denom = 0;
        IntSinglyLinkedList.IntListIterator correctIt = numCorrect.iterator();
        IntSinglyLinkedList.IntListIterator predictedIt = numPredicted.iterator();
        while(correctIt.hasNext() && predictedIt.hasNext()) {
            num += correctIt.nextInt();
            denom += predictedIt.nextInt();
        }
        return (1.0 * num)/denom;
    }

    protected double macroAverageRecall() {
        if(numCorrect.size() == 0) {
            return 0;
        }
        double sum = 0;
        IntSinglyLinkedList.IntListIterator correctIt = numCorrect.iterator();
        IntSinglyLinkedList.IntListIterator expectedIt = numExpected.iterator();
        while(correctIt.hasNext() && expectedIt.hasNext()) {
            sum += (1.0 * correctIt.nextInt())/expectedIt.nextInt();
        }
        return sum/numCorrect.size();
    }

    protected double microAverageRecall() {
        if(numCorrect.size() == 0) {
            return 0;
        }
        int num = 0, denom = 0;
        IntSinglyLinkedList.IntListIterator correctIt = numCorrect.iterator();
        IntSinglyLinkedList.IntListIterator expectedIt = numExpected.iterator();
        while(correctIt.hasNext() && expectedIt.hasNext()) {
            num += correctIt.nextInt();
            denom += expectedIt.nextInt();
        }
        return (1.0 * num)/denom;
    }

    protected double macroAverageF1Score() {
        double denom = macroAveragePrecision() + macroAverageRecall();
        if(denom == 0) {
            return 0;
        }
        return 2 * (macroAveragePrecision() * macroAverageRecall())/denom;
    }

    protected double microAverageF1Score() {
        double denom = microAveragePrecision() + microAverageRecall();
        if(denom == 0) {
            return 0;
        }
        return 2 * (microAveragePrecision() * microAverageRecall())/denom;
    }

    protected double subSetAccuracy() {
        if(numCorrect.size() == 0) {
            return 0;
        }
        int count = 0;
        IntSinglyLinkedList.IntListIterator correctIt = numCorrect.iterator();
        IntSinglyLinkedList.IntListIterator expectedIt = numExpected.iterator();
        while(correctIt.hasNext() && expectedIt.hasNext()) {
            if(correctIt.nextInt() == expectedIt.nextInt()) {
                count++;
            }
        }
        return (1.0 * count) / numCorrect.size();
    }

    @Override
    public void check(Set<?> expected, Set<?> actual) {
        // Treat the empty set as though it is a set containing only the "empty" label to avoid division by zero issues
        numExpected.enqueue(Math.max(1, expected.size()));
        numPredicted.enqueue(Math.max(1, actual.size()));
        if(expected.isEmpty() && actual.isEmpty()) {
            numCorrect.enqueue(1);
        } else {
            Set<Object> intersection = new HashSet<>(expected);
            intersection.retainAll(actual);
            numCorrect.enqueue(intersection.size());
        }
    }
}
