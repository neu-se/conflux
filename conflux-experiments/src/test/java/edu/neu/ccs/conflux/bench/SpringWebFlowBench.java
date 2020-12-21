package edu.neu.ccs.conflux.bench;

import edu.neu.ccs.conflux.FlowBench;
import edu.neu.ccs.conflux.internal.runtime.FlowBenchResult;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.JavaScriptUtils;
import org.springframework.web.util.UriUtils;

import java.util.function.UnaryOperator;

public class SpringWebFlowBench {

    private static final UnaryOperator<String> urlEncoder = s -> UriUtils.encode(s, "UTF-8");
    private static final UnaryOperator<String> urlDecoder = s -> UriUtils.decode(s, "UTF-8");

    @FlowBench(group = "reserved-percent-encode", project = "Spring Web", implementation = "UriUtils")
    public void reservedPercentEncode(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkReservedPercentEncode(benchResult, numberOfEntities, urlEncoder);
    }

    @FlowBench(group = "reserved-percent-decode", project = "Spring Web", implementation = "UriUtils")
    public void reservedPercentDecode(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkReservedPercentDecode(benchResult, numberOfEntities, urlDecoder);
    }

    @FlowBench(group = "unicode-percent-encode", project = "Spring Web", implementation = "UriUtils")
    public void unicodePercentEncode(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkUnicodePercentEncode(benchResult, numberOfEntities, urlEncoder);
    }

    @FlowBench(group = "unicode-percent-decode", project = "Spring Web", implementation = "UriUtils")
    public void unicodePercentDecode(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkUnicodePercentDecode(benchResult, numberOfEntities, urlDecoder);
    }

    @FlowBench(group = "html-escape", project = "Spring Web", implementation = "HtmlUtils-UTF-8")
    public void htmlEscapeUTF8(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkHtmlEscape(benchResult, numberOfEntities, s -> HtmlUtils.htmlEscape(s, "UTF-8"));
    }

    @FlowBench(group = "html-escape", project = "Spring Web", implementation = "HtmlUtils-ISO-8859-1")
    public void htmlEscapeISO(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkHtmlEscape(benchResult, numberOfEntities, s -> HtmlUtils.htmlEscape(s, "ISO-8859-1"));
    }

    @FlowBench(group = "html-unescape", project = "Spring Web", implementation = "HtmlUtils")
    public void htmlUnescape(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkHtmlUnescape(benchResult, numberOfEntities, HtmlUtils::htmlUnescape);
    }

    @FlowBench(group = "javascript-escape", project = "Spring Web", implementation = "JavaScriptUtils")
    public void javaScriptEscape(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkJavaScriptEscape(benchResult, numberOfEntities, JavaScriptUtils::javaScriptEscape);
    }
}
