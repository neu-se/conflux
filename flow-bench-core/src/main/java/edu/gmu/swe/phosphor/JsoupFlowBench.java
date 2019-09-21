package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.MultiLabelFlowBenchResult;
import org.jsoup.parser.Parser;

import java.util.Collections;
import java.util.LinkedList;

import static edu.gmu.swe.phosphor.FlowBenchUtil.taintWithIndices;

/**
 * Tests implicit flows found in jsoup
 */
public class JsoupFlowBench {

    /**
     * Unescapes HTML named character entities using jsoup's Parser class. There is a control flow, but not a data flow
     * between escaped entities and the unescaped values produced from them.
     */
    @FlowBench
    public void testParserUnescapeEntities(MultiLabelFlowBenchResult benchResult, TaintedPortionPolicy policy) {
        String input = "eget nullam &quot;&amp;&lt;&gt; non nisi est ";
        input = taintWithIndices(input + input, policy);
        String output = Parser.unescapeEntities(input, true);
        for(int inputIndex = 0, outputIndex = 0; inputIndex < input.length(); inputIndex++, outputIndex++) {
            LinkedList<Object> expected = new LinkedList<>();
            expected.add(inputIndex);
            if(input.charAt(inputIndex) == '&') {
                while(input.charAt(inputIndex) != ';') {
                    expected.add(++inputIndex);
                }
            }
            if(policy.inTaintedRange(inputIndex, input.length())) {
                benchResult.check(expected, output.charAt(outputIndex));
            } else {
                benchResult.checkEmpty(output.charAt(outputIndex));
            }
        }
    }
}
