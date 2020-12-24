package edu.neu.ccs.conflux.bench;

import edu.neu.ccs.conflux.internal.FlowBench;
import edu.neu.ccs.conflux.internal.runtime.TaintTagChecker;

import static com.google.common.html.HtmlEscapers.htmlEscaper;
import static com.google.common.net.UrlEscapers.urlFormParameterEscaper;
import static edu.neu.ccs.conflux.bench.ControlFlowBenchUtil.*;

public class GuavaFlowBench {

    @FlowBench(group = "spaces-url-encode", project = "Guava", implementation = "UrlEscapers")
    public void spacesUrlEncode(TaintTagChecker checker, int numberOfEntities) {
        checkSpacesUrlEncode(checker, numberOfEntities, s -> urlFormParameterEscaper().escape(s));
    }

    @FlowBench(group = "reserved-percent-encode", project = "Guava", implementation = "UrlEscapers")
    public void reservedPercentEncode(TaintTagChecker checker, int numberOfEntities) {
        checkReservedPercentEncode(checker, numberOfEntities, s -> urlFormParameterEscaper().escape(s));
    }

    @FlowBench(group = "unicode-percent-encode", project = "Guava", implementation = "UrlEscapers")
    public void unicodePercentEncode(TaintTagChecker checker, int numberOfEntities) {
        checkUnicodePercentEncode(checker, numberOfEntities, s -> urlFormParameterEscaper().escape(s));
    }

    @FlowBench(group = "html-escape", project = "Guava", implementation = "HtmlEscapers")
    public void htmlEscape(TaintTagChecker checker, int numberOfEntities) {
        checkHtmlEscape(checker, numberOfEntities, s -> htmlEscaper().escape(s));
    }
}
