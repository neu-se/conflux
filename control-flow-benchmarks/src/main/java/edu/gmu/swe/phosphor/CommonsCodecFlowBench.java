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

    @FlowBench
    public void hexEncode(FlowBenchResultImpl benchResult) {
        Hex encoder = new Hex();
        checkHexEncode(benchResult, TaintedPortionPolicy.ALL, b -> new String(encoder.encode(b)));
    }

    @FlowBench
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

    @FlowBench
    public void urlCodecEncodeSpaces(FlowBenchResultImpl benchResult) {
        checkURLEncodeSpaces(benchResult, TaintedPortionPolicy.ALL, wrappedURLEncoder);
    }

    @FlowBench
    public void urlCodecDecodeSpaces(FlowBenchResultImpl benchResult) {
        checkURLDecodeSpaces(benchResult, TaintedPortionPolicy.ALL, wrappedURLDecoder);
    }

    @FlowBench
    public void urlCodecEncodeReserved(FlowBenchResultImpl benchResult) {
        checkPercentEncodeReserved(benchResult, TaintedPortionPolicy.ALL, wrappedURLEncoder);

    }

    @FlowBench
    public void urlCodecDecodeReserved(FlowBenchResultImpl benchResult) {
        checkPercentDecodeReserved(benchResult, TaintedPortionPolicy.ALL, wrappedURLDecoder);
    }

    @FlowBench
    public void urlCodecEncodeUnicode(FlowBenchResultImpl benchResult) {
        checkPercentEncodeUnicode(benchResult, TaintedPortionPolicy.ALL, wrappedURLEncoder);
    }

    @FlowBench
    public void urlCodecDecodeUnicode(FlowBenchResultImpl benchResult) {
        checkPercentDecodeUnicode(benchResult, TaintedPortionPolicy.ALL, wrappedURLDecoder);
    }

    @FlowBench
    public void percentCodecEncodeSpaces(FlowBenchResultImpl benchResult) {
        checkURLEncodeSpaces(benchResult, TaintedPortionPolicy.ALL, wrappedPercentEncoder);
    }

    @FlowBench
    public void percentCodecDecodeSpaces(FlowBenchResultImpl benchResult) {
        checkURLDecodeSpaces(benchResult, TaintedPortionPolicy.ALL, wrappedPercentDecoder);
    }

    @FlowBench
    public void percentCodecEncodeReserved(FlowBenchResultImpl benchResult) {
        checkPercentEncodeReserved(benchResult, TaintedPortionPolicy.ALL, wrappedPercentEncoder);

    }

    @FlowBench
    public void percentCodecDecodeReserved(FlowBenchResultImpl benchResult) {
        checkPercentDecodeReserved(benchResult, TaintedPortionPolicy.ALL, wrappedPercentDecoder);
    }

    @FlowBench
    public void percentCodecEncodeUnicode(FlowBenchResultImpl benchResult) {
        checkPercentEncodeUnicode(benchResult, TaintedPortionPolicy.ALL, wrappedPercentEncoder);
    }

    @FlowBench
    public void percentCodecDecodeUnicode(FlowBenchResultImpl benchResult) {
        checkPercentDecodeUnicode(benchResult, TaintedPortionPolicy.ALL, wrappedPercentDecoder);
    }

    /**
     * Quoted-Printable content-transfer-encoding as defined by RFC 1521
     */
    @FlowBench
    public void quotedPrintableCodecEncode(FlowBenchResultImpl benchResult) throws EncoderException {
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
    @FlowBench
    public void quotedPrintableCodecDecode(FlowBenchResultImpl benchResult) throws DecoderException {
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
