package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.maven.MultiLabelFlowBenchResult;

import java.util.*;

/**
 * Tests implicit flows found in the package java.util
 */
public class UtilFlowBench extends BaseFlowBench {

    @FlowBench
    public void testBase64Encode(MultiLabelFlowBenchResult benchResult) {
        String value = "Lorem ipsum dolor sit amett"; // Note: length of value is divisible by 3
        byte[] input = (value + value).getBytes();
        int taintedLen = input.length/2;
        taintWithIndices(input, 0, taintedLen);// Only taint half of the input
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] output = encoder.encode(input);
        for(int outputIndex = 0; outputIndex < output.length; outputIndex++) {
            Collection<Object> expected;
            int block = outputIndex / 4;
            if(block * 3 < taintedLen) {
                int blockIndex = outputIndex % 4;
                switch(blockIndex) {
                    case 0:
                        expected = Collections.singletonList(block * 3);
                        break;
                    case 1:
                        expected = Arrays.asList(block * 3, block * 3 + 1);
                        break;
                    case 2:
                        expected = Arrays.asList(block * 3 + 1, block * 3 + 2);
                        break;
                    default:
                        expected = Collections.singletonList(block * 3 + 2);
                }
            } else {
                expected = new HashSet<>();
            }
            // Ideally this is what you would want to see, but the way the code is written, I'm not sure its feasible to
            // have it work out this way
            benchResult.check(expected, output[outputIndex]);
        }
    }

    @FlowBench
    public void testBase64Decode(MultiLabelFlowBenchResult benchResult) {
        String value = "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXR0";
        byte[] input = (value + value).getBytes();
        int taintedLen = input.length/2;
        taintWithIndices(input, 0, taintedLen);// Only taint half of the input
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] output = decoder.decode(input);
        for(int outputIndex = 0; outputIndex < output.length; outputIndex++) {
            Collection<Object> expected;
            int block = outputIndex / 3;
            if(block * 4 < taintedLen) {
                int blockIndex = outputIndex % 3;
                switch(blockIndex) {
                    case 0:
                        expected = Arrays.asList(block * 4, block * 4 + 1);
                        break;
                    case 1:
                        expected = Arrays.asList(block * 4 + 1, block * 4 + 2);
                        break;
                    default:
                        expected = Arrays.asList(block * 4 + 2, block * 4 + 3);
                }
            } else {
                expected = new HashSet<>();
            }
            // Ideally this is what you would want to see, but the way the code is written, I'm not sure its feasible to
            // have it work out this way
            benchResult.check(expected, output[outputIndex]);
        }
    }
}
