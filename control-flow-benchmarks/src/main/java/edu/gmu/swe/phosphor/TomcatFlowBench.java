package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.buf.UDecoder;
import org.apache.tomcat.util.buf.UEncoder;

import java.io.IOException;
import java.util.function.UnaryOperator;

import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.*;

public class TomcatFlowBench {

    private static final UnaryOperator<String> wrappedURLEncoder = s -> {
        try {
            UEncoder encoder = new UEncoder(UEncoder.SafeCharsSet.DEFAULT);
            return encoder.encodeURL(s, 0, s.length()).toStringInternal();
        } catch(IOException e) {
            throw new IllegalArgumentException();
        }
    };
    private static final UnaryOperator<String> wrappedURLDecoder = s -> {
        ByteChunk chunk = new ByteChunk();
        chunk.setBytes(s.getBytes(), 0, s.getBytes().length);
        UDecoder decoder = new UDecoder();
        try {
            decoder.convert(chunk, true);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return chunk.toStringInternal();
    };

    @FlowBench
    public void hexEncode(FlowBenchResultImpl benchResult) {
        checkHexEncode(benchResult, TaintedPortionPolicy.ALL, HexUtils::toHexString);
    }

    @FlowBench
    public void hexDecode(FlowBenchResultImpl benchResult) {
        checkHexDecode(benchResult, TaintedPortionPolicy.ALL, HexUtils::fromHexString);
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
}
