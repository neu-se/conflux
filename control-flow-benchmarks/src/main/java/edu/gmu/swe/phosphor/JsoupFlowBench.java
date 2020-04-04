package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import org.jsoup.nodes.Entities;

import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.checkEscapeHtml;
import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.checkUnescapeHtml;

public class JsoupFlowBench {

    @FlowBench
    public void htmlEscape(FlowBenchResultImpl benchResult) {
        checkEscapeHtml(benchResult, TaintedPortionPolicy.ALL, Entities::escape);
    }

    @FlowBench
    public void htmlUnescape(FlowBenchResultImpl benchResult) {
        checkUnescapeHtml(benchResult, TaintedPortionPolicy.ALL, Entities::unescape);
    }
}
