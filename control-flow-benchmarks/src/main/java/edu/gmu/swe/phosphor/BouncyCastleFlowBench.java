package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import org.bouncycastle.util.encoders.Hex;

import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.checkHexDecode;
import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.checkHexEncode;

public class BouncyCastleFlowBench {

    @FlowBench(group = "hex-encode", project = "Bouncy Castle Provider", implementation = "Hex")
    public void hexEncode(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkHexEncode(benchResult, numberOfEntities, b -> new String(org.bouncycastle.util.encoders.Hex.encode(b)));
    }

    @FlowBench(group = "hex-decode", project = "Bouncy Castle Provider", implementation = "Hex")
    public void hexDecode(FlowBenchResultImpl benchResult, int numberOfEntities) {
        checkHexDecode(benchResult, numberOfEntities, Hex::decode);
    }
}
