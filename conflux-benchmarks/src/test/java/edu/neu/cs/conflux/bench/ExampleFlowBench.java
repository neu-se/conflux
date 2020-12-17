package edu.neu.cs.conflux.bench;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;

import static edu.gmu.swe.phosphor.FlowBenchUtil.taintWithIndices;

public class ExampleFlowBench {


    //@FlowBench(group = "example", project = "example", implementation = "example")
    public void spaceDecode(FlowBenchResultImpl benchResult, int numberOfEntities) {
        char[] input = "Hello+World".toCharArray();
        taintWithIndices(input);
        char[] output = spaceDecode(input);
    }

    //@FlowBench(group = "example2", project = "example2", implementation = "example2")
    public void percentDecode(FlowBenchResultImpl benchResult, int numberOfEntities) {
        char[] input = "%48%69%21".toCharArray();
        taintWithIndices(input);
        char[] output = percentDecode(input);
    }

    public static char[] spaceDecode(char[] input) {
        char[] result = new char[input.length];
        for(int i = 0; i < input.length; i++) {
            switch(input[i]) {
                case ' ':
                    throw new IllegalArgumentException();
                case '+':
                    result[i] = ' ';
                    break;
                default:
                    result[i] = input[i];
            }
        }
        return result;
    }

    public static char[] percentDecode(char[] input) {
        char[] result = new char[input.length - 2 * count(input, '%')];
        int size = 0;
        for(int i = 0; i < input.length; i++) {
            if(input[i] == '%') {
                result[size++] = hexToChar(input[i + 1], input[i + 2]);
                i += 2;
            } else {
                result[size++] = input[i];
            }
        }
        return result;
    }

    private static int count(char[] input, char target) {
        int count = 0;
        for(char c : input) {
            if(c == target) {
                count++;
            }
        }
        return count;
    }

    private static char hexToChar(char c1, char c2) {
        int digit = (c1 >= 'A') ? ((c1 & 0xDF) - 'A') + 10 : (c1 - '0');
        digit *= 16;
        digit += (c2 >= 'A') ? ((c2 & 0xDF) - 'A') + 10 : (c2 - '0');
        return (char) digit;
    }

    /**
     * Over-tainting in URLDecoder
     */
    public static String urlDecoderDecode(char[] input) {
        char[] output = new char[input.length / 3];
        int outputPosition = 0;
        int inputPosition = 0;
        char c = input[0];
        while(c == '%') { // source of problematic flow, pushVariant +1
            output[outputPosition++] = hexToChar(input[inputPosition + 1], input[inputPosition + 2]);
            inputPosition += 3;
            if(inputPosition + 2 >= input.length) {
                break;
            }
            c = input[inputPosition]; // problematic statement, copyVariant +1
        }
        return new String(output);
    }
}
