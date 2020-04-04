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

    @FlowBench
    public void urlEncodeReserved(FlowBenchResultImpl benchResult) {
        checkPercentEncodeReserved(benchResult, TaintedPortionPolicy.ALL, urlEncoder);
    }

    @FlowBench
    public void urlDecodeReserved(FlowBenchResultImpl benchResult) {
        checkPercentDecodeReserved(benchResult, TaintedPortionPolicy.ALL, urlDecoder);
    }

    @FlowBench
    public void urlEncodeUnicode(FlowBenchResultImpl benchResult) {
        checkPercentEncodeUnicode(benchResult, TaintedPortionPolicy.ALL, urlEncoder);
    }

    @FlowBench
    public void urlDecodeUnicode(FlowBenchResultImpl benchResult) {
        checkPercentDecodeUnicode(benchResult, TaintedPortionPolicy.ALL, urlDecoder);
    }

    @FlowBench
    public void htmlEscapeUTF8(FlowBenchResultImpl benchResult) {
        checkEscapeHtml(benchResult, TaintedPortionPolicy.ALL, s -> HtmlUtils.htmlEscape(s, "UTF-8"));
    }

    @FlowBench
    public void htmlEscapeISO(FlowBenchResultImpl benchResult) {
        checkEscapeHtml(benchResult, TaintedPortionPolicy.ALL, s -> HtmlUtils.htmlEscape(s, "ISO-8859-1"));
    }

    @FlowBench
    public void htmlUnescape(FlowBenchResultImpl benchResult) {
        checkUnescapeHtml(benchResult, TaintedPortionPolicy.ALL, HtmlUtils::htmlUnescape);
    }

    @FlowBench
    public void javaScriptEscape(FlowBenchResultImpl benchResult) {
        checkEscapeJavaScript(benchResult, TaintedPortionPolicy.ALL, JavaScriptUtils::javaScriptEscape);
    }
}
