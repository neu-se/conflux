package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.MultiLabelFlowBenchResult;

import java.util.*;

import static edu.gmu.swe.phosphor.FlowBenchUtil.taintWithIndices;

/**
 * Tests implicit flows found in the package java.util
 */
public class UtilFlowBench {

    /**
     * Base64 encodes and then decodes a string using java.util.Base64. Checks that the output is labeled the same as
     * the input.
     */
    @FlowBench(requiresBitLevelPrecision = true)
    public void testBase64RoundTrip(MultiLabelFlowBenchResult benchResult, TaintedPortionPolicy policy) {
        String value = "Lorem ipsum dolor sit amett"; // Note: length of value is divisible by 3
        byte[] input = taintWithIndices((value + value).getBytes(), policy);
        byte[] output = Base64.getDecoder().decode(Base64.getEncoder().encode(input.clone()));
        for(int inputIndex = 0; inputIndex < input.length; inputIndex++) {
            if(policy.inTaintedRange(inputIndex, input.length)) {
                benchResult.check(Collections.singletonList(inputIndex), output[inputIndex]);
            } else {
                benchResult.checkEmpty(output[inputIndex]);
            }
        }
    }

    /**
     * Base64 encodes a string using java.util.Base64. Checks that the union of the tags on groups of four output
     * characters match the tags on the three input characters that were used to create the output group.
     */
    @FlowBench
    public void testBase64Encode(MultiLabelFlowBenchResult benchResult, TaintedPortionPolicy policy) {
        String value = "Lorem ipsum dolor sit amett"; // Note: length of value is divisible by 3
        byte[] input = taintWithIndices((value + value).getBytes(), policy);
        byte[] output = Base64.getEncoder().encode(input);
        for(int inputIndex = 0; inputIndex < input.length; inputIndex+=3) {
            byte[] outputGroup = new byte[4];
            System.arraycopy(output, inputIndex/3 * 4, outputGroup, 0, outputGroup.length);
            if(policy.inTaintedRange(inputIndex, input.length)) {
                benchResult.check(Arrays.asList(inputIndex, inputIndex + 1, inputIndex + 2), outputGroup);
            } else {
                benchResult.checkEmpty(outputGroup);
            }
        }
    }

    /**
     * Base64 decodes a string using java.util.Base64. Checks that the union of the tags on groups of three output
     * characters match the tags on the four input characters that were used to create the output group.
     */
    @FlowBench
    public void testBase64Decode(MultiLabelFlowBenchResult benchResult, TaintedPortionPolicy policy) {
        String value = "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXR0";
        byte[] input = taintWithIndices((value + value).getBytes(), policy);
        byte[] output = Base64.getDecoder().decode(input);
        for(int inputIndex = 0; inputIndex < input.length; inputIndex+=4) {
            byte[] outputGroup = new byte[3];
            System.arraycopy(output, inputIndex/4 * 3, outputGroup, 0, outputGroup.length);
            if(policy.inTaintedRange(inputIndex, input.length)) {
                benchResult.check(Arrays.asList(inputIndex, inputIndex + 1, inputIndex + 2, inputIndex + 3), outputGroup);
            } else {
                benchResult.checkEmpty(outputGroup);
            }
        }
    }
}
