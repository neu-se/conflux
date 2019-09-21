package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.MultiLabelFlowBenchResult;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.UDecoder;
import org.apache.tomcat.util.buf.UEncoder;
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static edu.gmu.swe.phosphor.FlowBenchUtil.taintWithIndices;

public class StrictControlFlowBench {

    /**
     * Unescapes HTML named character entities using jsoup's Parser class. There is a control flow, but not a data flow
     * between escaped entities and the unescaped values produced from them.
     */
    @FlowBench
    public void testParserUnescapeEntities(MultiLabelFlowBenchResult benchResult, TaintedPortionPolicy policy) {
        String input = "eget nullam &quot;&amp;&lt;&gt; non nisi est ";
        input = taintWithIndices(input + input, policy);
        String output = Parser.unescapeEntities(input, true);
        for(int inputIndex = 0, outputIndex = 0; inputIndex < input.length(); inputIndex++, outputIndex++) {
            LinkedList<Object> expected = new LinkedList<>();
            expected.add(inputIndex);
            if(input.charAt(inputIndex) == '&') {
                while(input.charAt(inputIndex) != ';') {
                    expected.add(++inputIndex);
                }
            }
            if(policy.inTaintedRange(inputIndex, input.length())) {
                benchResult.check(expected, output.charAt(outputIndex));
            } else {
                benchResult.checkEmpty(output.charAt(outputIndex));
            }
        }
    }

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
}
