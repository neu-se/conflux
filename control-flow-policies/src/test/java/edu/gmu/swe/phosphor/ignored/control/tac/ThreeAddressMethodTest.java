package edu.gmu.swe.phosphor.ignored.control.tac;

import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.AbstractInsnNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.LabelNode;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.tree.MethodNode;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.*;
import edu.gmu.swe.phosphor.ignored.control.ssa.statement.*;
import edu.gmu.swe.phosphor.ignored.control.tac.ThreeAddressMethod;
import edu.gmu.swe.phosphor.ignored.control.tac.ThreeAddressMethodTestMethods;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static edu.columbia.cs.psl.phosphor.org.objectweb.asm.Opcodes.F_NEW;
import static edu.gmu.swe.phosphor.ignored.control.tac.ThreeAddressMethodTestMethods.OWNER;
import static edu.gmu.swe.phosphor.ignored.control.ssa.expression.ArrayLengthOperation.ARRAY_LENGTH;
import static edu.gmu.swe.phosphor.ignored.control.ssa.expression.InvocationType.*;
import static edu.gmu.swe.phosphor.ignored.control.ssa.expression.NegateOperation.NEGATE;
import static org.junit.Assert.assertEquals;

public class ThreeAddressMethodTest {

    @Test
    public void testPushPopConstants() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.pushPopConstants();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
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
        MethodNode methodNode = ThreeAddressMethodTestMethods.loadLocals();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
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
                }, INVOKE_STATIC)),
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testStoreLocals() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.storeLocals();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
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
        MethodNode methodNode = ThreeAddressMethodTestMethods.loadArrayElements();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
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
                }, INVOKE_STATIC)),
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testStoreArrayElements() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.storeArrayElements();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
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
        MethodNode methodNode = ThreeAddressMethodTestMethods.dup();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
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
        MethodNode methodNode = ThreeAddressMethodTestMethods.dup_x1();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
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
        MethodNode methodNode = ThreeAddressMethodTestMethods.dup_x2();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
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
        MethodNode methodNode = ThreeAddressMethodTestMethods.dup2();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
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
        MethodNode methodNode = ThreeAddressMethodTestMethods.dup2_x1();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
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
        MethodNode methodNode = ThreeAddressMethodTestMethods.dup2_x2();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
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
        MethodNode methodNode = ThreeAddressMethodTestMethods.swap();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
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
        testBinaryArithmeticOperation(BinaryOperation.ADD, ThreeAddressMethodTestMethods.add());
    }

    @Test
    public void testSubtract() throws Exception {
        testBinaryArithmeticOperation(BinaryOperation.SUBTRACT, ThreeAddressMethodTestMethods.subtract());
    }

    @Test
    public void testMultiply() throws Exception {
        testBinaryArithmeticOperation(BinaryOperation.MULTIPLY, ThreeAddressMethodTestMethods.multiply());
    }

    @Test
    public void testDivide() throws Exception {
        testBinaryArithmeticOperation(BinaryOperation.DIVIDE, ThreeAddressMethodTestMethods.divide());
    }

    @Test
    public void testRemainder() throws Exception {
        testBinaryArithmeticOperation(BinaryOperation.REMAINDER, ThreeAddressMethodTestMethods.remainder());
    }

    @Test
    public void testShift() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.shift();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
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
        testBinaryLogicalOperation(BinaryOperation.BITWISE_AND, ThreeAddressMethodTestMethods.and());
    }

    @Test
    public void testOr() throws Exception {
        testBinaryLogicalOperation(BinaryOperation.BITWISE_OR, ThreeAddressMethodTestMethods.or());
    }

    @Test
    public void testXor() throws Exception {
        testBinaryLogicalOperation(BinaryOperation.BITWISE_XOR, ThreeAddressMethodTestMethods.xor());
    }

    @Test
    public void testNegate() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.negate();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
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
        MethodNode methodNode = ThreeAddressMethodTestMethods.iinc();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new LocalVariable(0),
                        new BinaryExpression(BinaryOperation.ADD, new LocalVariable(0), ConstantExpression.I4)),
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testCast() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.cast();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
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
        MethodNode methodNode = ThreeAddressMethodTestMethods.arrayLength();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
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
        MethodNode methodNode = ThreeAddressMethodTestMethods.athrow();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new ThrowStatement(new StackElement(0))
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testInstanceOf() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.instanceOf();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
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
        MethodNode methodNode = ThreeAddressMethodTestMethods.iReturn();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.I0),
                new ReturnStatement(new StackElement(0))
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testLReturn() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.lReturn();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.L0),
                new ReturnStatement(new StackElement(0))
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testFReturn() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.fReturn();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.F0),
                new ReturnStatement(new StackElement(0))
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testDReturn() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.dReturn();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.D0),
                new ReturnStatement(new StackElement(0))
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testAReturn() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.aReturn();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.NULL),
                new ReturnStatement(new StackElement(0))
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testMonitor() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.monitor();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new MonitorStatement(MonitorOperation.ENTER, new StackElement(0)),
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new MonitorStatement(MonitorOperation.EXIT, new StackElement(0)),
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testGetFields() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.getFields();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(0), new FieldExpression(OWNER, "i", new StackElement(0))),
                IdleStatement.POP,
                new AssignmentStatement(new StackElement(0), new FieldExpression(OWNER, "b", null)),
                IdleStatement.POP,
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testPutFields() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.putFields();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.I1),
                new AssignmentStatement(new FieldExpression(OWNER, "b", null), new StackElement(0)),
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I0),
                new AssignmentStatement(new FieldExpression(OWNER, "i", new StackElement(0)), new StackElement(1)),
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testInvokeDynamic() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.invokeDynamic();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(0),
                        new InvokeExpression(null, "get", null,
                                new Expression[]{new StackElement(0)},
                                INVOKE_DYNAMIC)),
                IdleStatement.POP,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(1)),
                new AssignmentStatement(new StackElement(1), new LocalVariable(2)),
                new AssignmentStatement(new StackElement(0),
                        new InvokeExpression(null, "get", null,
                                new Expression[]{new StackElement(0), new StackElement(1)},
                                INVOKE_DYNAMIC)),
                IdleStatement.POP,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(0),
                        new InvokeExpression(null, "get", null,
                                new Expression[]{new StackElement(0)},
                                INVOKE_DYNAMIC)),
                IdleStatement.POP,
                //
                new AssignmentStatement(new StackElement(0),
                        new InvokeExpression(null, "get", null, new Expression[]{}, INVOKE_DYNAMIC)),
                IdleStatement.POP,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(3)),
                new AssignmentStatement(new StackElement(0),
                        new InvokeExpression(null, "compare", null, new Expression[]{new StackElement(0)},
                                INVOKE_DYNAMIC)),
                IdleStatement.POP,
                //
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testInvokeInterface() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.invokeInterface();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new InvokeStatement(new InvokeExpression("java/lang/Runnable", "run", new StackElement(0),
                        new Expression[0], INVOKE_INTERFACE)),
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testInvokeVirtual() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.invokeVirtual();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(0),
                        new InvokeExpression("java/lang/Object", "toString", new StackElement(0), new Expression[0],
                                INVOKE_VIRTUAL)),
                IdleStatement.POP,
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testInvokeStatic() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.invokeStatic();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(1), new LocalVariable(1)),
                new AssignmentStatement(new StackElement(2), new LocalVariable(2)),
                new AssignmentStatement(new StackElement(0),
                        new InvokeExpression(OWNER, "example", null, new Expression[]{
                                new StackElement(0),
                                new StackElement(1),
                                new StackElement(2)
                        }, INVOKE_STATIC)),
                new ReturnStatement(new StackElement(0))
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testConstructorCall() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.constructorCall();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new NewExpression("java/lang/String")),
                new AssignmentStatement(new StackElement(1), new StackElement(0)),
                new AssignmentStatement(new StackElement(2), new LocalVariable(0)),
                new InvokeStatement(new InvokeExpression("java/lang/String", "<init>", new StackElement(1),
                        new Expression[]{new StackElement(2)}, INVOKE_SPECIAL)),
                IdleStatement.POP,
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testNewArray() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.newArray();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.I5),
                new AssignmentStatement(new StackElement(0), new NewArrayExpression("I", new StackElement(0))),
                IdleStatement.POP,
                new AssignmentStatement(new StackElement(0), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(0), new NewArrayExpression("Ljava/lang/String;",
                        new StackElement(0))),
                IdleStatement.POP,
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testMultiNewArray() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.multiNewArray();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), ConstantExpression.I2),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I1),
                new AssignmentStatement(new StackElement(0), new NewArrayExpression("Ljava/lang/String;",
                        new Expression[]{new StackElement(0), new StackElement(1)})),
                IdleStatement.POP,
                new AssignmentStatement(new StackElement(0), ConstantExpression.I3),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I2),
                new AssignmentStatement(new StackElement(2), ConstantExpression.I1),
                new AssignmentStatement(new StackElement(0), new NewArrayExpression("Ljava/lang/String;",
                        new Expression[]{new StackElement(0), new StackElement(1), new StackElement(2), null})),
                IdleStatement.POP,
                new AssignmentStatement(new StackElement(0), ConstantExpression.I0),
                new AssignmentStatement(new StackElement(0), new NewArrayExpression("Ljava/lang/String;",
                        new Expression[]{new StackElement(0), null, null, null})),
                IdleStatement.POP,
                new AssignmentStatement(new StackElement(0), ConstantExpression.I1),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I0),

                new AssignmentStatement(new StackElement(0), new NewArrayExpression("I",
                        new Expression[]{new StackElement(0), new StackElement(1), null, null})),
                IdleStatement.POP,
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testCompare() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.compare();
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(1), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(0), new BinaryExpression(BinaryOperation.COMPARE,
                        new StackElement(0), new StackElement(1))),
                IdleStatement.POP,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(2)),
                new AssignmentStatement(new StackElement(1), new LocalVariable(2)),
                new AssignmentStatement(new StackElement(0), new BinaryExpression(BinaryOperation.COMPARE_L,
                        new StackElement(0), new StackElement(1))),
                IdleStatement.POP,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(2)),
                new AssignmentStatement(new StackElement(1), new LocalVariable(2)),
                new AssignmentStatement(new StackElement(0), new BinaryExpression(BinaryOperation.COMPARE_G,
                        new StackElement(0), new StackElement(1))),
                IdleStatement.POP,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(3)),
                new AssignmentStatement(new StackElement(1), new LocalVariable(3)),
                new AssignmentStatement(new StackElement(0), new BinaryExpression(BinaryOperation.COMPARE_L,
                        new StackElement(0), new StackElement(1))),
                IdleStatement.POP,
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(3)),
                new AssignmentStatement(new StackElement(1), new LocalVariable(3)),
                new AssignmentStatement(new StackElement(0), new BinaryExpression(BinaryOperation.COMPARE_G,
                        new StackElement(0), new StackElement(1))),
                IdleStatement.POP,
                //
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testTableSwitch() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.tableSwitch();
        Label l0 = getFirstLabel(methodNode);
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new SwitchStatement(new StackElement(0), l0, new Label[]{l0, l0, l0, l0, l0},
                        new int[]{0, 1, 2, 3, 4}),
                new LabelStatement(l0),
                new FrameStatement(F_NEW, 0, new Object[0], 0, new Object[0]),
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testLookupSwitch() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.lookupSwitch();
        Label l0 = getFirstLabel(methodNode);
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new SwitchStatement(new StackElement(0), l0, new Label[]{l0, l0, l0, l0},
                        new int[]{0, 10, 78, 100}),
                new LabelStatement(l0),
                new FrameStatement(F_NEW, 0, new Object[0], 0, new Object[0]),
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testUnaryIf() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.unaryIf();
        Label l0 = getFirstLabel(methodNode);
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new IfStatement(new ConditionExpression(Condition.NOT_EQUAL, new StackElement(0),
                        ConstantExpression.I0), l0),
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new IfStatement(new ConditionExpression(Condition.EQUAL, new StackElement(0),
                        ConstantExpression.I0), l0),
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new IfStatement(new ConditionExpression(Condition.LESS_THAN, new StackElement(0),
                        ConstantExpression.I0), l0),
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new IfStatement(new ConditionExpression(Condition.GREATER_THAN_OR_EQUAL, new StackElement(0),
                        ConstantExpression.I0), l0),
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new IfStatement(new ConditionExpression(Condition.GREATER_THAN, new StackElement(0),
                        ConstantExpression.I0), l0),
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new IfStatement(new ConditionExpression(Condition.LESS_THAN_OR_EQUAL, new StackElement(0),
                        ConstantExpression.I0), l0),
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(1)),
                new IfStatement(new ConditionExpression(Condition.EQUAL, new StackElement(0),
                        ConstantExpression.NULL), l0),
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(1)),
                new IfStatement(new ConditionExpression(Condition.NOT_EQUAL, new StackElement(0),
                        ConstantExpression.NULL), l0),
                //
                new LabelStatement(l0),
                new FrameStatement(F_NEW, 0, new Object[0], 0, new Object[0]),
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    @Test
    public void testBinaryIf() throws Exception {
        MethodNode methodNode = ThreeAddressMethodTestMethods.binaryIf();
        Label l0 = getFirstLabel(methodNode);
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
        List<Statement> expectedStatements = new LinkedList<>(Arrays.asList(
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I1),
                new IfStatement(new ConditionExpression(Condition.EQUAL, new StackElement(0),
                        new StackElement(1)), l0),
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I1),
                new IfStatement(new ConditionExpression(Condition.NOT_EQUAL, new StackElement(0),
                        new StackElement(1)), l0),
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I1),
                new IfStatement(new ConditionExpression(Condition.LESS_THAN, new StackElement(0),
                        new StackElement(1)), l0),
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I2),
                new IfStatement(new ConditionExpression(Condition.GREATER_THAN_OR_EQUAL, new StackElement(0),
                        new StackElement(1)), l0),
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I3),

                new IfStatement(new ConditionExpression(Condition.GREATER_THAN, new StackElement(0),
                        new StackElement(1)), l0),
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(0)),
                new AssignmentStatement(new StackElement(1), ConstantExpression.I4),
                new IfStatement(new ConditionExpression(Condition.LESS_THAN_OR_EQUAL, new StackElement(0),
                        new StackElement(1)), l0),
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(1)),
                new AssignmentStatement(new StackElement(1), new LocalVariable(1)),
                new IfStatement(new ConditionExpression(Condition.EQUAL, new StackElement(0),
                        new StackElement(1)), l0),
                //
                new AssignmentStatement(new StackElement(0), new LocalVariable(1)),
                new AssignmentStatement(new StackElement(1), new LocalVariable(1)),
                new IfStatement(new ConditionExpression(Condition.NOT_EQUAL, new StackElement(0),
                        new StackElement(1)), l0),
                //
                new LabelStatement(l0),
                new FrameStatement(F_NEW, 0, new Object[0], 0, new Object[0]),
                new ReturnStatement(null)
        ));
        assertEquals(expectedStatements, actualStatements);
    }

    private static void testBinaryLogicalOperation(BinaryOperation operation, MethodNode methodNode) throws Exception {
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
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
        ThreeAddressMethod method = new ThreeAddressMethod(OWNER, methodNode);
        List<Statement> actualStatements = createStatementList(method);
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
        }, INVOKE_STATIC)));
        expectedStatements.add(new ReturnStatement(null));
        assertEquals(expectedStatements, actualStatements);
    }

    private static Label getFirstLabel(MethodNode methodNode) {
        Iterator<AbstractInsnNode> itr = methodNode.instructions.iterator();
        while(itr.hasNext()) {
            AbstractInsnNode insn = itr.next();
            if(insn instanceof LabelNode) {
                return ((LabelNode) insn).getLabel();
            }
        }
        throw new IllegalArgumentException();
    }

    private static List<Statement> createStatementList(ThreeAddressMethod method) {
        List<Statement> statements = new LinkedList<>();
        Iterator<AbstractInsnNode> itr = method.getOriginalMethod().instructions.iterator();
        while(itr.hasNext()) {
            Statement[] s = method.getStatementMap().get(itr.next());
            statements.addAll(Arrays.asList(s));
        }
        return statements;
    }
}