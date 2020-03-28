package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.buf.UDecoder;
import org.apache.tomcat.util.buf.UEncoder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.checkHexDecode;
import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.checkHexEncode;
import static edu.gmu.swe.phosphor.FlowBenchUtil.taintWithIndices;

public class TomcatFlowBench {

    /**
     * Converts an array of bytes into a string of hexadecimal digits
     */
    @FlowBench
    public void hexEncode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy) {
        checkHexEncode(benchResult, policy, HexUtils::toHexString);
    }

    /**
     * Converts a string of hexadecimal digits to an array of bytes
     */
    @FlowBench
    public void hexDecode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy) {
        checkHexDecode(benchResult, policy, HexUtils::fromHexString);
    }

    /**
     * Decodes percent encoded bytes and spaces encoded as plus signs.
     */
    @FlowBench
    public void uDecoderDecode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy) throws IOException {
        String value = "purus+faucibus+ornare+suspendisse+%3b%3a%40%26%3d%2b%2f%3f%23%5b%5d";
        value += value;
        char[] input = taintWithIndices(value.toCharArray(), policy);
        CharChunk chunk = new CharChunk();
        chunk.setChars(input.clone(), 0, input.length);
        UDecoder decoder = new UDecoder();
        decoder.convert(chunk, true);
        String output = chunk.toStringInternal();
        for(int inputIndex = 0, outputIndex = 0; inputIndex < input.length; inputIndex++, outputIndex++) {
            List<Object> expected;
            if(input[inputIndex] == '%') {
                expected = Arrays.asList(inputIndex, ++inputIndex, ++inputIndex);
            } else {
                expected = Collections.singletonList(inputIndex);
            }
            if(policy.inTaintedRange(inputIndex, input.length)) {
                benchResult.check(expected, output.charAt(outputIndex));
            } else {
                benchResult.checkEmpty(output.charAt(outputIndex));
            }
        }
    }

    /**
     * Percent encodes URL reserved characters.
     */
    @FlowBench
    public void uEncoderEncode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy) throws IOException {
        String input = "purus faucibus ornare suspendisse ;:@&=+/?#[]";
        input = taintWithIndices(input + input, policy);
        UEncoder encoder = new UEncoder(UEncoder.SafeCharsSet.DEFAULT);
        String output = encoder.encodeURL(input, 0, input.length()).toStringInternal();
        for(int inputIndex = 0, outputIndex = 0; inputIndex < input.length(); inputIndex++, outputIndex++) {
            List<Object> expected = Collections.emptyList();
            if(policy.inTaintedRange(inputIndex, input.length())) {
                expected = Collections.singletonList(inputIndex);
            }
            benchResult.check(expected, output.charAt(outputIndex));
            if(output.charAt(outputIndex) == '%') {
                benchResult.check(expected, output.charAt(++outputIndex));
                benchResult.check(expected, output.charAt(++outputIndex));
            }
        }
    }
}
