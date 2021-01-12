package edu.neu.ccs.conflux;

import edu.neu.ccs.conflux.internal.FlowBench;
import edu.neu.ccs.conflux.internal.runtime.BenchTaintTagChecker;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.JavaScriptUtils;
import org.springframework.web.util.UriUtils;

import java.util.function.UnaryOperator;

public class SpringWebFlowBench {

    private static final UnaryOperator<String> urlEncoder = s -> UriUtils.encode(s, "UTF-8");
    private static final UnaryOperator<String> urlDecoder = s -> UriUtils.decode(s, "UTF-8");

    @FlowBench(group = "reserved-percent-encode", project = "Spring Web", implementation = "UriUtils")
    public void reservedPercentEncode(BenchTaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkReservedPercentEncode(checker, numberOfEntities, urlEncoder);
    }

    @FlowBench(group = "reserved-percent-decode", project = "Spring Web", implementation = "UriUtils")
    public void reservedPercentDecode(BenchTaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkReservedPercentDecode(checker, numberOfEntities, urlDecoder);
    }

    @FlowBench(group = "unicode-percent-encode", project = "Spring Web", implementation = "UriUtils")
    public void unicodePercentEncode(BenchTaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkUnicodePercentEncode(checker, numberOfEntities, urlEncoder);
    }

    @FlowBench(group = "unicode-percent-decode", project = "Spring Web", implementation = "UriUtils")
    public void unicodePercentDecode(BenchTaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkUnicodePercentDecode(checker, numberOfEntities, urlDecoder);
    }

    @FlowBench(group = "html-escape", project = "Spring Web", implementation = "HtmlUtils-UTF-8")
    public void htmlEscapeUTF8(BenchTaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkHtmlEscape(checker, numberOfEntities, s -> HtmlUtils.htmlEscape(s, "UTF-8"));
    }

    @FlowBench(group = "html-escape", project = "Spring Web", implementation = "HtmlUtils-ISO-8859-1")
    public void htmlEscapeISO(BenchTaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkHtmlEscape(checker, numberOfEntities, s -> HtmlUtils.htmlEscape(s, "ISO-8859-1"));
    }

    @FlowBench(group = "html-unescape", project = "Spring Web", implementation = "HtmlUtils")
    public void htmlUnescape(BenchTaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkHtmlUnescape(checker, numberOfEntities, HtmlUtils::htmlUnescape);
    }

    @FlowBench(group = "javascript-escape", project = "Spring Web", implementation = "JavaScriptUtils")
    public void javaScriptEscape(BenchTaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkJavaScriptEscape(checker, numberOfEntities, JavaScriptUtils::javaScriptEscape);
    }
}
