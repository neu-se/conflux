package edu.gmu.swe.phosphor;

import edu.columbia.cs.psl.phosphor.maven.FlowBenchReport;

import java.util.Comparator;

public class FlowBenchReportComparator implements Comparator<FlowBenchReport> {

    @Override
    public int compare(FlowBenchReport o1, FlowBenchReport o2) {
        int c = o1.getClassName().compareTo(o2.getClassName());
        return (c == 0) ? o1.getMethodName().compareTo(o2.getMethodName()): c;
    }
}
