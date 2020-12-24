package edu.neu.ccs.conflux.bench;

import edu.neu.ccs.conflux.internal.FlowBench;
import edu.neu.ccs.conflux.internal.runtime.TaintTagChecker;
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
    public void hexEncode(TaintTagChecker checker, int numberOfEntities) {
        Hex encoder = new Hex();
        ControlFlowBenchUtil.checkHexEncode(checker, numberOfEntities, b -> new String(encoder.encode(b)));
    }

    @FlowBench(group = "hex-decode", project = "Apache Commons Codec", implementation = "Hex")
    public void hexDecode(TaintTagChecker checker, int numberOfEntities) {
        Hex decoder = new Hex();
        ControlFlowBenchUtil.checkHexDecode(checker, numberOfEntities, s -> {
            try {
                return decoder.decode(s.getBytes());
            } catch (DecoderException e) {
                throw new IllegalArgumentException();
            }
        });
    }

    @FlowBench(group = "spaces-url-encode", project = "Apache Commons Codec", implementation = "URLCodec")
    public void spacesUrlEncodeUrlCodec(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkSpacesUrlEncode(checker, numberOfEntities, wrappedURLEncoder);
    }

    @FlowBench(group = "spaces-url-decode", project = "Apache Commons Codec", implementation = "URLCodec")
    public void spacesUrlDecodeUrlCodec(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkSpacesUrlDecode(checker, numberOfEntities, wrappedURLDecoder);
    }

    @FlowBench(group = "reserved-percent-encode", project = "Apache Commons Codec", implementation = "URLCodec")
    public void reservedPercentEncodeUrlCodec(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkReservedPercentEncode(checker, numberOfEntities, wrappedURLEncoder);
    }

    @FlowBench(group = "reserved-percent-decode", project = "Apache Commons Codec", implementation = "URLCodec")
    public void reservedPercentDecodeUrlCodec(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkReservedPercentDecode(checker, numberOfEntities, wrappedURLDecoder);
    }

    @FlowBench(group = "unicode-percent-encode", project = "Apache Commons Codec", implementation = "URLCodec")
    public void unicodePercentEncodeURLCodec(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkUnicodePercentEncode(checker, numberOfEntities, wrappedURLEncoder);
    }

    @FlowBench(group = "unicode-percent-decode", project = "Apache Commons Codec", implementation = "URLCodec")
    public void unicodePercentDecodeURLCodec(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkUnicodePercentDecode(checker, numberOfEntities, wrappedURLDecoder);
    }

    @FlowBench(group = "spaces-url-encode", project = "Apache Commons Codec", implementation = "PercentCodec")
    public void spacesUrlEncodePercentCodec(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkSpacesUrlEncode(checker, numberOfEntities, wrappedPercentEncoder);
    }

    @FlowBench(group = "spaces-url-decode", project = "Apache Commons Codec", implementation = "PercentCodec")
    public void spacesUrlDecodePercentCodec(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkSpacesUrlDecode(checker, numberOfEntities, wrappedPercentDecoder);
    }

    @FlowBench(group = "reserved-percent-encode", project = "Apache Commons Codec", implementation = "PercentCodec")
    public void reservedPercentEncodePercentCodec(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkReservedPercentEncode(checker, numberOfEntities, wrappedPercentEncoder);
    }

    @FlowBench(group = "reserved-percent-decode", project = "Apache Commons Codec", implementation = "PercentCodec")
    public void reservedPercentDecodePercentCodec(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkReservedPercentDecode(checker, numberOfEntities, wrappedPercentDecoder);
    }

    @FlowBench(group = "unicode-percent-encode", project = "Apache Commons Codec", implementation = "PercentCodec")
    public void unicodePercentEncodePercentCodec(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkUnicodePercentEncode(checker, numberOfEntities, wrappedPercentEncoder);
    }

    @FlowBench(group = "unicode-percent-decode", project = "Apache Commons Codec", implementation = "PercentCodec")
    public void unicodePercentDecodePercentCodec(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkUnicodePercentDecode(checker, numberOfEntities, wrappedPercentDecoder);
    }

    @FlowBench(group = "quoted-printable-encode", project = "Apache Commons Codec", implementation = "QuotedPrintableCodec")
    public void quotedPrintableEncode(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkQuotedPrintableEncode(checker, numberOfEntities, wrapStringEncoder(new QuotedPrintableCodec(false)));
    }

    @FlowBench(group = "quoted-printable-decode", project = "Apache Commons Codec", implementation = "QuotedPrintableCodec")
    public void quotedPrintableDecode(TaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkQuotedPrintableDecode(checker, numberOfEntities, wrapStringDecoder(new QuotedPrintableCodec(false)));
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
