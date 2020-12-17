package edu.neu.cs.conflux.bench;

import edu.gmu.swe.phosphor.FlowBench;
import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;

import static com.google.common.html.HtmlEscapers.htmlEscaper;
import static com.google.common.net.UrlEscapers.urlFormParameterEscaper;
import static edu.neu.cs.conflux.bench.ControlFlowBenchUtil.*;

public class GuavaFlowBench {

    @FlowBench(group = "spaces-url-encode", project = "Guava", implementation = "UrlEscapers")
    public void spacesUrlEncode(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkSpacesUrlEncode(benchResult, numberOfEntities, s -> urlFormParameterEscaper().escape(s));
    }

    @FlowBench(group = "reserved-percent-encode", project = "Guava", implementation = "UrlEscapers")
    public void reservedPercentEncode(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkReservedPercentEncode(benchResult, numberOfEntities, s -> urlFormParameterEscaper().escape(s));
    }

    @FlowBench(group = "unicode-percent-encode", project = "Guava", implementation = "UrlEscapers")
    public void unicodePercentEncode(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkUnicodePercentEncode(benchResult, numberOfEntities, s -> urlFormParameterEscaper().escape(s));
    }

    @FlowBench(group = "html-escape", project = "Guava", implementation = "HtmlEscapers")
    public void htmlEscape(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkHtmlEscape(benchResult, numberOfEntities, s -> htmlEscaper().escape(s));
    }
}
