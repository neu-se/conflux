package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.checkHexDecode;
import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.checkHexEncode;

public class CommonsCodecFlowBench {

    /**
     * Converts an array of bytes into a string of hexadecimal digits
     */
    @FlowBench
    public void hexEncode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy) {
        Hex encoder = new Hex();
        checkHexEncode(benchResult, policy, b -> new String(encoder.encode(b)));
    }


    /**
     * Converts a string of hexadecimal digits to an array of bytes
     */
    @FlowBench
    public void hexDecode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy) {
        Hex decoder = new Hex();
        checkHexDecode(benchResult, policy, s -> {
            try {
                return decoder.decode(s.getBytes());
            } catch(DecoderException e) {
                throw new IllegalArgumentException();
            }
        });
    }

    /**
     * Percent encoding as defined by HTTP 1.1
     */
    @FlowBench
    public void percentCodecEncode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy) {

    }

    /**
     * Percent decoding as defined by HTTP 1.1
     */
    @FlowBench
    public void percentCodecDecode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy) {

    }

    /**
     * Quoted-Printable content-transfer-encoding as defined by RFC 1521
     */
    @FlowBench
    public void quotedPrintableCodecEncode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy) {

    }

    /**
     * Quoted-Printable content-transfer-encoding decoding as defined by RFC 1521
     */
    @FlowBench
    public void quotedPrintableCodecDecode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy) {

    }

    /**
     * www-form-urlencoded encoding scheme (see URLEncoder)
     */
    @FlowBench
    public void urlCodecEncode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy) {

    }

    /**
     * www-form-urlencoded encoding scheme (see URLDecoder)
     */
    @FlowBench
    public void urlCodecDecode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy) {

    }
}
