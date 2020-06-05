package edu.gmu.swe.phosphor.ignored.runtime;

import java.util.*;

public class RunResult {

    private int truePositives = 0;
    private int falsePositives = 0;
    private int falseNegatives = 0;

    public void check(Set<?> expected, Set<?> predicted) {
        for(Object label : expected) {
            if(predicted.contains(label)) {
                truePositives++;
            } else {
                falseNegatives++;
            }
        }
        for(Object label : predicted) {
            if(!expected.contains(label)) {
                falsePositives++;
            }
        }
    }

    public double precision() {
        if(truePositives + falsePositives == 0) {
            return 0; // undefined, no labels were predicted
        } else {
            return (1.0 * truePositives) / (truePositives + falsePositives);
        }
    }

    public double recall() {
        if(truePositives + falseNegatives == 0) {
            return 0; // undefined, no labels were expected
        } else {
            return (1.0 * truePositives) / (truePositives + falseNegatives);
        }
    }

    public double f1Score() {
        if(truePositives == 0) {
            return 0;
        }
        double denominator = (2.0 * truePositives + falsePositives + falseNegatives);
        return (2.0 * truePositives) / denominator;
    }

    public int truePositives() {
        return truePositives;
    }

    public int falsePositives() {
        return falsePositives;
    }

    public int falseNegatives() {
        return falseNegatives;
    }
}
