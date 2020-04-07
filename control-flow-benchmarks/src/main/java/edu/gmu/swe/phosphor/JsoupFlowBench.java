package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import org.jsoup.nodes.Entities;

import java.util.Arrays;
import java.util.List;

import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.checkEscapeHtml;
import static edu.gmu.swe.phosphor.ControlFlowBenchUtil.checkManyInputsToOneOutput;
import static edu.gmu.swe.phosphor.FlowBenchUtil.taintWithIndices;

public class JsoupFlowBench {

    @FlowBench
    public void htmlEscape(FlowBenchResultImpl benchResult) {
        checkEscapeHtml(benchResult, TaintedPortionPolicy.ALL, Entities::escape);
    }

    @FlowBench
    public void htmlUnescape(FlowBenchResultImpl benchResult) {
        // The ; is semi-optional - Jsoup will parse the entity regardless
        String input = "&amp&lt&gt";
        input = taintWithIndices(input + input, TaintedPortionPolicy.ALL);
        String output = Entities.escape(input);
        List<Integer> inputCharsPerOutput = Arrays.asList(4, 3, 3, 4, 3, 3);
        checkManyInputsToOneOutput(benchResult, TaintedPortionPolicy.ALL, output, inputCharsPerOutput);
    }
}
