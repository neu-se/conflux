package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResult;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static edu.gmu.swe.phosphor.FlowBenchUtil.taintWithIndices;

public class ControlFlowBenchUtil {

    public static final String RESERVED_CHARS_FOR_PERCENT_ENCODING = ":@&?%";

    /**
     * Treats the specified source lists as a circular list and returns a list containing the first n elements from this
     * circular list.
     */
    public static <T> List<T> takeN(List<T> source, int n) {
        return IntStream.range(0, n).mapToObj(i -> source.get(i % source.size())).collect(Collectors.toList());
    }

    public static void checkTransformer(List<String> entities, List<Integer> manyPerOnes, FlowBenchResult benchResult,
                                        int numberOfEntities, UnaryOperator<String> transformer, boolean oneToMany) {
        String input = taintWithIndices(String.join("", takeN(entities, numberOfEntities)));
        String output = transformer.apply(input);
        manyPerOnes = takeN(manyPerOnes, numberOfEntities);
        if(oneToMany) {
            checkOneInputToManyOutputs(benchResult, manyPerOnes, output);
        } else {
            checkManyInputsToOneOutput(benchResult, manyPerOnes, output);
        }
    }

    public static void checkOneInputToManyOutputs(FlowBenchResult benchResult, Iterable<Integer> outputCharsPerInput,
                                                  String output) {
        int inputIndex = 0;
        int outputIndex = 0;
        for(int outputChars : outputCharsPerInput) {
            List<Integer> expected = Collections.singletonList(inputIndex++);
            for(int i = 0; i < outputChars; i++) {
                benchResult.check(expected, output.charAt(outputIndex++));
            }
        }
    }

    public static void checkManyInputsToOneOutput(FlowBenchResult benchResult, Iterable<Integer> inputCharsPerOutput,
                                                  String output) {
        int inputIndex = 0;
        int outputIndex = 0;
        for(int inputChars : inputCharsPerOutput) {
            Set<Integer> expected = IntStream.range(inputIndex, inputIndex += inputChars)
                    .boxed()
                    .collect(Collectors.toSet());
            benchResult.check(expected, output.charAt(outputIndex++));
        }
    }

    public static void checkManyInputsToOneOutput(FlowBenchResult benchResult, Iterable<Integer> inputCharsPerOutput,
                                                  byte[] output) {
        int inputIndex = 0;
        int outputIndex = 0;
        for(int inputChars : inputCharsPerOutput) {
            Set<Integer> expected = IntStream.range(inputIndex, inputIndex += inputChars)
                    .boxed()
                    .collect(Collectors.toSet());
            benchResult.check(expected, output[outputIndex++]);
        }
    }

    /**
     * Converts an array of bytes into a string of hexadecimal digits
     */
    public static void checkHexEncode(FlowBenchResult benchResult, int numberOfEntities, Function<byte[], String> encoder) {
        List<Byte> entities = Arrays.asList((byte) 126, (byte) 74, (byte) -79, (byte) 32);
        entities = takeN(entities, numberOfEntities);
        byte[] input = new byte[entities.size()];
        int i = 0;
        for(Byte entity : entities) {
            input[i] = entity;
        }
        taintWithIndices(input);
        String output = encoder.apply(input);
        List<Integer> outputCharsPerInput = takeN(Collections.singletonList(2), numberOfEntities);
        checkOneInputToManyOutputs(benchResult, outputCharsPerInput, output);
    }

    /**
     * Converts a string of hexadecimal digits to an array of bytes
     */
    public static void checkHexDecode(FlowBenchResult benchResult, int numberOfEntities, Function<String, byte[]> decoder) {
        List<String> entities = Arrays.asList("7e", "4a", "b1", "20");
        String input = taintWithIndices(String.join("", takeN(entities, numberOfEntities)));
        byte[] output = decoder.apply(input);
        List<Integer> inputCharsPerOutput = takeN(Collections.singletonList(2), numberOfEntities);
        checkManyInputsToOneOutput(benchResult, inputCharsPerOutput, output);
    }

    /**
     * Checks taint propagation when encoding a string of spaces characters as '+'s as is done in the
     * 'www-form-urlencoded' encoding scheme.
     */
    public static void checkSpacesUrlEncode(FlowBenchResult benchResult, int numberOfEntities, UnaryOperator<String> encoder) {
        checkTransformer(Collections.singletonList(" "), Collections.singletonList(1), benchResult, numberOfEntities,
                encoder, true);
    }

    /**
     * Checks taint propagation when decoding a string of '+' characters as spaces as is done when decoding the
     * 'www-form-urlencoded' encoding scheme.
     */
    public static void checkSpacesUrlDecode(FlowBenchResult benchResult, int numberOfEntities, UnaryOperator<String> decoder) {
        checkTransformer(Collections.singletonList("+"), Collections.singletonList(1), benchResult, numberOfEntities,
                decoder, false);
    }

