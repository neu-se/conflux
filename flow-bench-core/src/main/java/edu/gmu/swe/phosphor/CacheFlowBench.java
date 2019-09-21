package edu.gmu.swe.phosphor;

import com.opensymphony.xwork2.ognl.OgnlUtil;
import edu.gmu.swe.phosphor.ignored.runtime.BinaryFlowBenchResult;
import ognl.ASTConst;
import ognl.ASTProperty;
import ognl.OgnlException;

import static edu.gmu.swe.phosphor.FlowBenchUtil.taintWithIndices;

/**
 * Tests implicit flows that result from caching values.
 */
public class CacheFlowBench {

    /**
     * TODO finish description - note Cache + implicit flow probably in parseExpression
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
}
