package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import org.apache.commons.text.StringEscapeUtils;

import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.*;

public class CommonsTextFlowBench {

    @FlowBench(group = "html-escape", project = "common-text", implementation = "StringEscapeUtils")
    public void htmlEscape(FlowBenchResultImpl benchResult) {
        checkHtmlEscape(benchResult, TaintedPortionPolicy.ALL, StringEscapeUtils::escapeHtml4);
    }

    @FlowBench(group = "html-unescape", project = "common-text", implementation = "StringEscapeUtils")
    public void htmlUnescape(FlowBenchResultImpl benchResult) {
        checkHtmlUnescape(benchResult, TaintedPortionPolicy.ALL, StringEscapeUtils::unescapeHtml4);
    }

    @FlowBench(group = "javascript-escape", project = "common-text", implementation = "StringEscapeUtils")
    public void javaScriptEscape(FlowBenchResultImpl benchResult) {
        checkJavaScriptEscape(benchResult, TaintedPortionPolicy.ALL, StringEscapeUtils::escapeEcmaScript);
    }

    @FlowBench(group = "javascript-unescape", project = "common-text", implementation = "StringEscapeUtils")
    public void javaScriptUnescape(FlowBenchResultImpl benchResult) {
        checkJavaScriptUnescape(benchResult, TaintedPortionPolicy.ALL, StringEscapeUtils::unescapeEcmaScript);
    }
}
