package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.JavaScriptUtils;
import org.springframework.web.util.UriUtils;

import java.util.function.UnaryOperator;

import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.*;

public class SpringWebFlowBench {

    private static final UnaryOperator<String> urlEncoder = s -> UriUtils.encode(s, "UTF-8");
    private static final UnaryOperator<String> urlDecoder = s -> UriUtils.decode(s, "UTF-8");

    @FlowBench(group = "reserved-percent-encode", project = "spring-web", implementation = "UriUtils")
    public void reservedPercentEncode(FlowBenchResultImpl benchResult) {
        checkReservedPercentEncode(benchResult, TaintedPortionPolicy.ALL, urlEncoder);
    }

    @FlowBench(group = "reserved-percent-decode", project = "spring-web", implementation = "UriUtils")
    public void reservedPercentDecode(FlowBenchResultImpl benchResult) {
        checkReservedPercentDecode(benchResult, TaintedPortionPolicy.ALL, urlDecoder);
    }

    @FlowBench(group = "unicode-percent-encode", project = "spring-web", implementation = "UriUtils")
    public void unicodePercentEncode(FlowBenchResultImpl benchResult) {
        checkUnicodePercentEncode(benchResult, TaintedPortionPolicy.ALL, urlEncoder);
    }

    @FlowBench(group = "unicode-percent-decode", project = "spring-web", implementation = "UriUtils")
    public void unicodePercentDecode(FlowBenchResultImpl benchResult) {
        checkUnicodePercentDecode(benchResult, TaintedPortionPolicy.ALL, urlDecoder);
    }

    @FlowBench(group = "html-escape", project = "spring-web", implementation = "HtmlUtils-UTF-8")
    public void htmlEscapeUTF8(FlowBenchResultImpl benchResult) {
        checkHtmlEscape(benchResult, TaintedPortionPolicy.ALL, s -> HtmlUtils.htmlEscape(s, "UTF-8"));
    }

    @FlowBench(group = "html-escape", project = "spring-web", implementation = "HtmlUtils-ISO-8859-1")
    public void htmlEscapeISO(FlowBenchResultImpl benchResult) {
        checkHtmlEscape(benchResult, TaintedPortionPolicy.ALL, s -> HtmlUtils.htmlEscape(s, "ISO-8859-1"));
    }

    @FlowBench(group = "html-unescape", project = "spring-web", implementation = "HtmlUtils")
    public void htmlUnescape(FlowBenchResultImpl benchResult) {
        checkHtmlUnescape(benchResult, TaintedPortionPolicy.ALL, HtmlUtils::htmlUnescape);
    }

    @FlowBench(group = "javascript-escape", project = "spring-web", implementation = "JavaScriptUtils")
    public void javaScriptEscape(FlowBenchResultImpl benchResult) {
        checkJavaScriptEscape(benchResult, TaintedPortionPolicy.ALL, JavaScriptUtils::javaScriptEscape);
    }
}
