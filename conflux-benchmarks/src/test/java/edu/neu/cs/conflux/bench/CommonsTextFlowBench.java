package edu.neu.cs.conflux.bench;

import edu.gmu.swe.phosphor.FlowBench;
import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import org.apache.commons.text.StringEscapeUtils;

public class CommonsTextFlowBench {

    @FlowBench(group = "html-escape", project = "Apache Commons Text", implementation = "StringEscapeUtils")
    public void htmlEscape(FlowBenchResultImpl benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkHtmlEscape(benchResult, numberOfEntities, StringEscapeUtils::escapeHtml4);
    }

    @FlowBench(group = "html-unescape", project = "Apache Commons Text", implementation = "StringEscapeUtils")
    public void htmlUnescape(FlowBenchResultImpl benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkHtmlUnescape(benchResult, numberOfEntities, StringEscapeUtils::unescapeHtml4);
    }

    @FlowBench(group = "javascript-escape", project = "Apache Commons Text", implementation = "StringEscapeUtils")
    public void javaScriptEscape(FlowBenchResultImpl benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkJavaScriptEscape(benchResult, numberOfEntities, StringEscapeUtils::escapeEcmaScript);
    }

    @FlowBench(group = "javascript-unescape", project = "Apache Commons Text", implementation = "StringEscapeUtils")
    public void javaScriptUnescape(FlowBenchResultImpl benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkJavaScriptUnescape(benchResult, numberOfEntities, StringEscapeUtils::unescapeEcmaScript);
    }
}
