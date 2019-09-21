package edu.gmu.swe.phosphor;

import com.opensymphony.xwork2.ognl.OgnlUtil;
import edu.columbia.cs.psl.phosphor.runtime.MultiTainter;
import edu.gmu.swe.phosphor.ignored.runtime.BinaryFlowBenchResult;
import ognl.ASTConst;
import ognl.ASTProperty;
import ognl.OgnlException;
import org.apache.juli.DateFormatCache;
import sun.util.locale.BaseLocale;

import static edu.gmu.swe.phosphor.FlowBenchUtil.taintWithIndices;

/**
 * Tests implicit flows that result from caching values.
 */
public class CacheFlowBench {

    /**
     * Compiles OGNL expressions using com.opensymphony.xwork2.ognl.OgnlUtil which caches OGNL expressions.
     */
    @FlowBench
    public void testOgnlExpressionCache(BinaryFlowBenchResult benchResult) throws OgnlException {
        String expression = "toString";
        String taintedExpression = taintWithIndices(expression);
        // Tainted value put into cache first
        OgnlUtil ognl1 = new OgnlUtil();
        ASTConst compiledConst = (ASTConst)((ASTProperty) ognl1.compile(taintedExpression)).jjtGetChild(0);
        benchResult.checkNonEmpty(compiledConst.getValue());
        compiledConst = (ASTConst)((ASTProperty) ognl1.compile(expression)).jjtGetChild(0);
        benchResult.checkEmpty(compiledConst.getValue());
        // Non-tainted value put into cache first
        OgnlUtil ognl2 = new OgnlUtil();
        compiledConst = (ASTConst)((ASTProperty) ognl2.compile(expression)).jjtGetChild(0);
        benchResult.checkEmpty(compiledConst.getValue());
        compiledConst = (ASTConst)((ASTProperty) ognl2.compile(taintedExpression)).jjtGetChild(0);
        benchResult.checkNonEmpty(compiledConst.getValue());
    }

    /**
     * Gets sun.util.locale.BaseLocale instances. sun.util.locale.BaseLocale caches BaseLocale instances.
     * sun.util.locale.BaseLocale also interns BaseLocales' fields when constructing them.
     */
    @FlowBench
    public void testBaseLocaleCache(BinaryFlowBenchResult benchResult) {
        // Tainted value put into cache first
        BaseLocale taintedZLocale = BaseLocale.getInstance(taintWithIndices("zzzzzzzz"),"ZZ", "", "");
        benchResult.checkNonEmpty(taintedZLocale.getLanguage());
        BaseLocale zLocale = BaseLocale.getInstance("zzzzzzzz", "ZZ", "", "");
        benchResult.checkEmpty(zLocale.getLanguage());
        // Non-tainted value put into cache first
        BaseLocale qLocale = BaseLocale.getInstance("qqqqqqqq", "QQ", "", "");
        benchResult.checkEmpty(qLocale.getLanguage());
        BaseLocale taintedQLocale = BaseLocale.getInstance(taintWithIndices("qqqqqqqq"), "QQ", "", "");
        benchResult.checkNonEmpty(taintedQLocale.getLanguage());
    }

    /**
     * Formats longs as dates using org.apache.juli.DateFormatCache which caches formatted dates.
     */
    @FlowBench
    public void testDateFormatCache(BinaryFlowBenchResult benchResult) {
        // Tainted value put into cache first
        DateFormatCache cache1 = new DateFormatCache(30, "dd-MMM-yyyy HH:mm:ss", null);
        String taintedDate1 = cache1.getFormat(MultiTainter.taintedLong(1000, 0));
        benchResult.checkNonEmpty(taintedDate1);
        String date1 = cache1.getFormat(1000);
        benchResult.checkEmpty(date1);
        // Non-tainted value put into cache first
        DateFormatCache cache2 = new DateFormatCache(30, "dd-MMM-yyyy HH:mm:ss", null);
        String date2 = cache2.getFormat(55000);
        benchResult.checkEmpty(date2);
        String taintedDate2 = cache2.getFormat(MultiTainter.taintedLong(55000, 1));
        benchResult.checkNonEmpty(taintedDate2);
    }
}
