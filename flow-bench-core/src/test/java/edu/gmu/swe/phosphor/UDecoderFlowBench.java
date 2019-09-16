package edu.gmu.swe.phosphor;

import edu.columbia.cs.psl.phosphor.maven.MultiLabelFlowBenchResult;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.UDecoder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class UDecoderFlowBench extends BaseFlowBench {

    @FlowBench
    public void testUDecoder(MultiLabelFlowBenchResult benchResult) throws IOException {
        // Decode string: reserved characters !*'();:@&=+$,/?#[]
        String input = taintWithIndices("reserved+characters+!*'()%3b%3a%40%26%3d%2b$,%2f%3f%23%5b%5d");
        ByteChunk bytes = new ByteChunk();
        bytes.setBytes(input.getBytes(), 0, input.getBytes().length);
        UDecoder decoder = new UDecoder();
        decoder.convert(bytes, true);
        String output = bytes.toStringInternal();
        for(int inputIndex = 0, outputIndex = 0; inputIndex < input.length(); inputIndex++, outputIndex++) {
            if(input.charAt(inputIndex) == '%') {
                benchResult.check(Arrays.asList(inputIndex, ++inputIndex, ++inputIndex), output.charAt(outputIndex));
            } else {
                benchResult.check(Collections.singletonList(inputIndex), output.charAt(outputIndex));
            }
        }
    }
}
