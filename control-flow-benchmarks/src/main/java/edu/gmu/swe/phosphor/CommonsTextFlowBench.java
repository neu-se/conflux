package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import org.apache.commons.text.StringEscapeUtils;

import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.*;

public class CommonsTextFlowBench {

    @FlowBench
    public void htmlEscape(FlowBenchResultImpl benchResult) {
        checkEscapeHtml(benchResult, TaintedPortionPolicy.ALL, StringEscapeUtils::escapeHtml4);
    }

    @FlowBench
    public void htmlUnescape(FlowBenchResultImpl benchResult) {
        checkUnescapeHtml(benchResult, TaintedPortionPolicy.ALL, StringEscapeUtils::unescapeHtml4);
    }

    @FlowBench
    public void javaScriptEscape(FlowBenchResultImpl benchResult) {
        checkEscapeJavaScript(benchResult, TaintedPortionPolicy.ALL, StringEscapeUtils::escapeEcmaScript);
    }

    @FlowBench
    public void javaScriptUnescape(FlowBenchResultImpl benchResult) {
        checkUnescapeJavaScript(benchResult, TaintedPortionPolicy.ALL, StringEscapeUtils::unescapeEcmaScript);
    }
}
