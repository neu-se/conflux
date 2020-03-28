package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;

import javax.xml.bind.DatatypeConverter;

import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.checkHexDecode;
import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.checkHexEncode;

public class JavaRTFlowBench {

    /**
     * Converts an array of bytes into a string of hexadecimal digits
     */
    @FlowBench
    public void hexEncode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy) {
        checkHexEncode(benchResult, policy, DatatypeConverter::printHexBinary);
    }

    /**
     * Converts a string of hexadecimal digits to an array of bytes
     */
    @FlowBench
    public void hexDecode(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy) {
        checkHexDecode(benchResult, policy, DatatypeConverter::parseHexBinary);
    }
}
