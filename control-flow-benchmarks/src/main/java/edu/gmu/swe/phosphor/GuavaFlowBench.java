package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;

import static com.google.common.html.HtmlEscapers.htmlEscaper;
import static com.google.common.net.UrlEscapers.urlFormParameterEscaper;
import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.*;

public class GuavaFlowBench {

    @FlowBench
    public void urlEncodeSpaces(FlowBenchResultImpl benchResult) {
        checkURLEncodeSpaces(benchResult, TaintedPortionPolicy.ALL, s -> urlFormParameterEscaper().escape(s));
    }

    @FlowBench
    public void urlEncodeReserved(FlowBenchResultImpl benchResult) {
        checkPercentEncodeReserved(benchResult, TaintedPortionPolicy.ALL, s -> urlFormParameterEscaper().escape(s));
    }

    @FlowBench
    public void urlEncodeUnicode(FlowBenchResultImpl benchResult) {
        checkPercentEncodeUnicode(benchResult, TaintedPortionPolicy.ALL, s -> urlFormParameterEscaper().escape(s));
    }

    @FlowBench
    public void htmlEscape(FlowBenchResultImpl benchResult) {
        checkEscapeHtml(benchResult, TaintedPortionPolicy.ALL, s -> htmlEscaper().escape(s));
    }
}
