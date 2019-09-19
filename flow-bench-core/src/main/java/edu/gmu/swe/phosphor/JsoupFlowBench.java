package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.maven.MultiLabelFlowBenchResult;
import org.jsoup.parser.Parser;

import java.util.HashSet;
import java.util.LinkedList;

public class JsoupFlowBench extends BaseFlowBench {

    @FlowBench
    public void testParserUnescapeEntities(MultiLabelFlowBenchResult benchResult) {
        String taintedStr = "tainted &quot;&amp;&lt;&gt;";
        int taintedLen = taintedStr.length();
        String input = taintWithIndices(taintedStr) + "untainted &num;&comma;&sol;&semi;";
        String output = Parser.unescapeEntities(input, true);
        for(int inputIndex = 0, outputIndex = 0; inputIndex < input.length(); inputIndex++, outputIndex++) {
            LinkedList<Object> expected = new LinkedList<>();
            expected.add(inputIndex);
            if(input.charAt(inputIndex) == '&') {
                while(input.charAt(inputIndex) != ';') {
                    expected.add(++inputIndex);
                }
            }
            if(inputIndex < taintedLen) {
                benchResult.check(expected, output.charAt(outputIndex));
            } else {
                benchResult.check(new HashSet<>(), output.charAt(outputIndex));
            }
        }
    }
}
