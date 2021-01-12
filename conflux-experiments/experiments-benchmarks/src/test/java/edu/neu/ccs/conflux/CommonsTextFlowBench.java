package edu.neu.ccs.conflux;

import edu.neu.ccs.conflux.internal.FlowBench;
import edu.neu.ccs.conflux.internal.runtime.BenchTaintTagChecker;
import org.apache.commons.text.StringEscapeUtils;

public class CommonsTextFlowBench {

    @FlowBench(group = "html-escape", project = "Apache Commons Text", implementation = "StringEscapeUtils")
    public void htmlEscape(BenchTaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkHtmlEscape(checker, numberOfEntities, StringEscapeUtils::escapeHtml4);
    }

    @FlowBench(group = "html-unescape", project = "Apache Commons Text", implementation = "StringEscapeUtils")
    public void htmlUnescape(BenchTaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkHtmlUnescape(checker, numberOfEntities, StringEscapeUtils::unescapeHtml4);
    }

    @FlowBench(group = "javascript-escape", project = "Apache Commons Text", implementation = "StringEscapeUtils")
    public void javaScriptEscape(BenchTaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkJavaScriptEscape(checker, numberOfEntities, StringEscapeUtils::escapeEcmaScript);
    }

    @FlowBench(group = "javascript-unescape", project = "Apache Commons Text", implementation = "StringEscapeUtils")
    public void javaScriptUnescape(BenchTaintTagChecker checker, int numberOfEntities) {
        ControlFlowBenchUtil.checkJavaScriptUnescape(checker, numberOfEntities, StringEscapeUtils::unescapeEcmaScript);
    }
}
