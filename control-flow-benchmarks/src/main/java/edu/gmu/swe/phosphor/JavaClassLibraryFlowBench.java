package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.function.UnaryOperator;

import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.*;

public class JavaClassLibraryFlowBench {

    private static final UnaryOperator<String> wrappedURLEncoder = s -> {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch(UnsupportedEncodingException e) {
            throw new IllegalStateException();
        }
    };
    private static final UnaryOperator<String> wrappedURLDecoder = s -> {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch(UnsupportedEncodingException e) {
            throw new IllegalStateException();
        }
    };

    @FlowBench(group = "hex-encode", project = "Java Class Library", implementation = "DatatypeConverter")
    public void hexEncode(FlowBenchResultImpl benchResult) {
        checkHexEncode(benchResult, TaintedPortionPolicy.ALL, DatatypeConverter::printHexBinary);
    }

    @FlowBench(group = "hex-decode", project = "Java Class Library", implementation = "DatatypeConverter")
    public void hexDecode(FlowBenchResultImpl benchResult) {
        checkHexDecode(benchResult, TaintedPortionPolicy.ALL, DatatypeConverter::parseHexBinary);
    }

    @FlowBench(group = "spaces-url-encode", project = "Java Class Library", implementation = "URLEncoder")
    public void spacesUrlEncode(FlowBenchResultImpl benchResult) {
        checkSpacesUrlEncode(benchResult, TaintedPortionPolicy.ALL, wrappedURLEncoder);
    }

    @FlowBench(group = "spaces-url-decode", project = "Java Class Library", implementation = "URLDecoder")
    public void spacesUrlDecode(FlowBenchResultImpl benchResult) {
        checkSpacesUrlDecode(benchResult, TaintedPortionPolicy.ALL, wrappedURLDecoder);
    }

    @FlowBench(group = "reserved-percent-encode", project = "Java Class Library", implementation = "URLEncoder")
    public void reservedPercentEncode(FlowBenchResultImpl benchResult) {
        checkReservedPercentEncode(benchResult, TaintedPortionPolicy.ALL, wrappedURLEncoder);
    }

    @FlowBench(group = "reserved-percent-decode", project = "Java Class Library", implementation = "URLDecoder")
    public void reservedPercentDecode(FlowBenchResultImpl benchResult) {
        checkReservedPercentDecode(benchResult, TaintedPortionPolicy.ALL, wrappedURLDecoder);
    }

    @FlowBench(group = "unicode-percent-encode", project = "Java Class Library", implementation = "URLEncoder")
    public void unicodePercentEncode(FlowBenchResultImpl benchResult) {
        checkUnicodePercentEncode(benchResult, TaintedPortionPolicy.ALL, wrappedURLEncoder);
    }

    @FlowBench(group = "unicode-percent-decode", project = "Java Class Library", implementation = "URLDecoder")
    public void unicodePercentDecode(FlowBenchResultImpl benchResult) {
        checkUnicodePercentDecode(benchResult, TaintedPortionPolicy.ALL, wrappedURLDecoder);
    }
}