    /**
     * Checks taint propagation when encoding reserved ascii characters to percent-encoded octets.
     */
    public static void checkReservedPercentEncode(FlowBenchResult benchResult, int numberOfEntities, UnaryOperator<String> encoder) {
        List<String> entities = new LinkedList<>();
        for(char c : RESERVED_CHARS_FOR_PERCENT_ENCODING.toCharArray()) {
            entities.add(Character.toString(c));
        }
        checkTransformer(entities, Collections.singletonList(3), benchResult, numberOfEntities,
                encoder, true);
    }

    /**
     * Checks taint propagation when decoding reserved ascii characters from percent-encoded octets.
     */
    public static void checkReservedPercentDecode(FlowBenchResult benchResult, int numberOfEntities, UnaryOperator<String> decoder) {
        checkTransformer(Arrays.asList("%3a", "%40", "%26", "%3f", "%25"), Collections.singletonList(3), benchResult,
                numberOfEntities, decoder, false);
    }

    /**
     * Checks taint propagation when encoding unicode characters to percent-encoded octets.
     */
    public static void checkUnicodePercentEncode(FlowBenchResult benchResult, int numberOfEntities, UnaryOperator<String> encoder) {
        checkTransformer(Arrays.asList("\u03A9", "\u3399"), Arrays.asList(6, 9), benchResult, numberOfEntities,
                encoder, true);
    }

    /**
     * Checks taint propagation when decoding unicode characters from percent-encoded octets.
     */
    public static void checkUnicodePercentDecode(FlowBenchResult benchResult, int numberOfEntities, UnaryOperator<String> decoder) {
        checkTransformer(Arrays.asList("%CE%A9", "%E3%8E%99"), Arrays.asList(6, 9), benchResult, numberOfEntities,
                decoder, false);
    }

    /**
     * Checks taint propagation when escaping HTML reserved characters by replacing them with named character entities.
     */
    public static void checkHtmlEscape(FlowBenchResult benchResult, int numberOfEntities, UnaryOperator<String> escaper) {
        checkTransformer(Arrays.asList("&", "<", ">"), Arrays.asList(5, 4, 4), benchResult, numberOfEntities,
                escaper, true);
    }

    /**
     * Checks taint propagation when unescaping named character entities to HTML reserved characters;
     */
    public static void checkHtmlUnescape(FlowBenchResult benchResult, int numberOfEntities, UnaryOperator<String> unescaper) {
        checkTransformer(Arrays.asList("&amp;", "&lt;", "&gt;"), Arrays.asList(5, 4, 4), benchResult, numberOfEntities,
                unescaper, false);
    }

    /**
     * Checks taint propagation when escaping EcmaScript/JavaScript special characters.
     */
    public static void checkJavaScriptEscape(FlowBenchResult benchResult, int numberOfEntities, UnaryOperator<String> escaper) {
        checkTransformer(Arrays.asList("\"", "'", "\\", "/", "\t", "\n", "\f", "\b", "\u2028", "\u2029"),
                Arrays.asList(2, 2, 2, 2, 2, 2, 2, 2, 6, 6), benchResult, numberOfEntities, escaper, true);
    }

    /**
     * Checks taint propagation when unescaping EcmaScript/JavaScript special characters.
     */
    public static void checkJavaScriptUnescape(FlowBenchResult benchResult, int numberOfEntities, UnaryOperator<String> unescaper) {
        checkTransformer(Arrays.asList("\\\"", "\\'", "\\\\", "\\/", "\\t", "\\n", "\\f", "\\b", "\\u2028", "\\u2029"),
                Arrays.asList(2, 2, 2, 2, 2, 2, 2, 2, 6, 6), benchResult, numberOfEntities, unescaper, false);
    }

    /**
     * Quoted-Printable content-transfer-encoding as defined by RFC 1521
     */
    public static void checkQuotedPrintableEncode(FlowBenchResult benchResult, int numberOfEntities, UnaryOperator<String> encoder) {
        checkTransformer(Arrays.asList("\r", "\n", "\b", "\u03A9", "\u3399"), Arrays.asList(3, 3, 3, 6, 9),
                benchResult, numberOfEntities, encoder, true);
    }

    /**
     * Checks taint propagation when decoding Quoted-Printable content-transfer-encoding as defined by RFC 1521.
     */
    public static void checkQuotedPrintableDecode(FlowBenchResult benchResult, int numberOfEntities,
                                                  UnaryOperator<String> decoder) {
        checkTransformer(Arrays.asList("=0D", "=0A", "=08", "=CE=A9", "=E3=8E=99"), Arrays.asList(3, 3, 3, 6, 9),
                benchResult, numberOfEntities, decoder, false);
    }
}
