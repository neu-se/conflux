package edu.neu.ccs.conflux.bench;

import edu.neu.ccs.conflux.FlowBench;
import edu.neu.ccs.conflux.internal.runtime.FlowBenchResult;
import org.apache.commons.codec.*;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.net.PercentCodec;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.apache.commons.codec.net.URLCodec;

import java.util.function.UnaryOperator;

public class CommonsCodecFlowBench {

    private static final UnaryOperator<String> wrappedURLEncoder = wrapBinaryEncoder(new URLCodec());
    private static final UnaryOperator<String> wrappedURLDecoder = wrapBinaryDecoder(new URLCodec());
    private static final PercentCodec percentCodec = new PercentCodec(ControlFlowBenchUtil.RESERVED_CHARS_FOR_PERCENT_ENCODING.getBytes(), true);
    private static final UnaryOperator<String> wrappedPercentEncoder = wrapBinaryEncoder(percentCodec);
    private static final UnaryOperator<String> wrappedPercentDecoder = wrapBinaryDecoder(percentCodec);

    @FlowBench(group = "hex-encode", project = "Apache Commons Codec", implementation = "Hex")
    public void hexEncode(FlowBenchResult benchResult, int numberOfEntities) {
        Hex encoder = new Hex();
        ControlFlowBenchUtil.checkHexEncode(benchResult, numberOfEntities, b -> new String(encoder.encode(b)));
    }

    @FlowBench(group = "hex-decode", project = "Apache Commons Codec", implementation = "Hex")
    public void hexDecode(FlowBenchResult benchResult, int numberOfEntities) {
        Hex decoder = new Hex();
        ControlFlowBenchUtil.checkHexDecode(benchResult, numberOfEntities, s -> {
            try {
                return decoder.decode(s.getBytes());
            } catch (DecoderException e) {
                throw new IllegalArgumentException();
            }
        });
    }

    @FlowBench(group = "spaces-url-encode", project = "Apache Commons Codec", implementation = "URLCodec")
    public void spacesUrlEncodeUrlCodec(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkSpacesUrlEncode(benchResult, numberOfEntities, wrappedURLEncoder);
    }

    @FlowBench(group = "spaces-url-decode", project = "Apache Commons Codec", implementation = "URLCodec")
    public void spacesUrlDecodeUrlCodec(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkSpacesUrlDecode(benchResult, numberOfEntities, wrappedURLDecoder);
    }

    @FlowBench(group = "reserved-percent-encode", project = "Apache Commons Codec", implementation = "URLCodec")
    public void reservedPercentEncodeUrlCodec(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkReservedPercentEncode(benchResult, numberOfEntities, wrappedURLEncoder);
    }

    @FlowBench(group = "reserved-percent-decode", project = "Apache Commons Codec", implementation = "URLCodec")
    public void reservedPercentDecodeUrlCodec(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkReservedPercentDecode(benchResult, numberOfEntities, wrappedURLDecoder);
    }

    @FlowBench(group = "unicode-percent-encode", project = "Apache Commons Codec", implementation = "URLCodec")
    public void unicodePercentEncodeURLCodec(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkUnicodePercentEncode(benchResult, numberOfEntities, wrappedURLEncoder);
    }

    @FlowBench(group = "unicode-percent-decode", project = "Apache Commons Codec", implementation = "URLCodec")
    public void unicodePercentDecodeURLCodec(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkUnicodePercentDecode(benchResult, numberOfEntities, wrappedURLDecoder);
    }

    @FlowBench(group = "spaces-url-encode", project = "Apache Commons Codec", implementation = "PercentCodec")
    public void spacesUrlEncodePercentCodec(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkSpacesUrlEncode(benchResult, numberOfEntities, wrappedPercentEncoder);
    }

    @FlowBench(group = "spaces-url-decode", project = "Apache Commons Codec", implementation = "PercentCodec")
    public void spacesUrlDecodePercentCodec(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkSpacesUrlDecode(benchResult, numberOfEntities, wrappedPercentDecoder);
    }

    @FlowBench(group = "reserved-percent-encode", project = "Apache Commons Codec", implementation = "PercentCodec")
    public void reservedPercentEncodePercentCodec(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkReservedPercentEncode(benchResult, numberOfEntities, wrappedPercentEncoder);
    }

    @FlowBench(group = "reserved-percent-decode", project = "Apache Commons Codec", implementation = "PercentCodec")
    public void reservedPercentDecodePercentCodec(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkReservedPercentDecode(benchResult, numberOfEntities, wrappedPercentDecoder);
    }

    @FlowBench(group = "unicode-percent-encode", project = "Apache Commons Codec", implementation = "PercentCodec")
    public void unicodePercentEncodePercentCodec(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkUnicodePercentEncode(benchResult, numberOfEntities, wrappedPercentEncoder);
    }

    @FlowBench(group = "unicode-percent-decode", project = "Apache Commons Codec", implementation = "PercentCodec")
    public void unicodePercentDecodePercentCodec(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkUnicodePercentDecode(benchResult, numberOfEntities, wrappedPercentDecoder);
    }

    @FlowBench(group = "quoted-printable-encode", project = "Apache Commons Codec", implementation = "QuotedPrintableCodec")
    public void quotedPrintableEncode(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkQuotedPrintableEncode(benchResult, numberOfEntities, wrapStringEncoder(new QuotedPrintableCodec(false)));
    }

    @FlowBench(group = "quoted-printable-decode", project = "Apache Commons Codec", implementation = "QuotedPrintableCodec")
    public void quotedPrintableDecode(FlowBenchResult benchResult, int numberOfEntities) {
        ControlFlowBenchUtil.checkQuotedPrintableDecode(benchResult, numberOfEntities, wrapStringDecoder(new QuotedPrintableCodec(false)));
    }

    private static UnaryOperator<String> wrapBinaryEncoder(BinaryEncoder encoder) {
        return s -> {
            try {
                return new String(encoder.encode(s.getBytes()));
            } catch(EncoderException e) {
                throw new IllegalArgumentException();
            }
        };
    }

    private static UnaryOperator<String> wrapBinaryDecoder(BinaryDecoder decoder) {
        return s -> {
            try {
                return new String(decoder.decode(s.getBytes()));
            } catch(DecoderException e) {
                throw new IllegalArgumentException();
            }
        };
    }

    private static UnaryOperator<String> wrapStringEncoder(StringEncoder encoder) {
        return s -> {
            try {
                return encoder.encode(s);
            } catch(EncoderException e) {
                throw new IllegalArgumentException();
            }
        };
    }

    private static UnaryOperator<String> wrapStringDecoder(StringDecoder decoder) {
        return s -> {
            try {
                return decoder.decode(s);
            } catch(DecoderException e) {
                throw new IllegalArgumentException();
            }
        };
    }
}
