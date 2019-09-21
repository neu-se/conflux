package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.MultiLabelFlowBenchResult;
import org.apache.tomcat.util.buf.HexUtils;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;

import static edu.gmu.swe.phosphor.FlowBenchUtil.taintWithIndices;

public class ArrayIndexFlowBench {

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
