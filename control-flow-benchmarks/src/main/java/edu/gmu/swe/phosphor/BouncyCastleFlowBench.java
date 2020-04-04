package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import org.bouncycastle.util.encoders.Hex;

import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.checkHexDecode;
import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.checkHexEncode;

public class BouncyCastleFlowBench {

    @FlowBench
    public void hexEncode(FlowBenchResultImpl benchResult) {
        checkHexEncode(benchResult, TaintedPortionPolicy.ALL, b -> new String(org.bouncycastle.util.encoders.Hex.encode(b)));
    }

    @FlowBench
    public void hexDecode(FlowBenchResultImpl benchResult) {
        checkHexDecode(benchResult, TaintedPortionPolicy.ALL, Hex::decode);
    }
}
