package edu.gmu.swe.phosphor.ignored.control.ssa;

import edu.columbia.cs.psl.phosphor.struct.SinglyLinkedList;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashMap;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.gmu.swe.phosphor.ignored.control.ssa.expression.VariableExpression;

public class VersionStack {

    private final VariableExpression baseExpression;
    private final SinglyLinkedList<SinglyLinkedList<VariableExpression>> currentVersion = new SinglyLinkedList<>();
    /**
     * A mapping from each VariableExpression created by this stack to the version the base expression was defined with
     * before the VariableExpression redefined it, null if the base expression was previously undefined.
     */
    private final Map<VariableExpression, VariableExpression> redefinitionMap = new HashMap<>();
    private int nextVersion = 0;

    public VersionStack(VariableExpression baseExpression) {
        this.baseExpression = baseExpression;
    }

    public boolean hasCurrentExpression() {
        return !currentVersion.peek().isEmpty();
    }

    public VariableExpression getCurrentExpression() {
        return currentVersion.peek().peek();
    }

    public VariableExpression createNewVersion() {
        VariableExpression previous = null;
        if(hasCurrentExpression()) {
            previous = getCurrentExpression();
        }
        VariableExpression v = baseExpression.setVersion(nextVersion++);
        currentVersion.peek().push(v);
        redefinitionMap.put(v, previous);
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

    public VariableExpression getRedefines(VariableExpression e) {
        return redefinitionMap.get(e);
    }
}
