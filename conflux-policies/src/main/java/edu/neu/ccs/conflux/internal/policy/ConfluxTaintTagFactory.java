package edu.neu.ccs.conflux.internal.policy;

import edu.columbia.cs.psl.phosphor.instrumenter.DataAndControlFlowTagFactory;
import edu.columbia.cs.psl.phosphor.instrumenter.LocalVariableManager;
import edu.columbia.cs.psl.phosphor.instrumenter.TaintPassingMV;
import edu.columbia.cs.psl.phosphor.instrumenter.TaintTagFactory;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.Label;
import edu.columbia.cs.psl.phosphor.org.objectweb.asm.MethodVisitor;
import edu.columbia.cs.psl.phosphor.runtime.StringUtils;
import edu.columbia.cs.psl.phosphor.runtime.Taint;

/**
 * Uses Phosphor's default propagation logic.
 * Indicates to Phosphor that the package "edu/neu/ccs/conflux/internal" should not be instrumented and
 * the package "edu/neu/ccs/conflux/internal/runtime" contains internal tainting classes.
 */
public class ConfluxTaintTagFactory implements TaintTagFactory {

    private final TaintTagFactory delegate = new DataAndControlFlowTagFactory();

    @Override
    public boolean isIgnoredClass(String className) {
        return StringUtils.startsWith(className, "edu/neu/ccs/conflux/internal");
    }

    @Override
    public boolean isInternalTaintingClass(String className) {
        return StringUtils.startsWith(className, "edu/neu/ccs/conflux/internal/runtime");
    }

    @Override
    public Taint<?> getAutoTaint(String source) {
        return delegate.getAutoTaint(source);
    }

    @Override
    public void instrumentationStarting(String className) {
        delegate.instrumentationStarting(className);
    }

    @Override
    public void instrumentationStarting(int access, String methodName, String methodDesc) {
        delegate.instrumentationStarting(access, methodName, methodDesc);
    }

    @Override
    public void instrumentationEnding(String className) {
        delegate.instrumentationEnding(className);
    }

    @Override
    public void insnIndexVisited(int offset) {
        delegate.insnIndexVisited(offset);
    }

    @Override
    public void generateEmptyTaint(MethodVisitor methodVisitor) {
        delegate.generateEmptyTaint(methodVisitor);
    }

    @Override
    public void generateEmptyTaintArray(Object[] array, int dimensions) {
        delegate.generateEmptyTaintArray(array, dimensions);
    }

    @Override
    public void methodOp(int i, String s, String s1, String s2, boolean b, MethodVisitor methodVisitor,
                         LocalVariableManager localVariableManager, TaintPassingMV taintPassingMV) {
        delegate.methodOp(i, s, s1, s2, b, methodVisitor, localVariableManager, taintPassingMV);
    }

    @Override
    public void stackOp(int i, MethodVisitor methodVisitor, LocalVariableManager localVariableManager,
                        TaintPassingMV taintPassingMV) {
        delegate.stackOp(i, methodVisitor, localVariableManager, taintPassingMV);
    }

    @Override
    public void jumpOp(int i, Label label, MethodVisitor methodVisitor, LocalVariableManager localVariableManager,
                       TaintPassingMV taintPassingMV) {
        delegate.jumpOp(i, label, methodVisitor, localVariableManager, taintPassingMV);
    }

    @Override
    public void typeOp(int i, String s, MethodVisitor methodVisitor, LocalVariableManager localVariableManager,
                       TaintPassingMV taintPassingMV) {
        delegate.typeOp(i, s, methodVisitor, localVariableManager, taintPassingMV);
    }

    @Override
    public void iincOp(int i, int i1, MethodVisitor methodVisitor, LocalVariableManager localVariableManager,
                       TaintPassingMV taintPassingMV) {
        delegate.iincOp(i, i1, methodVisitor, localVariableManager, taintPassingMV);
    }

    @Override
    public void intOp(int i, int i1, MethodVisitor methodVisitor, LocalVariableManager localVariableManager,
                      TaintPassingMV taintPassingMV) {
        delegate.intOp(i, i1, methodVisitor, localVariableManager, taintPassingMV);
    }

    @Override
    public void signalOp(int signal, Object option) {
        delegate.signalOp(signal, option);
    }

    @Override
    public void fieldOp(int i, String s, String s1, String s2, MethodVisitor methodVisitor,
                        LocalVariableManager localVariableManager, TaintPassingMV taintPassingMV, boolean b) {
        delegate.fieldOp(i, s, s1, s2, methodVisitor, localVariableManager, taintPassingMV, b);
    }

    @Override
    public void methodEntered(String s, String s1, String s2, MethodVisitor methodVisitor,
                              LocalVariableManager localVariableManager, TaintPassingMV taintPassingMV) {
        delegate.methodEntered(s, s1, s2, methodVisitor, localVariableManager, taintPassingMV);
    }

    @Override
    public void lineNumberVisited(int line) {
        delegate.lineNumberVisited(line);
    }

    @Override
    public void lookupSwitch(Label label, int[] ints, Label[] labels, MethodVisitor methodVisitor,
                             LocalVariableManager localVariableManager, TaintPassingMV taintPassingMV) {
        delegate.lookupSwitch(label, ints, labels, methodVisitor, localVariableManager, taintPassingMV);
    }

    @Override
    public void tableSwitch(int i, int i1, Label label, Label[] labels, MethodVisitor methodVisitor,
                            LocalVariableManager localVariableManager, TaintPassingMV taintPassingMV) {
        delegate.tableSwitch(i, i1, label, labels, methodVisitor, localVariableManager, taintPassingMV);
    }

    @Override
    public void propagateTagNative(String s, int i, String s1, String s2, MethodVisitor methodVisitor) {
        delegate.propagateTagNative(s, i, s1, s2, methodVisitor);
    }

    @Override
    public void generateSetTag(MethodVisitor methodVisitor, String s) {
        delegate.generateSetTag(methodVisitor, s);
    }
}
