package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.struct.SinglyLinkedList;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;

public class VersionStack {

    private final VariableExpression baseExpression;
    private final SinglyLinkedList<SinglyLinkedList<VariableExpression>> currentVersion = new SinglyLinkedList<>();
    private int nextVersion = 0;

    public VersionStack(VariableExpression baseExpression) {
        this.baseExpression = baseExpression;
    }

    public VariableExpression getCurrentExpression() {
        return currentVersion.peek().peek();
    }

    public VariableExpression createNewVersion() {
        VariableExpression v = baseExpression.setVersion(nextVersion++);
        currentVersion.peek().push(v);
        return v;
    }

    public void processingBlock() {
        SinglyLinkedList<VariableExpression> next = new SinglyLinkedList<>();
        if(!currentVersion.isEmpty() && !currentVersion.peek().isEmpty()) {
            next.push(currentVersion.peek().peek());
        }
        currentVersion.push(next);
    }

    public void finishedProcessingBlock() {
        currentVersion.pop();
    }
}
