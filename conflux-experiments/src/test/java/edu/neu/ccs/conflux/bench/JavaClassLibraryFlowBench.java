package edu.neu.ccs.conflux.bench;

import edu.neu.ccs.conflux.FlowBench;
import edu.neu.ccs.conflux.internal.runtime.FlowBenchResult;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.function.UnaryOperator;

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
    public void hexEncode(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkHexEncode(benchResult, numberOfEntities, DatatypeConverter::printHexBinary);
    }

    @FlowBench(group = "hex-decode", project = "Java Class Library", implementation = "DatatypeConverter")
    public void hexDecode(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkHexDecode(benchResult, numberOfEntities, DatatypeConverter::parseHexBinary);
    }

    @FlowBench(group = "spaces-url-encode", project = "Java Class Library", implementation = "URLEncoder")
    public void spacesUrlEncode(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkSpacesUrlEncode(benchResult, numberOfEntities, wrappedURLEncoder);
    }

    @FlowBench(group = "spaces-url-decode", project = "Java Class Library", implementation = "URLDecoder")
    public void spacesUrlDecode(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkSpacesUrlDecode(benchResult, numberOfEntities, wrappedURLDecoder);
    }

    @FlowBench(group = "reserved-percent-encode", project = "Java Class Library", implementation = "URLEncoder")
    public void reservedPercentEncode(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkReservedPercentEncode(benchResult, numberOfEntities, wrappedURLEncoder);
    }

    @FlowBench(group = "reserved-percent-decode", project = "Java Class Library", implementation = "URLDecoder")
    public void reservedPercentDecode(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkReservedPercentDecode(benchResult, numberOfEntities, wrappedURLDecoder);
    }

    @FlowBench(group = "unicode-percent-encode", project = "Java Class Library", implementation = "URLEncoder")
    public void unicodePercentEncode(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkUnicodePercentEncode(benchResult, numberOfEntities, wrappedURLEncoder);
    }

    @FlowBench(group = "unicode-percent-decode", project = "Java Class Library", implementation = "URLDecoder")
    public void unicodePercentDecode(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkUnicodePercentDecode(benchResult, numberOfEntities, wrappedURLDecoder);
    }
}
