package edu.neu.ccs.conflux.bench;

import edu.neu.ccs.conflux.FlowBench;
import edu.neu.ccs.conflux.internal.runtime.FlowBenchResult;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.buf.UDecoder;
import org.apache.tomcat.util.buf.UEncoder;

import java.io.IOException;
import java.util.function.UnaryOperator;

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

    @FlowBench(group = "hex-encode", project = "Tomcat Embed Core", implementation = "HexUtils")
    public void hexEncode(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkHexEncode(benchResult, numberOfEntities, HexUtils::toHexString);
    }

    @FlowBench(group = "hex-decode", project = "Tomcat Embed Core", implementation = "HexUtils")
    public void hexDecode(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkHexDecode(benchResult, numberOfEntities, HexUtils::fromHexString);
    }

    @FlowBench(group = "spaces-url-decode", project = "Tomcat Embed Core", implementation = "UDecoder")
    public void spacesUrlDecode(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkSpacesUrlDecode(benchResult, numberOfEntities, wrappedURLDecoder);
    }

    @FlowBench(group = "reserved-percent-encode", project = "Tomcat Embed Core", implementation = "UEncoder")
    public void reservedPercentEncode(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkReservedPercentEncode(benchResult, numberOfEntities, wrappedURLEncoder);
    }

    @FlowBench(group = "reserved-percent-decode", project = "Tomcat Embed Core", implementation = "UDecoder")
    public void reservedPercentDecode(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkReservedPercentDecode(benchResult, numberOfEntities, wrappedURLDecoder);
    }
}
