package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.struct.SinglyLinkedList;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VersionedExpression;

public class VersionStack {

    private final VersionedExpression baseExpression;
    private final SinglyLinkedList<SinglyLinkedList<VersionedExpression>> currentVersion = new SinglyLinkedList<>();
    private int nextVersion = 0;

    public VersionStack(VersionedExpression baseExpression) {
        this.baseExpression = baseExpression;
    }

    public VersionedExpression getCurrentExpression() {
        return currentVersion.peek().peek();
    }

    public VersionedExpression createNewVersion() {
        VersionedExpression v = baseExpression.setVersion(nextVersion++);
        currentVersion.peek().push(v);
        return v;
    }

    public void processingBlock() {
        SinglyLinkedList<VersionedExpression> next = new SinglyLinkedList<>();
        if(!currentVersion.isEmpty() && !currentVersion.peek().isEmpty()) {
            next.push(currentVersion.peek().peek());
        }
        currentVersion.push(next);
    }

    public void finishedProcessingBlock() {
        currentVersion.pop();
    }
}
