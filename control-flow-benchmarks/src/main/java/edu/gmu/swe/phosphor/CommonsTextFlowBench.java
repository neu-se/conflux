package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import org.apache.commons.text.StringEscapeUtils;

import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.checkEscapeHtml;

public class CommonsTextFlowBench {

    @FlowBench
    public void escapeHtml(FlowBenchResultImpl benchResult) {
        checkEscapeHtml(benchResult, TaintedPortionPolicy.ALL, StringEscapeUtils::escapeHtml4);
    }
}
