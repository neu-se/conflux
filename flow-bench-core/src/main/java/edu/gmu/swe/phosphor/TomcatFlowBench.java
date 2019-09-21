package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.MultiLabelFlowBenchResult;
import org.apache.tomcat.util.buf.*;

import java.io.IOException;
import java.util.*;

import static edu.gmu.swe.phosphor.FlowBenchUtil.taintWithIndices;

/**
 * Tests implicit flows found in Apache Tomcat
 */
public class TomcatFlowBench {

    /**
     * Decodes percent encoded bytes and spaces encoded as plus signs using Tomcat's UDecoder class. There is a control
     * flow, but not a data flow between values decoded from percent encoded bytes and the percent signs. There is a
     * control flow, but not a data flow between decoded spaces and the plus signs they were decoded from.
     */
    @FlowBench
    public void testUDecoderDecode(MultiLabelFlowBenchResult benchResult, TaintedPortionPolicy policy) throws IOException {
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
     * Percent encodes URL reserved characters using Tomcat's UEncoder class. There is a control flow, but not a data
     * flow between the percent sign of percent encoded characters and the original, reserved character that is encoded.
     */
    @FlowBench
    public void testUEncoderEncode(MultiLabelFlowBenchResult benchResult, TaintedPortionPolicy policy) throws IOException {
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

    /**
     * Converts an array of bytes into a string of hexadecimal digits using Tomcat's HexUtils class. Translation is done
     * by using values derived from the input as indices into an array.
     */
    @FlowBench
    public void testHexUtilsToHexString(MultiLabelFlowBenchResult benchResult, TaintedPortionPolicy policy) {
        byte[] input = taintWithIndices(new byte[]{126, 74, -79, 32, 126, 74, -79, 32}, policy);
        String output = HexUtils.toHexString(input);
        for(int i = 0; i < input.length; i++) {
            if(policy.inTaintedRange(i, input.length)) {
                benchResult.check(Collections.singletonList(i), output.charAt(i * 2));
                benchResult.check(Collections.singletonList(i), output.charAt(i * 2 + 1));
            } else {
                benchResult.checkEmpty(output.charAt(i * 2));
                benchResult.checkEmpty(output.charAt(i * 2 + 1));
            }
        }
    }

    /**
     * Converts a string of hexadecimal digits to an array of bytes using Tomcat's HexUtils class. Translation is done
     * by using values derived from the input as indices into an array.
     */
    @FlowBench
    public void testHexUtilsFromHexString(MultiLabelFlowBenchResult benchResult, TaintedPortionPolicy policy) {
        String input = taintWithIndices("7e4ab1207e4ab120", policy);
        byte[] output = HexUtils.fromHexString(input);
        for(int i = 0; i < input.length(); i+=2) {
            if(policy.inTaintedRange(i, input.length())) {
                benchResult.check(Arrays.asList(i, i+1), output[i/2]);
            } else {
                benchResult.checkEmpty(output[i/2]);
            }
        }
    }
}