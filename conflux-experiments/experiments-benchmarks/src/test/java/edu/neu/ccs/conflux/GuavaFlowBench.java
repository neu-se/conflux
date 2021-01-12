package edu.neu.ccs.conflux;

import edu.neu.ccs.conflux.internal.FlowBench;
import edu.neu.ccs.conflux.internal.runtime.BenchTaintTagChecker;

import static com.google.common.html.HtmlEscapers.htmlEscaper;
import static com.google.common.net.UrlEscapers.urlFormParameterEscaper;
import static edu.neu.ccs.conflux.ControlFlowBenchUtil.*;

public class GuavaFlowBench {

    @FlowBench(group = "spaces-url-encode", project = "Guava", implementation = "UrlEscapers")
    public void spacesUrlEncode(BenchTaintTagChecker checker, int numberOfEntities) {
        checkSpacesUrlEncode(checker, numberOfEntities, s -> urlFormParameterEscaper().escape(s));
    }

    @FlowBench(group = "reserved-percent-encode", project = "Guava", implementation = "UrlEscapers")
    public void reservedPercentEncode(BenchTaintTagChecker checker, int numberOfEntities) {
        checkReservedPercentEncode(checker, numberOfEntities, s -> urlFormParameterEscaper().escape(s));
    }

    @FlowBench(group = "unicode-percent-encode", project = "Guava", implementation = "UrlEscapers")
    public void unicodePercentEncode(BenchTaintTagChecker checker, int numberOfEntities) {
        checkUnicodePercentEncode(checker, numberOfEntities, s -> urlFormParameterEscaper().escape(s));
    }

    @FlowBench(group = "html-escape", project = "Guava", implementation = "HtmlEscapers")
    public void htmlEscape(BenchTaintTagChecker checker, int numberOfEntities) {
        checkHtmlEscape(checker, numberOfEntities, s -> htmlEscaper().escape(s));
    }
}
