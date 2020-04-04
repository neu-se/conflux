package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import org.springframework.web.util.HtmlUtils;

import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.checkEscapeHtml;

public class SpringWebFlowBench {

    @FlowBench
    public void escapeHtmlUTF8(FlowBenchResultImpl benchResult) {
        checkEscapeHtml(benchResult, TaintedPortionPolicy.ALL, s -> HtmlUtils.htmlEscape(s, "UTF-8"));
    }

    @FlowBench
    public void escapeHtmlISO(FlowBenchResultImpl benchResult) {
        checkEscapeHtml(benchResult, TaintedPortionPolicy.ALL, s -> HtmlUtils.htmlEscape(s, "ISO-8859-1"));
    }
}
