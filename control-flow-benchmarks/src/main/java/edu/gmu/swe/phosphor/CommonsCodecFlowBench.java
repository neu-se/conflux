package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import org.apache.commons.codec.BinaryDecoder;
import org.apache.commons.codec.BinaryEncoder;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.net.PercentCodec;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.apache.commons.codec.net.URLCodec;

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.*;
import static edu.gmu.swe.phosphor.FlowBenchUtil.taintWithIndices;

public class CommonsCodecFlowBench {

    private static final UnaryOperator<String> wrappedURLEncoder = wrapEncoder(new URLCodec());
    private static final UnaryOperator<String> wrappedURLDecoder = wrapDecoder(new URLCodec());
    private static final PercentCodec percentCodec = new PercentCodec(RESERVED_CHARS_FOR_PERCENT_ENCODING.getBytes(), true);
    private static final UnaryOperator<String> wrappedPercentEncoder = wrapEncoder(percentCodec);
    private static final UnaryOperator<String> wrappedPercentDecoder = wrapDecoder(percentCodec);

    @FlowBench(group = "hex-encode", project = "common-codec", implementation = "Hex")
    public void hexEncode(FlowBenchResultImpl benchResult) {
        Hex encoder = new Hex();
        checkHexEncode(benchResult, TaintedPortionPolicy.ALL, b -> new String(encoder.encode(b)));
    }

    @FlowBench(group = "hex-decode", project = "common-codec", implementation = "Hex")
    public void hexDecode(FlowBenchResultImpl benchResult) {
        Hex decoder = new Hex();
        checkHexDecode(benchResult, TaintedPortionPolicy.ALL, s -> {
            try {
                return decoder.decode(s.getBytes());
            } catch(DecoderException e) {
                throw new IllegalArgumentException();
            }
        });
    }

    @FlowBench(group = "spaces-url-encode", project = "common-codec", implementation = "URLCodec")
    public void spacesUrlEncodeUrlCodec(FlowBenchResultImpl benchResult) {
        checkSpacesUrlEncode(benchResult, TaintedPortionPolicy.ALL, wrappedURLEncoder);
    }

    @FlowBench(group = "spaces-url-decode", project = "common-codec", implementation = "URLCodec")
    public void spacesUrlDecodeUrlCodec(FlowBenchResultImpl benchResult) {
        checkSpacesUrlDecode(benchResult, TaintedPortionPolicy.ALL, wrappedURLDecoder);
    }

    @FlowBench(group = "reserved-percent-encode", project = "common-codec", implementation = "URLCodec")
    public void reservedPercentEncodeUrlCodec(FlowBenchResultImpl benchResult) {
        checkReservedPercentEncode(benchResult, TaintedPortionPolicy.ALL, wrappedURLEncoder);
    }

    @FlowBench(group = "reserved-percent-decode", project = "common-codec", implementation = "URLCodec")
    public void reservedPercentDecodeUrlCodec(FlowBenchResultImpl benchResult) {
        checkReservedPercentDecode(benchResult, TaintedPortionPolicy.ALL, wrappedURLDecoder);
    }

    @FlowBench(group = "unicode-percent-encode", project = "common-codec", implementation = "URLCodec")
    public void unicodePercentEncodeURLCodec(FlowBenchResultImpl benchResult) {
        checkUnicodePercentEncode(benchResult, TaintedPortionPolicy.ALL, wrappedURLEncoder);
    }

    @FlowBench(group = "unicode-percent-decode", project = "common-codec", implementation = "URLCodec")
    public void unicodePercentDecodeURLCodec(FlowBenchResultImpl benchResult) {
        checkUnicodePercentDecode(benchResult, TaintedPortionPolicy.ALL, wrappedURLDecoder);
    }

    @FlowBench(group = "spaces-url-encode", project = "common-codec", implementation = "PercentCodec")
    public void spacesUrlEncodePercentCodec(FlowBenchResultImpl benchResult) {
        checkSpacesUrlEncode(benchResult, TaintedPortionPolicy.ALL, wrappedPercentEncoder);
    }

    @FlowBench(group = "spaces-url-decode", project = "common-codec", implementation = "PercentCodec")
    public void spacesUrlDecodePercentCodec(FlowBenchResultImpl benchResult) {
        checkSpacesUrlDecode(benchResult, TaintedPortionPolicy.ALL, wrappedPercentDecoder);
    }

    @FlowBench(group = "reserved-percent-encode", project = "common-codec", implementation = "PercentCodec")
    public void reservedPercentEncodePercentCodec(FlowBenchResultImpl benchResult) {
        checkReservedPercentEncode(benchResult, TaintedPortionPolicy.ALL, wrappedPercentEncoder);
    }

    @FlowBench(group = "reserved-percent-decode", project = "common-codec", implementation = "PercentCodec")
    public void reservedPercentDecodePercentCodec(FlowBenchResultImpl benchResult) {
        checkReservedPercentDecode(benchResult, TaintedPortionPolicy.ALL, wrappedPercentDecoder);
    }

    @FlowBench(group = "unicode-percent-encode", project = "common-codec", implementation = "PercentCodec")
    public void unicodePercentEncodePercentCodec(FlowBenchResultImpl benchResult) {
        checkUnicodePercentEncode(benchResult, TaintedPortionPolicy.ALL, wrappedPercentEncoder);
    }

    @FlowBench(group = "unicode-percent-decode", project = "common-codec", implementation = "PercentCodec")
    public void unicodePercentDecodePercentCodec(FlowBenchResultImpl benchResult) {
        checkUnicodePercentDecode(benchResult, TaintedPortionPolicy.ALL, wrappedPercentDecoder);
    }

    /**
     * Quoted-Printable content-transfer-encoding as defined by RFC 1521
     */
    @FlowBench(group = "quoted-printable-encode", project = "common-codec", implementation = "QuotedPrintableCodec")
    public void quotedPrintableEncode(FlowBenchResultImpl benchResult) throws EncoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec(true);
        String input = "\r\n\b\u03A9\u3399 ";
        input = taintWithIndices(input);
        String output = codec.encode(input);
        List<Integer> outputCharsPerInput = Arrays.asList(3, 3, 3, 6, 9, 3);
        checkOneInputToManyOutputs(benchResult, TaintedPortionPolicy.ALL, output, outputCharsPerInput);
    }

    /**
     * Quoted-Printable content-transfer-encoding as defined by RFC 1521
     */
    @FlowBench(group = "quoted-printable-decode", project = "common-codec", implementation = "QuotedPrintableCodec")
    public void quotedPrintableDecode(FlowBenchResultImpl benchResult) throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec(true);
        String input = "=0D=0A=08=CE=A9=E3=8E=99=20";
        input = taintWithIndices(input);
        String output = codec.decode(input);
        List<Integer> inputCharsPerOutput = Arrays.asList(3, 3, 3, 6, 9, 3);
        checkManyInputsToOneOutput(benchResult, TaintedPortionPolicy.ALL, output, inputCharsPerOutput);
    }

    private static UnaryOperator<String> wrapEncoder(BinaryEncoder encoder) {
        return s -> {
            try {
                return new String(encoder.encode(s.getBytes()));
            } catch(EncoderException e) {
                throw new IllegalArgumentException();
            }
        };
    }

    private static UnaryOperator<String> wrapDecoder(BinaryDecoder decoder) {
        return s -> {
            try {
                return new String(decoder.decode(s.getBytes()));
            } catch(DecoderException e) {
                throw new IllegalArgumentException();
            }
        };
    }
}
