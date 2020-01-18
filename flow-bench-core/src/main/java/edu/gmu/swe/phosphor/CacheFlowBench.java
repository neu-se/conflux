package edu.gmu.swe.phosphor;

import com.opensymphony.xwork2.ognl.OgnlUtil;
import edu.columbia.cs.psl.phosphor.runtime.MultiTainter;
import edu.gmu.swe.phosphor.ignored.runtime.FlowBenchResultImpl;
import ognl.ASTConst;
import ognl.ASTProperty;
import ognl.OgnlException;
import org.apache.juli.DateFormatCache;
import org.mortbay.io.Buffer;
import org.mortbay.io.BufferCache;
import sun.util.locale.BaseLocale;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static edu.gmu.swe.phosphor.FlowBenchUtil.taintWithIndices;

/**
 * Tests implicit flows that result from caching values.
 */
public class CacheFlowBench {

    /**
     * Compiles OGNL expressions using com.opensymphony.xwork2.ognl.OgnlUtil instances which cache OGNL expressions.
     */
    @FlowBench
    public void testOgnlExpressionCache(FlowBenchResultImpl benchResult) throws OgnlException {
        String expression = "toString";
        String taintedExpression = taintWithIndices(expression);
        // Tainted value put into cache first
        OgnlUtil ognl1 = new OgnlUtil();
        ognl1.setEnableExpressionCache("true");
        ASTConst taintedCompiledConst1 = (ASTConst) ((ASTProperty) ognl1.compile(taintedExpression)).jjtGetChild(0);
        benchResult.check(IntStream.range(0, expression.length()).boxed().collect(Collectors.toList()), taintedCompiledConst1.getValue());
        ASTConst compiledConst1 = (ASTConst) ((ASTProperty) ognl1.compile(expression)).jjtGetChild(0);
        benchResult.checkEmpty(compiledConst1.getValue());
        // Non-tainted value put into cache first
        OgnlUtil ognl2 = new OgnlUtil();
        ognl2.setEnableExpressionCache("true");
        ASTConst compiledConst2 = (ASTConst) ((ASTProperty) ognl2.compile(expression)).jjtGetChild(0);
        benchResult.checkEmpty(compiledConst2.getValue());
        ASTConst taintedCompiledConst2 = (ASTConst) ((ASTProperty) ognl2.compile(taintedExpression)).jjtGetChild(0);
        benchResult.check(IntStream.range(0, expression.length()).boxed().collect(Collectors.toList()), taintedCompiledConst2.getValue());
    }

    /**
     * Gets sun.util.locale.BaseLocale instances. The BaseLocale class caches BaseLocale instances. BaseLocale also
     * interns BaseLocales' string fields when constructing them.
     */
    @FlowBench
    public void testBaseLocaleCache(FlowBenchResultImpl benchResult) {
        // Tainted value put into cache first
        String lang1 = "zzzzzzzz";
        BaseLocale taintedZLocale = BaseLocale.getInstance(taintWithIndices(lang1), "ZZ", "", "");
        benchResult.check(IntStream.range(0, lang1.length()).boxed().collect(Collectors.toList()), taintedZLocale.getLanguage());
        BaseLocale zLocale = BaseLocale.getInstance(lang1, "ZZ", "", "");
        benchResult.checkEmpty(zLocale.getLanguage());
        // Non-tainted value put into cache first
        String lang2 = "qqqqqqqq";
        BaseLocale qLocale = BaseLocale.getInstance(lang2, "QQ", "", "");
        benchResult.checkEmpty(qLocale.getLanguage());
        BaseLocale taintedQLocale = BaseLocale.getInstance(taintWithIndices(lang2), "QQ", "", "");
        benchResult.check(IntStream.range(0, lang2.length()).boxed().collect(Collectors.toList()), taintedQLocale.getLanguage());
    }

    /**
     * Formats longs as dates using org.apache.juli.DateFormatCache which caches formatted dates.
     */
    @FlowBench
    public void testDateFormatCache(FlowBenchResultImpl benchResult) {
        // Tainted value put into cache first
        DateFormatCache cache1 = new DateFormatCache(30, "dd-MMM-yyyy HH:mm:ss", null);
        String taintedDate1 = cache1.getFormat(MultiTainter.taintedLong(1000, 0));
        benchResult.check(Collections.singletonList(0), taintedDate1);
        String date1 = cache1.getFormat(1000);
        benchResult.checkEmpty(date1);
        // Non-tainted value put into cache first
        DateFormatCache cache2 = new DateFormatCache(30, "dd-MMM-yyyy HH:mm:ss", null);
        String date2 = cache2.getFormat(55000);
        benchResult.checkEmpty(date2);
        String taintedDate2 = cache2.getFormat(MultiTainter.taintedLong(55000, 1));
        benchResult.check(Collections.singletonList(1), taintedDate2);
    }

    /**
     * Gets Buffers from org.mortbay.io.BufferCache instances which cache Buffer instances.
     */
    @FlowBench
    public void testBufferCache(FlowBenchResultImpl benchResult) {
        // Tainted value cached
        BufferCache cache1 = new BufferCache();
        String value1 = "leo a diam sollicitudin";
        cache1.add(taintWithIndices(value1), 0);
        Buffer taintedBuffer1 = cache1.lookup(taintWithIndices(value1));
        benchResult.check(IntStream.range(0, value1.length()).boxed().collect(Collectors.toList()), taintedBuffer1.asArray());
        Buffer buffer1 = cache1.lookup(value1);
        benchResult.checkEmpty(buffer1.asArray());
        // Non-tainted value cached
        String value2 = "cursus metus aliquam eleifend";
        BufferCache cache2 = new BufferCache();
        cache2.add(value2, 0);
        Buffer buffer2 = cache2.lookup(value2);
        benchResult.checkEmpty(buffer2.asArray());
        Buffer taintedBuffer2 = cache2.lookup(taintWithIndices(value2));
        benchResult.check(IntStream.range(0, value2.length()).boxed().collect(Collectors.toList()), taintedBuffer2.asArray());
    }
}
