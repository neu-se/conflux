package edu.neu.ccs.conflux.internal;

import edu.columbia.cs.psl.phosphor.struct.harmony.util.Set;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.SortedSet;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.TreeSet;

import java.util.Iterator;

public class StudyRunResult {

    private final int[] predicted;
    private final String input;

    public StudyRunResult(String input, Set<Object> predicted) {
        if (input == null) {
            throw new NullPointerException();
        }
        this.input = input;
        SortedSet<Integer> temp = new TreeSet<>();
        for (Object o : predicted) {
            if (o instanceof Integer) {
                temp.add((Integer) o);
            }
        }
        this.predicted = new int[temp.size()];
        Iterator<Integer> itr = temp.iterator();
        for (int i = 0; i < this.predicted.length; i++) {
            this.predicted[i] = itr.next();
        }
    }

    public String getInput() {
        return input;
    }

    public int[] getPredicted() {
        return predicted.clone();
    }
}
