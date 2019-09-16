package edu.gmu.swe.phosphor;

import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.UDecoder;
import org.apache.tomcat.util.buf.UEncoder;

import java.io.IOException;

@SuppressWarnings("unused")
public class UDecoderFlowBench extends BaseFlowBench {

    @FlowBench
    public FlowBenchResult testUDecoder() throws IOException {
        UEncoder encoder = new UEncoder(UEncoder.SafeCharsSet.DEFAULT);
        String input = "reserved characters !*'();:@&=+$,/?#[]";
        CharChunk inputChars = encoder.encodeURL(input, 0, input.length());
        input = taintWithIndices(inputChars.toStringInternal());
        ByteChunk bytes = new ByteChunk();
        bytes.setBytes(input.getBytes(), 0, input.getBytes().length);
        UDecoder decoder = new UDecoder();
        decoder.convert(bytes, true);
        CharChunk chars = encoder.encodeURL(bytes.toStringInternal(), 0, bytes.toStringInternal().length());
        String output = chars.toStringInternal();
        return calculateMultiLabelResult(getTaints(input), getTaints(output));
    }
}
