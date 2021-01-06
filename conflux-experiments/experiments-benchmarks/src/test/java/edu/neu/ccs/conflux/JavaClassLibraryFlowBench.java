package edu.neu.ccs.conflux;

import edu.neu.ccs.conflux.internal.FlowBench;
import edu.neu.ccs.conflux.internal.runtime.TaintTagChecker;

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
    public void hexEncode(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkHexEncode(checker, numberOfEntities, DatatypeConverter::printHexBinary);
    }

    @FlowBench(group = "hex-decode", project = "Java Class Library", implementation = "DatatypeConverter")
    public void hexDecode(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkHexDecode(checker, numberOfEntities, DatatypeConverter::parseHexBinary);
    }

    @FlowBench(group = "spaces-url-encode", project = "Java Class Library", implementation = "URLEncoder")
    public void spacesUrlEncode(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkSpacesUrlEncode(checker, numberOfEntities, wrappedURLEncoder);
    }

    @FlowBench(group = "spaces-url-decode", project = "Java Class Library", implementation = "URLDecoder")
    public void spacesUrlDecode(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkSpacesUrlDecode(checker, numberOfEntities, wrappedURLDecoder);
    }

    @FlowBench(group = "reserved-percent-encode", project = "Java Class Library", implementation = "URLEncoder")
    public void reservedPercentEncode(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkReservedPercentEncode(checker, numberOfEntities, wrappedURLEncoder);
    }

    @FlowBench(group = "reserved-percent-decode", project = "Java Class Library", implementation = "URLDecoder")
    public void reservedPercentDecode(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkReservedPercentDecode(checker, numberOfEntities, wrappedURLDecoder);
    }

    @FlowBench(group = "unicode-percent-encode", project = "Java Class Library", implementation = "URLEncoder")
    public void unicodePercentEncode(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkUnicodePercentEncode(checker, numberOfEntities, wrappedURLEncoder);
    }

    @FlowBench(group = "unicode-percent-decode", project = "Java Class Library", implementation = "URLDecoder")
    public void unicodePercentDecode(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkUnicodePercentDecode(checker, numberOfEntities, wrappedURLDecoder);
    }
}
