package edu.gmu.swe.phosphor;

import edu.columbia.cs.psl.phosphor.maven.MultiLabelFlowBenchResult;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.buf.UDecoder;
import org.apache.tomcat.util.buf.UEncoder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class TomcatFlowBench extends BaseFlowBench {

    @FlowBench
    public void testUDecoderDecode(MultiLabelFlowBenchResult benchResult) throws IOException {
        String encodedStr = "reserved+characters+!*'()%3b%3a%40%26%3d%2b$,%2f%3f%23%5b%5d";
        String input = taintWithIndices(encodedStr);
        input += encodedStr; // Only taint the first half of the string
        ByteChunk bytes = new ByteChunk();
        bytes.setBytes(input.getBytes(), 0, input.getBytes().length);
        UDecoder decoder = new UDecoder();
        decoder.convert(bytes, true);
        String output = bytes.toStringInternal();
        for(int inputIndex = 0, outputIndex = 0; inputIndex < input.length(); inputIndex++, outputIndex++) {
            if(inputIndex < encodedStr.length()) {
                if(input.charAt(inputIndex) == '%') {
                    benchResult.check(Arrays.asList(inputIndex, ++inputIndex, ++inputIndex), output.charAt(outputIndex));
                } else {
                    benchResult.check(Collections.singletonList(inputIndex), output.charAt(outputIndex));
                }
            } else {
                benchResult.check(new HashSet<>(), output.charAt(outputIndex));
                if(input.charAt(inputIndex) == '%') {
                    inputIndex += 2;
                }
            }
        }
    }

    @FlowBench
    public void testUEncoderEncode(MultiLabelFlowBenchResult benchResult) throws IOException {
        String decodedStr = "reserved characters !*'();:@&=+$,/?#[]";
        String input = taintWithIndices(decodedStr);
        input += decodedStr; // Only taint the first half of the string
        UEncoder encoder = new UEncoder(UEncoder.SafeCharsSet.DEFAULT);
        String output = encoder.encodeURL(input, 0, input.length()).toStringInternal();
        for(int inputIndex = 0, outputIndex = 0; inputIndex < input.length(); inputIndex++, outputIndex++) {
            if(inputIndex < decodedStr.length()) {
                benchResult.check(Collections.singletonList(inputIndex), output.charAt(outputIndex));
                if(output.charAt(outputIndex) == '%') {
                    benchResult.check(Collections.singletonList(inputIndex), output.charAt(++outputIndex));
                    benchResult.check(Collections.singletonList(inputIndex), output.charAt(++outputIndex));
                }
            } else {
                benchResult.check(new HashSet<>(), output.charAt(outputIndex));
                if(output.charAt(outputIndex) == '%') {
                    outputIndex += 2;
                }
            }
        }
    }

    @FlowBench
    public void testHexUtilsToHexString(MultiLabelFlowBenchResult benchResult) {
        byte[] input = new byte[]{126, 74, -79, 32, 126, 74, -79, 32};
        input = taintWithIndices(input, 0, 4);
        String output = HexUtils.toHexString(input);
        for(int i = 0; i < output.length(); i++) {
            if(i/2 < input.length/2) {
                benchResult.check(Collections.singletonList(i/2), output.charAt(i));
            } else {
                benchResult.check(new HashSet<>(), output.charAt(i));
            }
        }
    }

    @FlowBench
    public void testHexUtilsFromHexString(MultiLabelFlowBenchResult benchResult) {
        String input = taintWithIndices("7e4ab1207e4ab120", 0, 8); // Only taint the first half of the string
        byte[] output = HexUtils.fromHexString(input);
        for(int i = 0; i < output.length; i++) {
            if(i * 2 < input.length()/2) {
                benchResult.check(Arrays.asList(i*2, i*2+1), output[i]);
            } else {
                benchResult.check(new HashSet<>(), output[i]);
            }
        }
    }
}