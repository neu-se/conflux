package edu.neu.ccs.conflux.bench;

import edu.neu.ccs.conflux.FlowBench;
import edu.neu.ccs.conflux.internal.runtime.FlowBenchResult;
import org.bouncycastle.util.encoders.Hex;

import static edu.neu.ccs.conflux.bench.ControlFlowBenchUtil.checkHexDecode;
import static edu.neu.ccs.conflux.bench.ControlFlowBenchUtil.checkHexEncode;

public class BouncyCastleFlowBench {

    @FlowBench(group = "hex-encode", project = "Bouncy Castle Provider", implementation = "Hex")
    public void hexEncode(FlowBenchResult benchResult, int numberOfEntities) {
        checkHexEncode(benchResult, numberOfEntities, b -> new String(org.bouncycastle.util.encoders.Hex.encode(b)));
    }

    @FlowBench(group = "hex-decode", project = "Bouncy Castle Provider", implementation = "Hex")
    public void hexDecode(FlowBenchResult benchResult, int numberOfEntities) {
        checkHexDecode(benchResult, numberOfEntities, Hex::decode);
    }
}
