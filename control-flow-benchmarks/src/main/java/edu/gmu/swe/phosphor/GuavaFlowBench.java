package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;

import static com.google.common.html.HtmlEscapers.htmlEscaper;
import static com.google.common.net.UrlEscapers.urlFormParameterEscaper;
import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.*;

public class GuavaFlowBench {

    @FlowBench(group = "spaces-url-encode", project = "guava", implementation = "UrlEscapers")
    public void spacesUrlEncode(FlowBenchResultImpl benchResult) {
        checkSpacesUrlEncode(benchResult, TaintedPortionPolicy.ALL, s -> urlFormParameterEscaper().escape(s));
    }

    @FlowBench(group = "reserved-percent-encode", project = "guava", implementation = "UrlEscapers")
    public void reservedPercentEncode(FlowBenchResultImpl benchResult) {
        checkReservedPercentEncode(benchResult, TaintedPortionPolicy.ALL, s -> urlFormParameterEscaper().escape(s));
    }

    @FlowBench(group = "unicode-percent-encode", project = "guava", implementation = "UrlEscapers")
    public void unicodePercentEncode(FlowBenchResultImpl benchResult) {
        checkUnicodePercentEncode(benchResult, TaintedPortionPolicy.ALL, s -> urlFormParameterEscaper().escape(s));
    }

    @FlowBench(group = "html-escape", project = "guava", implementation = "HtmlEscapers")
    public void htmlEscape(FlowBenchResultImpl benchResult) {
        checkHtmlEscape(benchResult, TaintedPortionPolicy.ALL, s -> htmlEscaper().escape(s));
    }
}
