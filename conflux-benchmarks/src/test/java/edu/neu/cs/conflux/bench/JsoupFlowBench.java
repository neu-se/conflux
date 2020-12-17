package edu.neu.cs.conflux.bench;

import edu.gmu.swe.phosphor.FlowBench;
import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import org.jsoup.nodes.Entities;

import static edu.neu.cs.conflux.bench.ControlFlowBenchUtil.checkHtmlEscape;

public class JsoupFlowBench {

    @FlowBench(group = "html-escape", project = "jsoup", implementation = "Entities")
    public void htmlEscape(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkHtmlEscape(benchResult, numberOfEntities, Entities::escape);
    }
}
