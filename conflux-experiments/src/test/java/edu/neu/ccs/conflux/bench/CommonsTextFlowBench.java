package edu.neu.ccs.conflux.bench;

import edu.neu.ccs.conflux.FlowBench;
import edu.neu.ccs.conflux.internal.runtime.FlowBenchResult;
import org.apache.commons.text.StringEscapeUtils;

public class CommonsTextFlowBench {

    @FlowBench(group = "html-escape", project = "Apache Commons Text", implementation = "StringEscapeUtils")
    public void htmlEscape(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkHtmlEscape(benchResult, numberOfEntities, StringEscapeUtils::escapeHtml4);
    }

    @FlowBench(group = "html-unescape", project = "Apache Commons Text", implementation = "StringEscapeUtils")
    public void htmlUnescape(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkHtmlUnescape(benchResult, numberOfEntities, StringEscapeUtils::unescapeHtml4);
    }

    @FlowBench(group = "javascript-escape", project = "Apache Commons Text", implementation = "StringEscapeUtils")
    public void javaScriptEscape(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkJavaScriptEscape(benchResult, numberOfEntities, StringEscapeUtils::escapeEcmaScript);
    }

    @FlowBench(group = "javascript-unescape", project = "Apache Commons Text", implementation = "StringEscapeUtils")
    public void javaScriptUnescape(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkJavaScriptUnescape(benchResult, numberOfEntities, StringEscapeUtils::unescapeEcmaScript);
    }
}
