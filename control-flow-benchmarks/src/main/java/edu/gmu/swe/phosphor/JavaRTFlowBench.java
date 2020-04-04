package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.function.UnaryOperator;

import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.*;

public class JavaRTFlowBench {

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

    @FlowBench
    public void hexEncode(FlowBenchResultImpl benchResult) {
        checkHexEncode(benchResult, TaintedPortionPolicy.ALL, DatatypeConverter::printHexBinary);
    }

    @FlowBench
    public void hexDecode(FlowBenchResultImpl benchResult) {
        checkHexDecode(benchResult, TaintedPortionPolicy.ALL, DatatypeConverter::parseHexBinary);
    }

    @FlowBench
    public void urlEncodeSpaces(FlowBenchResultImpl benchResult) {
        checkURLEncodeSpaces(benchResult, TaintedPortionPolicy.ALL, wrappedURLEncoder);
    }

    @FlowBench
    public void urlDecodeSpaces(FlowBenchResultImpl benchResult) {
        checkURLDecodeSpaces(benchResult, TaintedPortionPolicy.ALL, wrappedURLDecoder);
    }

    @FlowBench
    public void urlEncodeReserved(FlowBenchResultImpl benchResult) {
        checkPercentEncodeReserved(benchResult, TaintedPortionPolicy.ALL, wrappedURLEncoder);
    }

    @FlowBench
    public void urlDecodeReserved(FlowBenchResultImpl benchResult) {
        checkPercentDecodeReserved(benchResult, TaintedPortionPolicy.ALL, wrappedURLDecoder);
    }

    @FlowBench
    public void urlEncodeUnicode(FlowBenchResultImpl benchResult) {
        checkPercentEncodeUnicode(benchResult, TaintedPortionPolicy.ALL, wrappedURLEncoder);
    }

    @FlowBench
    public void urlDecodeUnicode(FlowBenchResultImpl benchResult) {
        checkPercentDecodeUnicode(benchResult, TaintedPortionPolicy.ALL, wrappedURLDecoder);
    }
}
