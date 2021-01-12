package edu.neu.ccs.conflux;

import edu.neu.ccs.conflux.internal.FlowBench;
import edu.neu.ccs.conflux.internal.runtime.BenchTaintTagChecker;
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
    public void hexEncode(BenchTaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkHexEncode(checker, numberOfEntities, HexUtils::toHexString);
    }

    @FlowBench(group = "hex-decode", project = "Tomcat Embed Core", implementation = "HexUtils")
    public void hexDecode(BenchTaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkHexDecode(checker, numberOfEntities, HexUtils::fromHexString);
    }

    @FlowBench(group = "spaces-url-decode", project = "Tomcat Embed Core", implementation = "UDecoder")
    public void spacesUrlDecode(BenchTaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkSpacesUrlDecode(checker, numberOfEntities, wrappedURLDecoder);
    }

    @FlowBench(group = "reserved-percent-encode", project = "Tomcat Embed Core", implementation = "UEncoder")
    public void reservedPercentEncode(BenchTaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkReservedPercentEncode(checker, numberOfEntities, wrappedURLEncoder);
    }

    @FlowBench(group = "reserved-percent-decode", project = "Tomcat Embed Core", implementation = "UDecoder")
    public void reservedPercentDecode(BenchTaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkReservedPercentDecode(checker, numberOfEntities, wrappedURLDecoder);
    }
}
