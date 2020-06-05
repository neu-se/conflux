package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import org.apache.commons.codec.*;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.net.PercentCodec;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.apache.commons.codec.net.URLCodec;

import java.util.function.UnaryOperator;

import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.*;

public class CommonsCodecFlowBench {

    private static final UnaryOperator<String> wrappedURLEncoder = wrapBinaryEncoder(new URLCodec());
    private static final UnaryOperator<String> wrappedURLDecoder = wrapBinaryDecoder(new URLCodec());
    private static final PercentCodec percentCodec = new PercentCodec(RESERVED_CHARS_FOR_PERCENT_ENCODING.getBytes(), true);
    private static final UnaryOperator<String> wrappedPercentEncoder = wrapBinaryEncoder(percentCodec);
    private static final UnaryOperator<String> wrappedPercentDecoder = wrapBinaryDecoder(percentCodec);

    @FlowBench(group = "hex-encode", project = "Apache Commons Codec", implementation = "Hex")
    public void hexEncode(FlowBenchResultImpl benchResult, int numberOfEntities) {
        Hex encoder = new Hex();
        checkHexEncode(benchResult, numberOfEntities, b -> new String(encoder.encode(b)));
    }

    @FlowBench(group = "hex-decode", project = "Apache Commons Codec", implementation = "Hex")
    public void hexDecode(FlowBenchResultImpl benchResult, int numberOfEntities) {
        Hex decoder = new Hex();
        checkHexDecode(benchResult, numberOfEntities, s -> {
            try {
                return decoder.decode(s.getBytes());
            } catch(DecoderException e) {
                throw new IllegalArgumentException();
            }
        });
    }

    @FlowBench(group = "spaces-url-encode", project = "Apache Commons Codec", implementation = "URLCodec")
    public void spacesUrlEncodeUrlCodec(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkSpacesUrlEncode(benchResult, numberOfEntities, wrappedURLEncoder);
    }

    @FlowBench(group = "spaces-url-decode", project = "Apache Commons Codec", implementation = "URLCodec")
    public void spacesUrlDecodeUrlCodec(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkSpacesUrlDecode(benchResult, numberOfEntities, wrappedURLDecoder);
    }

    @FlowBench(group = "reserved-percent-encode", project = "Apache Commons Codec", implementation = "URLCodec")
    public void reservedPercentEncodeUrlCodec(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkReservedPercentEncode(benchResult, numberOfEntities, wrappedURLEncoder);
    }

    @FlowBench(group = "reserved-percent-decode", project = "Apache Commons Codec", implementation = "URLCodec")
    public void reservedPercentDecodeUrlCodec(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkReservedPercentDecode(benchResult, numberOfEntities, wrappedURLDecoder);
    }

    @FlowBench(group = "unicode-percent-encode", project = "Apache Commons Codec", implementation = "URLCodec")
    public void unicodePercentEncodeURLCodec(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkUnicodePercentEncode(benchResult, numberOfEntities, wrappedURLEncoder);
    }

    @FlowBench(group = "unicode-percent-decode", project = "Apache Commons Codec", implementation = "URLCodec")
    public void unicodePercentDecodeURLCodec(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkUnicodePercentDecode(benchResult, numberOfEntities, wrappedURLDecoder);
    }

    @FlowBench(group = "spaces-url-encode", project = "Apache Commons Codec", implementation = "PercentCodec")
    public void spacesUrlEncodePercentCodec(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkSpacesUrlEncode(benchResult, numberOfEntities, wrappedPercentEncoder);
    }

    @FlowBench(group = "spaces-url-decode", project = "Apache Commons Codec", implementation = "PercentCodec")
    public void spacesUrlDecodePercentCodec(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkSpacesUrlDecode(benchResult, numberOfEntities, wrappedPercentDecoder);
    }

    @FlowBench(group = "reserved-percent-encode", project = "Apache Commons Codec", implementation = "PercentCodec")
    public void reservedPercentEncodePercentCodec(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkReservedPercentEncode(benchResult, numberOfEntities, wrappedPercentEncoder);
    }

    @FlowBench(group = "reserved-percent-decode", project = "Apache Commons Codec", implementation = "PercentCodec")
    public void reservedPercentDecodePercentCodec(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkReservedPercentDecode(benchResult, numberOfEntities, wrappedPercentDecoder);
    }

    @FlowBench(group = "unicode-percent-encode", project = "Apache Commons Codec", implementation = "PercentCodec")
    public void unicodePercentEncodePercentCodec(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkUnicodePercentEncode(benchResult, numberOfEntities, wrappedPercentEncoder);
    }

    @FlowBench(group = "unicode-percent-decode", project = "Apache Commons Codec", implementation = "PercentCodec")
    public void unicodePercentDecodePercentCodec(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkUnicodePercentDecode(benchResult, numberOfEntities, wrappedPercentDecoder);
    }

    @FlowBench(group = "quoted-printable-encode", project = "Apache Commons Codec", implementation = "QuotedPrintableCodec")
    public void quotedPrintableEncode(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkQuotedPrintableEncode(benchResult, numberOfEntities, wrapStringEncoder(new QuotedPrintableCodec(true)));
    }

    @FlowBench(group = "quoted-printable-decode", project = "Apache Commons Codec", implementation = "QuotedPrintableCodec")
    public void quotedPrintableDecode(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkQuotedPrintableDecode(benchResult, numberOfEntities, wrapStringDecoder(new QuotedPrintableCodec(true)));
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
