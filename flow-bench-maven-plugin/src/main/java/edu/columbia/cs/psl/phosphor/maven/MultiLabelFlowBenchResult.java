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
            throw new IllegalArgumentException();
        }
        double sum = 0;
        IntSinglyLinkedList.IntListIterator correctIt = numCorrect.iterator();
        IntSinglyLinkedList.IntListIterator predictedIt = numPredicted.iterator();
        for(int correct = correctIt.nextInt(), predicted = predictedIt.nextInt();
            correctIt.hasNext() && predictedIt.hasNext();
            correct = correctIt.nextInt(), predicted = predictedIt.nextInt()) {
            sum += (1.0 * correct)/predicted;
        }
        return sum/numCorrect.size();
    }

    protected double microAveragePrecision() {
        if(numCorrect.size() == 0) {
            throw new IllegalArgumentException();
        }
        int num = 0, denom = 0;
        IntSinglyLinkedList.IntListIterator correctIt = numCorrect.iterator();
        IntSinglyLinkedList.IntListIterator predictedIt = numPredicted.iterator();
        for(int correct = correctIt.nextInt(), predicted = predictedIt.nextInt();
            correctIt.hasNext() && predictedIt.hasNext();
            correct = correctIt.nextInt(), predicted = predictedIt.nextInt()) {
            num += correct;
            denom += predicted;
        }
        return (1.0 * num)/denom;
    }

    protected double macroAverageRecall() {
        if(numCorrect.size() == 0) {
            throw new IllegalArgumentException();
        }
        double sum = 0;
        IntSinglyLinkedList.IntListIterator correctIt = numCorrect.iterator();
        IntSinglyLinkedList.IntListIterator expectedIt = numExpected.iterator();
        for(int correct = correctIt.nextInt(), expected = expectedIt.nextInt();
            correctIt.hasNext() && expectedIt.hasNext();
            correct = correctIt.nextInt(), expected = expectedIt.nextInt()) {
            sum += (1.0 * correct)/expected;
        }
        return sum/numCorrect.size();
    }

    protected double microAverageRecall() {
        if(numCorrect.size() == 0) {
            throw new IllegalArgumentException();
        }
        int num = 0, denom = 0;
        IntSinglyLinkedList.IntListIterator correctIt = numCorrect.iterator();
        IntSinglyLinkedList.IntListIterator expectedIt = numExpected.iterator();
        for(int correct = correctIt.nextInt(), expected = expectedIt.nextInt();
            correctIt.hasNext() && expectedIt.hasNext();
            correct = correctIt.nextInt(), expected = expectedIt.nextInt()) {
            num += correct;
            denom += expected;
        }
        return (1.0 * num)/denom;
    }

    protected double macroAverageF1Score() {
        return 2 * (macroAveragePrecision() * macroAverageRecall())/(macroAveragePrecision() + macroAverageRecall());
    }

    protected double microAverageF1Score() {
        return 2 * (microAveragePrecision() * microAverageRecall())/(microAveragePrecision() + microAverageRecall());
    }

    @Override
    public void check(Set<?> expected, Set<?> actual) {
        // Treat the empty set as though it is a set containing only the "empty" label to avoid division by zero issues
        numExpected.enqueue(Math.max(1, expected.size()));
        numPredicted.enqueue(Math.max(1, actual.size()));
        if(expected.isEmpty() && actual.isEmpty()) {
            numCorrect.enqueue(1);
        } else {
            Set<Object> union = new HashSet<>(expected);
            union.retainAll(actual);
            numCorrect.enqueue(union.size());
        }
    }
}
