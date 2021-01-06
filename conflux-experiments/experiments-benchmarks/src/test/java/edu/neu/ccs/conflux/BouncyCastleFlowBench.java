package edu.neu.ccs.conflux;

import edu.neu.ccs.conflux.internal.FlowBench;
import edu.neu.ccs.conflux.internal.runtime.TaintTagChecker;
import org.bouncycastle.util.encoders.Hex;

import static edu.neu.ccs.conflux.ControlFlowBenchUtil.checkHexDecode;
import static edu.neu.ccs.conflux.ControlFlowBenchUtil.checkHexEncode;

public class BouncyCastleFlowBench {

    @FlowBench(group = "hex-encode", project = "Bouncy Castle Provider", implementation = "Hex")
    public void hexEncode(TaintTagChecker checker, int numberOfEntities) {
        checkHexEncode(checker, numberOfEntities, b -> new String(org.bouncycastle.util.encoders.Hex.encode(b)));
    }

    @FlowBench(group = "hex-decode", project = "Bouncy Castle Provider", implementation = "Hex")
    public void hexDecode(TaintTagChecker checker, int numberOfEntities) {
        checkHexDecode(checker, numberOfEntities, Hex::decode);
    }
}
