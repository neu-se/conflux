package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import org.jsoup.nodes.Entities;

import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.checkHtmlEscape;

public class JsoupFlowBench {

    @FlowBench(group = "html-escape", project = "jsoup", implementation = "Entities")
    public void htmlEscape(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkHtmlEscape(benchResult, numberOfEntities, Entities::escape);
    }
}
