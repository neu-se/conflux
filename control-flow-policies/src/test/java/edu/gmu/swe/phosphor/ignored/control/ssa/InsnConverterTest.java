package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Arrays;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.LinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.List;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.*;
import org.junit.Test;

import static edu.gmu.swe.phosphor.ignored.control.ssa.InsnConverterTestMethods.OWNER;
import static edu.gmu.swe.phosphor.ignored.control.ssa.expression.ArrayLengthOperation.ARRAY_LENGTH;
import static edu.gmu.swe.phosphor.ignored.control.ssa.expression.NegateOperation.NEGATE;
import static org.junit.Assert.assertEquals;

public class InsnConverterTest {

    @Test
    public void testPushPopConstants() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.pushPopConstants();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.NULL),
                new AssignmentStatement(new StackElement(1), ConstantExpression.M1),
                new AssignmentStatement(new StackElement(2), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(3), ConstantExpression.I1),
                new AssignmentStatement(new StackElement(4), ConstantExpression.I2),
                new AssignmentStatement(new StackElement(5), ConstantExpression.I3),
                new AssignmentStatement(new StackElement(6), ConstantExpression.I4),
                new AssignmentStatement(new StackElement(7), ConstantExpression.I5),
                new AssignmentStatement(new StackElement(8), ConstantExpression.F0),
                new AssignmentStatement(new StackElement(9), ConstantExpression.F1),
                new AssignmentStatement(new StackElement(10), ConstantExpression.F2),
                new AssignmentStatement(new StackElement(11), new IntegerConstantExpression(17)),
                new AssignmentStatement(new StackElement(12), new IntegerConstantExpression(34)),
                new AssignmentStatement(new StackElement(13), new ObjectConstantExpression("Hello")),
                new AssignmentStatement(new StackElement(14), ConstantExpression.D0),
                new AssignmentStatement(new StackElement(15), ConstantExpression.D1),
                new AssignmentStatement(new StackElement(16), ConstantExpression.L0),
                new AssignmentStatement(new StackElement(17), ConstantExpression.L1)
        ));
        for(int i = 0; i < 4; i++) {
            expectedStatements.add(IdleStatement.POP2);
        }
        for(int i = 0; i < 14; i++) {
            expectedStatements.add(IdleStatement.POP);
        }
        expectedStatements.add(new ReturnStatement(null));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testLoadLocals() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.loadLocals();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(1), new LocalVariable(1)),
                new AssignmentStatement(new StackElement(2), new LocalVariable(3)),
                new AssignmentStatement(new StackElement(3), new LocalVariable(4)),
                new AssignmentStatement(new StackElement(4), new LocalVariable(6)),
                new InvokeStatement(new InvokeExpression(OWNER, "example", null, new Expression[]{
                        new StackElement(0),
                        new StackElement(1),
                        new StackElement(2),
                        new StackElement(3),
                        new StackElement(4)
                })),
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testStoreLocals() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.storeLocals();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.I0),
                new AssignmentStatement(new LocalVariable(0), new StackElement(0)),
                new AssignmentStatement(new StackElement(0), ConstantExpression.L0),
                new AssignmentStatement(new LocalVariable(1), new StackElement(0)),
                new AssignmentStatement(new StackElement(0), ConstantExpression.F0),
                new AssignmentStatement(new LocalVariable(3), new StackElement(0)),
                new AssignmentStatement(new StackElement(0), ConstantExpression.D0),
                new AssignmentStatement(new LocalVariable(4), new StackElement(0)),
                new AssignmentStatement(new StackElement(0), ConstantExpression.NULL),
                new AssignmentStatement(new LocalVariable(6), new StackElement(0)),
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testLoadArrayElements() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.loadArrayElements();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(0), new ArrayExpression(new StackElement(0), new StackElement(1))),
                //
                new AssignmentStatement(new StackElement(1), new LocalVariable(1)),
                new AssignmentStatement(new StackElement(2), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(1), new ArrayExpression(new StackElement(1), new StackElement(2))),
                //
                new AssignmentStatement(new StackElement(2), new LocalVariable(2)),
                new AssignmentStatement(new StackElement(3), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(2), new ArrayExpression(new StackElement(2), new StackElement(3))),
                //
                new AssignmentStatement(new StackElement(3), new LocalVariable(3)),
                new AssignmentStatement(new StackElement(4), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(3), new ArrayExpression(new StackElement(3), new StackElement(4))),
                //
                new AssignmentStatement(new StackElement(4), new LocalVariable(4)),
                new AssignmentStatement(new StackElement(5), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(4), new ArrayExpression(new StackElement(4), new StackElement(5))),
                //
                new AssignmentStatement(new StackElement(5), new LocalVariable(5)),
                new AssignmentStatement(new StackElement(6), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(5), new ArrayExpression(new StackElement(5), new StackElement(6))),
                //
                new AssignmentStatement(new StackElement(6), new LocalVariable(6)),
                new AssignmentStatement(new StackElement(7), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(6), new ArrayExpression(new StackElement(6), new StackElement(7))),
                //
                new AssignmentStatement(new StackElement(7), new LocalVariable(7)),
                new AssignmentStatement(new StackElement(8), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(7), new ArrayExpression(new StackElement(7), new StackElement(8))),
                //
                new InvokeStatement(new InvokeExpression(OWNER, "example", null, new Expression[]{
                        new StackElement(0),
                        new StackElement(1),
                        new StackElement(2),
                        new StackElement(3),
                        new StackElement(4),
                        new StackElement(5),
                        new StackElement(6),
                        new StackElement(7)
                })),
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testStoreArrayElements() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.storeArrayElements();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(2), ConstantExpression.I0),
                new AssignmentStatement(new ArrayExpression(new StackElement(0), new StackElement(1)), new StackElement(2)),
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(1)),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(2), ConstantExpression.L0),
                new AssignmentStatement(new ArrayExpression(new StackElement(0), new StackElement(1)), new StackElement(2)),
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(2)),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(2), ConstantExpression.F0),
                new AssignmentStatement(new ArrayExpression(new StackElement(0), new StackElement(1)), new StackElement(2)),
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(3)),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(2), ConstantExpression.D0),
                new AssignmentStatement(new ArrayExpression(new StackElement(0), new StackElement(1)), new StackElement(2)),
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(4)),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(2), ConstantExpression.I0),
                new AssignmentStatement(new ArrayExpression(new StackElement(0), new StackElement(1)), new StackElement(2)),
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(5)),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(2), ConstantExpression.I0),
                new AssignmentStatement(new ArrayExpression(new StackElement(0), new StackElement(1)), new StackElement(2)),
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(6)),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(2), ConstantExpression.I0),
                new AssignmentStatement(new ArrayExpression(new StackElement(0), new StackElement(1)), new StackElement(2)),
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(7)),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(2), ConstantExpression.NULL),
                new AssignmentStatement(new ArrayExpression(new StackElement(0), new StackElement(1)), new StackElement(2)),
                //
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testDup() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.dup();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(1), new StackElement(0)),
                IdleStatement.POP2,
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testDupX1() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.dup_x1();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I1),
                new AssignmentStatement(new StackElement(2), new StackElement(1)),
                new AssignmentStatement(new StackElement(1), new StackElement(0)),
                new AssignmentStatement(new StackElement(0), new StackElement(2)),
                IdleStatement.POP2,
                IdleStatement.POP,
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testDupX2() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.dup_x2();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I1),
                new AssignmentStatement(new StackElement(2), ConstantExpression.I2),
                new AssignmentStatement(new StackElement(3), new StackElement(2)),
                new AssignmentStatement(new StackElement(2), new StackElement(1)),
                new AssignmentStatement(new StackElement(1), new StackElement(0)),
                new AssignmentStatement(new StackElement(0), new StackElement(3)),
                IdleStatement.POP2,
                IdleStatement.POP2,
                new AssignmentStatement(new StackElement(0), ConstantExpression.L0),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I1),
                new AssignmentStatement(new StackElement(2), new StackElement(1)),
                new AssignmentStatement(new StackElement(1), new StackElement(0)),
                new AssignmentStatement(new StackElement(0), new StackElement(2)),
                IdleStatement.POP,
                IdleStatement.POP2,
                IdleStatement.POP,
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testDup2() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.dup2();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I1),
                new AssignmentStatement(new StackElement(2), new StackElement(0)),
                new AssignmentStatement(new StackElement(3), new StackElement(1)),
                IdleStatement.POP2,
                IdleStatement.POP2,
                new AssignmentStatement(new StackElement(0), ConstantExpression.L0),
                new AssignmentStatement(new StackElement(1), new StackElement(0)),
                IdleStatement.POP2,
                IdleStatement.POP2,
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testDup2X1() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.dup2_x1();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I1),
                new AssignmentStatement(new StackElement(2), ConstantExpression.I2),
                new AssignmentStatement(new StackElement(3), new StackElement(1)),
                new AssignmentStatement(new StackElement(4), new StackElement(2)),
                new AssignmentStatement(new StackElement(2), new StackElement(0)),
                new AssignmentStatement(new StackElement(1), new StackElement(4)),
                new AssignmentStatement(new StackElement(0), new StackElement(3)),
                IdleStatement.POP2,
                IdleStatement.POP2,
                IdleStatement.POP,
                new AssignmentStatement(new StackElement(0), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(1), ConstantExpression.L1),
                new AssignmentStatement(new StackElement(2), new StackElement(1)),
                new AssignmentStatement(new StackElement(1), new StackElement(0)),
                new AssignmentStatement(new StackElement(0), new StackElement(2)),
                IdleStatement.POP2,
                IdleStatement.POP,
                IdleStatement.POP2,
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testDup2X2() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.dup2_x2();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I1),
                new AssignmentStatement(new StackElement(2), ConstantExpression.I2),
                new AssignmentStatement(new StackElement(3), ConstantExpression.I3),
                new AssignmentStatement(new StackElement(4), new StackElement(2)),
                new AssignmentStatement(new StackElement(5), new StackElement(3)),
                new AssignmentStatement(new StackElement(3), new StackElement(1)),
                new AssignmentStatement(new StackElement(2), new StackElement(0)),
                new AssignmentStatement(new StackElement(1), new StackElement(5)),
                new AssignmentStatement(new StackElement(0), new StackElement(4)),
                IdleStatement.POP2,
                IdleStatement.POP2,
                IdleStatement.POP2,
                //
                new AssignmentStatement(new StackElement(0), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I1),
                new AssignmentStatement(new StackElement(2), ConstantExpression.L1),
                new AssignmentStatement(new StackElement(3), new StackElement(2)),
                new AssignmentStatement(new StackElement(2), new StackElement(1)),
                new AssignmentStatement(new StackElement(1), new StackElement(0)),
                new AssignmentStatement(new StackElement(0), new StackElement(3)),
                IdleStatement.POP2,
                IdleStatement.POP2,
                IdleStatement.POP2,
                //
                new AssignmentStatement(new StackElement(0), ConstantExpression.L0),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I1),
                new AssignmentStatement(new StackElement(2), ConstantExpression.I2),
                new AssignmentStatement(new StackElement(3), new StackElement(1)),
                new AssignmentStatement(new StackElement(4), new StackElement(2)),
                new AssignmentStatement(new StackElement(2), new StackElement(0)),
                new AssignmentStatement(new StackElement(1), new StackElement(4)),
                new AssignmentStatement(new StackElement(0), new StackElement(3)),
                IdleStatement.POP2,
                IdleStatement.POP2,
                IdleStatement.POP2,
                //
                new AssignmentStatement(new StackElement(0), ConstantExpression.L0),
                new AssignmentStatement(new StackElement(1), ConstantExpression.L1),
                new AssignmentStatement(new StackElement(2), new StackElement(1)),
                new AssignmentStatement(new StackElement(1), new StackElement(0)),
                new AssignmentStatement(new StackElement(0), new StackElement(2)),
                IdleStatement.POP2,
                IdleStatement.POP2,
                IdleStatement.POP2,
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testSwap() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.swap();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I1),
                new AssignmentStatement(new StackElement(2), new StackElement(1)),
                new AssignmentStatement(new StackElement(1), new StackElement(0)),
                new AssignmentStatement(new StackElement(0), new StackElement(2)),
                IdleStatement.POP2,
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testAdd() throws Exception {
        testBinaryArithmeticOperation(BinaryOperation.ADD, InsnConverterTestMethods.add());
    }

    @Test
    public void testSubtract() throws Exception {
        testBinaryArithmeticOperation(BinaryOperation.SUBTRACT, InsnConverterTestMethods.subtract());
    }

    @Test
    public void testMultiply() throws Exception {
        testBinaryArithmeticOperation(BinaryOperation.MULTIPLY, InsnConverterTestMethods.multiply());
    }

    @Test
    public void testDivide() throws Exception {
        testBinaryArithmeticOperation(BinaryOperation.DIVIDE, InsnConverterTestMethods.divide());
    }

    @Test
    public void testRemainder() throws Exception {
        testBinaryArithmeticOperation(BinaryOperation.REMAINDER, InsnConverterTestMethods.remainder());
    }

    @Test
    public void testShift() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.shift();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(1), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(0), new BinaryExpression(BinaryOperation.SHIFT_LEFT,
                        new StackElement(0), new StackElement(1))),
                //
                new AssignmentStatement(new StackElement(1), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(0), new BinaryExpression(BinaryOperation.SHIFT_RIGHT_UNSIGNED,
                        new StackElement(0), new StackElement(1))),
                //
                new AssignmentStatement(new StackElement(1), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(0), new BinaryExpression(BinaryOperation.SHIFT_RIGHT,
                        new StackElement(0), new StackElement(1))),
                //
                IdleStatement.POP,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(1)),
                new AssignmentStatement(new StackElement(1), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(0), new BinaryExpression(BinaryOperation.SHIFT_LEFT,
                        new StackElement(0), new StackElement(1))),
                //
                new AssignmentStatement(new StackElement(1), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(0), new BinaryExpression(BinaryOperation.SHIFT_RIGHT_UNSIGNED,
                        new StackElement(0), new StackElement(1))),
                //
                new AssignmentStatement(new StackElement(1), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(0), new BinaryExpression(BinaryOperation.SHIFT_RIGHT,
                        new StackElement(0), new StackElement(1))),
                //
                IdleStatement.POP2,
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testAnd() throws Exception {
        testBinaryLogicalOperation(BinaryOperation.BITWISE_AND, InsnConverterTestMethods.and());
    }

    @Test
    public void testOr() throws Exception {
        testBinaryLogicalOperation(BinaryOperation.BITWISE_OR, InsnConverterTestMethods.or());
    }

    @Test
    public void testXor() throws Exception {
        testBinaryLogicalOperation(BinaryOperation.BITWISE_XOR, InsnConverterTestMethods.xor());
    }

    @Test
    public void testNegate() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.negate();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(0), new UnaryExpression(NEGATE, new StackElement(0))),
                IdleStatement.POP,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(1)),
                new AssignmentStatement(new StackElement(0), new UnaryExpression(NEGATE, new StackElement(0))),
                IdleStatement.POP2,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(3)),
                new AssignmentStatement(new StackElement(0), new UnaryExpression(NEGATE, new StackElement(0))),
                IdleStatement.POP,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(4)),
                new AssignmentStatement(new StackElement(0), new UnaryExpression(NEGATE, new StackElement(0))),
                IdleStatement.POP2,
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testIinc() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.iinc();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new LocalVariable(0),
                        new BinaryExpression(BinaryOperation.ADD, new LocalVariable(0), ConstantExpression.I4)),
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testCast() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.cast();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(0),
                        new UnaryExpression(CastOperation.TO_LONG, new StackElement(0))),
                IdleStatement.POP2,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(0),
                        new UnaryExpression(CastOperation.TO_FLOAT, new StackElement(0))),
                IdleStatement.POP,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(0),
                        new UnaryExpression(CastOperation.TO_DOUBLE, new StackElement(0))),
                IdleStatement.POP2,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(1)),
                new AssignmentStatement(new StackElement(0),
                        new UnaryExpression(CastOperation.TO_INT, new StackElement(0))),
                IdleStatement.POP,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(1)),
                new AssignmentStatement(new StackElement(0),
                        new UnaryExpression(CastOperation.TO_FLOAT, new StackElement(0))),
                IdleStatement.POP,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(1)),
                new AssignmentStatement(new StackElement(0),
                        new UnaryExpression(CastOperation.TO_DOUBLE, new StackElement(0))),
                IdleStatement.POP2,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(3)),
                new AssignmentStatement(new StackElement(0),
                        new UnaryExpression(CastOperation.TO_INT, new StackElement(0))),
                IdleStatement.POP,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(3)),
                new AssignmentStatement(new StackElement(0),
                        new UnaryExpression(CastOperation.TO_LONG, new StackElement(0))),
                IdleStatement.POP2,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(3)),
                new AssignmentStatement(new StackElement(0),
                        new UnaryExpression(CastOperation.TO_DOUBLE, new StackElement(0))),
                IdleStatement.POP2,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(4)),
                new AssignmentStatement(new StackElement(0),
                        new UnaryExpression(CastOperation.TO_INT, new StackElement(0))),
                IdleStatement.POP,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(4)),
                new AssignmentStatement(new StackElement(0),
                        new UnaryExpression(CastOperation.TO_LONG, new StackElement(0))),
                IdleStatement.POP2,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(4)),
                new AssignmentStatement(new StackElement(0),
                        new UnaryExpression(CastOperation.TO_FLOAT, new StackElement(0))),
                IdleStatement.POP,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(0),
                        new UnaryExpression(CastOperation.TO_BYTE, new StackElement(0))),
                IdleStatement.POP,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(0),
                        new UnaryExpression(CastOperation.TO_CHAR, new StackElement(0))),
                IdleStatement.POP,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(0),
                        new UnaryExpression(CastOperation.TO_SHORT, new StackElement(0))),
                IdleStatement.POP,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(6)),
                new AssignmentStatement(new StackElement(0),
                        new UnaryExpression(new CastOperation("java/lang/String"), new StackElement(0))),
                IdleStatement.POP,
                //
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testArrayLength() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.arrayLength();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(0),
                        new UnaryExpression(ARRAY_LENGTH, new StackElement(0))),
                IdleStatement.POP,
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testAthrow() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.athrow();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new ThrowStatement(new StackElement(0))
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testInstanceOf() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.instanceOf();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(0),
                        new UnaryExpression(new InstanceOfOperation("java/lang/String"), new StackElement(0))),
                IdleStatement.POP,
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testIReturn() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.iReturn();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.I0),
                new ReturnStatement(new StackElement(0))
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testLReturn() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.lReturn();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.L0),
                new ReturnStatement(new StackElement(0))
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testFReturn() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.fReturn();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.F0),
                new ReturnStatement(new StackElement(0))
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testDReturn() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.dReturn();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.D0),
                new ReturnStatement(new StackElement(0))
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testAReturn() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.aReturn();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.NULL),
                new ReturnStatement(new StackElement(0))
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testMonitor() throws Exception {
        MethodNode methodNode = InsnConverterTestMethods.monitor();
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new MonitorStatement(MonitorOperation.ENTER, new StackElement(0)),
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new MonitorStatement(MonitorOperation.EXIT, new StackElement(0)),
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    private static void testBinaryLogicalOperation(BinaryOperation operation, MethodNode methodNode) throws Exception {
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(1), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(0), new BinaryExpression(operation,
                        new StackElement(0), new StackElement(1))),
                IdleStatement.POP,
                new AssignmentStatement(new StackElement(0), new LocalVariable(1)),
                new AssignmentStatement(new StackElement(1), new LocalVariable(1)),
                new AssignmentStatement(new StackElement(0), new BinaryExpression(operation,
                        new StackElement(0), new StackElement(1))),
                IdleStatement.POP2,
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    private static void testBinaryArithmeticOperation(BinaryOperation operation, MethodNode methodNode) throws Exception {
        SSAAnalyzer analyzer = new SSAAnalyzer(OWNER, methodNode);
        List<Statement> actualStatements = analyzer.getFlattenedStatementsList();
        List<Statement> expectedStatements = new LinkedList<>();
        for(int i = 0; i < 4; i++) {
            int local = i > 1 ? i + 1 : i;
            expectedStatements.add(new AssignmentStatement(new StackElement(i), new LocalVariable(local)));
            expectedStatements.add(new AssignmentStatement(new StackElement(i + 1), new LocalVariable(local)));
            expectedStatements.add(new AssignmentStatement(new StackElement(i),
                    new BinaryExpression(operation, new StackElement(i), new StackElement(i + 1))));
        }
        expectedStatements.add(new InvokeStatement(new InvokeExpression(OWNER, "example", null, new Expression[]{
                new StackElement(0),
                new StackElement(1),
                new StackElement(2),
                new StackElement(3),
        })));
        expectedStatements.add(new ReturnStatement(null));
        assertEquals(expectedStatements, actualStatements);
    }
}