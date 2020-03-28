package edu.gmu.swe.phosphor;

import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.parser.Parser;
import org.springframework.web.util.HtmlUtils;

import java.util.Collections;
import java.util.LinkedList;

import static edu.gmu.swe.phosphor.FlowBenchUtil.taintWithIndices;

public class GeneralControlFlowBench {

    /**
     * Unescapes HTML named character entities using org.jsoup.parser.Parser. There is a control flow, but not a data
     * flow between escaped entities and the unescaped values produced from them.
     */
    @FlowBench
    public void parserUnescapeEntities(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy) {
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

    /**
     * Escapes HTML reserved characters using org.apache.commons.text.StringEscapeUtils. There is a control flow, but
     * not a data flow between reserved characters and the escaped values produced from them.
     */
    @FlowBench
    public void stringEscapeUtilsEscapeHtml4(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy) {
        String input = "eget nullam \"&<> non nisi est ";
        input = taintWithIndices(input + input, policy);
        String output = StringEscapeUtils.escapeHtml4(input);
        for(int inputIndex = 0, outputIndex = 0; inputIndex < input.length(); inputIndex++, outputIndex++) {
            int endOfRange = outputIndex;
            if(output.charAt(outputIndex) == '&') {
                while(output.charAt(endOfRange) != ';') {
                    endOfRange++;
                }
            }
            for(int i = outputIndex; i <= endOfRange; i++) {
                if(policy.inTaintedRange(inputIndex, input.length())) {
                    benchResult.check(Collections.singletonList(inputIndex), output.charAt(i));
                } else {
                    benchResult.checkEmpty(output.charAt(i));
                }
            }
            outputIndex = endOfRange;
        }
    }

    /**
     * Escapes HTML reserved characters using org.springframework.web.util.HtmlUtils. There is a control flow, but
     * not a data flow between reserved characters and the escaped values produced from them. Control flow uses a switch
     * statement.
     */
    @FlowBench
    public void htmlUtilsEscapeUTF8(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy) {
        String input = "eget nullam \"&<> non nisi est ";
        input = taintWithIndices(input + input, policy);
        String output = HtmlUtils.htmlEscape(input, "UTF-8");
        for(int inputIndex = 0, outputIndex = 0; inputIndex < input.length(); inputIndex++, outputIndex++) {
            int endOfRange = outputIndex;
            if(output.charAt(outputIndex) == '&') {
                while(output.charAt(endOfRange) != ';') {
                    endOfRange++;
                }
            }
            for(int i = outputIndex; i <= endOfRange; i++) {
                if(policy.inTaintedRange(inputIndex, input.length())) {
                    benchResult.check(Collections.singletonList(inputIndex), output.charAt(i));
                } else {
                    benchResult.checkEmpty(output.charAt(i));
                }
            }
            outputIndex = endOfRange;
        }
    }

    /**
     * Escapes HTML reserved characters using org.springframework.web.util.HtmlUtils. Characters are escaped by
     * indexing into an array of Strings.
     */
    @FlowBench
    public void htmlUtilsEscapeISO(FlowBenchResultImpl benchResult, TaintedPortionPolicy policy) {
        String input = "eget nullam \"&<> non nisi est ";
        input = taintWithIndices(input + input, policy);
        String output = HtmlUtils.htmlEscape(input, "ISO-8859-1");
        for(int inputIndex = 0, outputIndex = 0; inputIndex < input.length(); inputIndex++, outputIndex++) {
            int endOfRange = outputIndex;
            if(output.charAt(outputIndex) == '&') {
                while(output.charAt(endOfRange) != ';') {
                    endOfRange++;
                }
            }
            for(int i = outputIndex; i <= endOfRange; i++) {
                if(policy.inTaintedRange(inputIndex, input.length())) {
                    benchResult.check(Collections.singletonList(inputIndex), output.charAt(i));
                } else {
                    benchResult.checkEmpty(output.charAt(i));
                }
            }
            outputIndex = endOfRange;
        }
    }
}
