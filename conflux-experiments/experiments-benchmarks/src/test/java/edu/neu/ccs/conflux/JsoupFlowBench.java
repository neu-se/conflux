package edu.neu.ccs.conflux;

import edu.neu.ccs.conflux.internal.FlowBench;
import edu.neu.ccs.conflux.internal.runtime.BenchTaintTagChecker;
import org.jsoup.nodes.Entities;

import static edu.neu.ccs.conflux.ControlFlowBenchUtil.checkHtmlEscape;

public class JsoupFlowBench {

    @FlowBench(group = "html-escape", project = "jsoup", implementation = "Entities")
    public void htmlEscape(BenchTaintTagChecker checker, int numberOfEntities) {
        checkHtmlEscape(checker, numberOfEntities, Entities::escape);
    }
}
