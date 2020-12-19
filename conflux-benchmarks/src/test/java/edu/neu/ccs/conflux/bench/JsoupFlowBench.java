package edu.neu.ccs.conflux.bench;

import edu.neu.ccs.conflux.FlowBench;
import edu.neu.ccs.conflux.internal.runtime.FlowBenchResultImpl;
import org.jsoup.nodes.Entities;

import static edu.neu.ccs.conflux.bench.ControlFlowBenchUtil.checkHtmlEscape;

public class JsoupFlowBench {

    @FlowBench(group = "html-escape", project = "jsoup", implementation = "Entities")
    public void htmlEscape(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkHtmlEscape(benchResult, numberOfEntities, Entities::escape);
    }
}
