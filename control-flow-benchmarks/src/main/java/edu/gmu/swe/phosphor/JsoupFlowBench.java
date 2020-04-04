package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import org.jsoup.parser.Parser;

import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.checkUnescapeHtml;

public class JsoupFlowBench {

    @FlowBench
    public void unescapeHtml(FlowBenchResultImpl benchResult) {
        checkUnescapeHtml(benchResult, TaintedPortionPolicy.ALL, s -> Parser.unescapeEntities(s, true));
    }
}
