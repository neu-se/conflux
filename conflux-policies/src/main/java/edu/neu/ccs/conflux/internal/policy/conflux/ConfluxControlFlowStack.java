package edu.neu.ccs.conflux.internal.policy.conflux;

import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Arrays;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.HashMap;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.Map;
import edu.columbia.cs.psl.phosphor.struct.harmony.util.StringBuilder;
import edu.neu.ccs.conflux.internal.policy.exception.ExceptionTrackingControlFlowStack;

public final class ConfluxControlFlowStack<E> extends ExceptionTrackingControlFlowStack<E> {

    @SuppressWarnings("rawtypes")
    private static final ConfluxControlFlowStack disabledInstance = new ConfluxControlFlowStack(true);
    private static final int NOT_PUSHED = -1;
    private ControlFrame<E> stackTop;
    private final ControlFrameBuilder<E> frameBuilder;
    private Taint<E> nextBranchTag;

    public ConfluxControlFlowStack() {
        this(false);
    }

    public ConfluxControlFlowStack(boolean disabled) {
        super(disabled);
        stackTop = new ControlFrame<>(0, null, null);
        frameBuilder = new ControlFrameBuilder<>();
        nextBranchTag = Taint.emptyTaint();
    }

    private ConfluxControlFlowStack(ConfluxControlFlowStack<E> stack) {
        super(stack);
        stackTop = new ControlFrame<>(stack.stackTop.invocationLevel, stack.stackTop.argumentStabilityLevels, null);
        stackTop.levelStackMap.putAll(stack.stackTop.levelStackMap);
        frameBuilder = stack.frameBuilder.copy();
        nextBranchTag = Taint.emptyTaint();
    }

    @Override
    public ConfluxControlFlowStack<E> copyTop() {
        return new ConfluxControlFlowStack<>(this);
    }

    @Override
    public void enteringUninstrumentedWrapper() {
        frameBuilder.reset();
    }

    public ConfluxControlFlowStack<E> startFrame(int invocationLevel, int numArguments) {
        frameBuilder.start(invocationLevel, numArguments);
        return this;
    }

    public ConfluxControlFlowStack<E> setNextFrameArgStable() {
        frameBuilder.setNextArgLevel(0);
        return this;
    }

    public ConfluxControlFlowStack<E> setNextFrameArgDependent(int[] dependencies) {
        frameBuilder.setNextArgLevel(getLevel(dependencies));
        return this;
    }

    public ConfluxControlFlowStack<E> setNextFrameArgUnstable(int levelOffset) {
        frameBuilder.setNextArgLevel(getLevel(levelOffset));
        return this;
    }

    @Override
    public void pushFrame() {
        stackTop = frameBuilder.build(stackTop);
    }

    public int getLevel(int levelOffset) {
        return stackTop.getLevel(levelOffset);
    }

    public int getLevel(int[] dependencies) {
        return stackTop.getLevel(dependencies);
    }

    @Override
    public void popFrame() {
        stackTop = stackTop.next;
    }

    public Taint<E> copyTagStable() {
        return isDisabled() ? Taint.emptyTaint() : stackTop.copyTag(0);
    }

    public Taint<E> copyTagDependent(int[] dependencies) {
        return isDisabled() ? Taint.emptyTaint() : stackTop.copyTag(getLevel(dependencies));
    }

    public Taint<E> copyTagUnstable(int levelOffset) {
        return isDisabled() ? Taint.emptyTaint() : stackTop.copyTag(getLevel(levelOffset));
    }

    public void pushStable(int branchID, int branchesSize) {
        if(!isDisabled()) {
            stackTop.push(nextBranchTag, branchID, branchesSize, 0);
        }
    }

    public void pushDependent(int branchID, int branchesSize, int[] dependencies) {
        if(!isDisabled()) {
            stackTop.push(nextBranchTag, branchID, branchesSize, getLevel(dependencies));
        }
    }

    public void pushUnstable(int branchID, int branchesSize, int levelOffset) {
        if(!isDisabled()) {
            stackTop.push(nextBranchTag, branchID, branchesSize, getLevel(levelOffset));
        }
    }

    public void pop(int branchID) {
        stackTop.pop(branchID);
    }

    @Override
    public void reset() {
        stackTop.reset();
        nextBranchTag = Taint.emptyTaint();
    }

    public void exitLoopLevel(int levelOffset) {
        stackTop.exitLoopLevel(levelOffset);
    }

    public void setNextBranchTag(Taint<E> nextBranchTag) {
        this.nextBranchTag = nextBranchTag;
    }

    @Override
    public Taint<E> copyTag() {
        return copyTagStable();
    }

    private static final class ControlFrame<E> {

        private final int invocationLevel;
        private final int[] argumentStabilityLevels;
        private final Map<Integer, Node<E>> levelStackMap;
        private int[] branchLevels;
        private final ControlFrame<E> next;

        private ControlFrame(int invocationLevel, int[] argumentStabilityLevels, ControlFrame<E> next) {
            this.next = next;
            this.invocationLevel = invocationLevel;
            this.argumentStabilityLevels = argumentStabilityLevels;
            if(next == null) {
                levelStackMap = new HashMap<>();
            } else {
                levelStackMap = new HashMap<>(next.levelStackMap);
            }
        }

        int getLevel(int levelOffset) {
            return invocationLevel + levelOffset;
        }

        int getLevel(int[] dependencies) {
            if(argumentStabilityLevels == null) {
                return 0;
            } else {
                int max = 0;
                for(int dependency : dependencies) {
                    int value = argumentStabilityLevels[dependency];
                    if(value > max) {
                        max = value;
                    }
                }
                return max;
            }
        }

        Taint<E> copyTag(int level) {
            Taint<E> tag = Taint.emptyTaint();
            for(Integer key : levelStackMap.keySet()) {
                if(key <= level) {
                    tag = Taint.combineTags(tag, levelStackMap.get(key).tag);
                }
            }
            return tag;
        }

        void push(Taint<E> tag, int branchID, int branchesSize, int level) {
            if(tag != null && !tag.isEmpty()) {
                if(branchLevels == null) {
                    branchLevels = new int[branchesSize];
                    Arrays.fill(branchLevels, NOT_PUSHED);
                }
                if(!levelStackMap.containsKey(level)) {
                    levelStackMap.put(level, Node.emptyNode());
                }
                if(branchLevels[branchID] == NOT_PUSHED) {
                    branchLevels[branchID] = level;
                    Taint<E> combined = Taint.combineTags(tag, levelStackMap.get(level).tag);
                    levelStackMap.put(level, new Node<>(combined, levelStackMap.get(level)));
                } else {
                    Node<E> r = levelStackMap.get(level);
                    r.tag = r.tag.union(tag);
                }
            }
        }

        void pop(int branchID) {
            if(branchLevels != null && branchLevels[branchID] != NOT_PUSHED) {
                levelStackMap.put(branchLevels[branchID], levelStackMap.get(branchLevels[branchID]).next);
                branchLevels[branchID] = NOT_PUSHED;
            }
        }

        void reset() {
            if(next != null) {
                next.reset();
            }
            branchLevels = null;
            levelStackMap.clear();
        }

        void exitLoopLevel(int levelOffset) {
            if(branchLevels != null) {
                int level = getLevel(levelOffset);
                levelStackMap.put(level, Node.emptyNode());
                for(int i = 0; i < branchLevels.length; i++) {
                    if(branchLevels[i] == level) {
                        branchLevels[i] = NOT_PUSHED;
                    }
                }
            }
        }
    }

    private static final class ControlFrameBuilder<E> {

        private int invocationLevel = 0;
        private int[] argumentStabilityLevels = null;
        private int currentArg = 0;

        ControlFrameBuilder<E> copy() {
            ControlFrameBuilder<E> copy = new ControlFrameBuilder<>();
            copy.invocationLevel = invocationLevel;
            copy.argumentStabilityLevels = argumentStabilityLevels == null ? null : argumentStabilityLevels.clone();
            copy.currentArg = currentArg;
            return copy;
        }

        void start(int invocationLevel, int numArguments) {
            this.invocationLevel = invocationLevel;
            argumentStabilityLevels = new int[numArguments];
            currentArg = 0;
        }

        void setNextArgLevel(int level) {
            argumentStabilityLevels[currentArg++] = level;
        }

        void reset() {
            invocationLevel = 0;
            argumentStabilityLevels = null;
            currentArg = 0;
        }

        ControlFrame<E> build(ControlFrame<E> next) {
            ControlFrame<E> frame = new ControlFrame<>(invocationLevel, argumentStabilityLevels, next);
            invocationLevel = 0;
            argumentStabilityLevels = null;
            currentArg = 0;
            return frame;
        }
    }

    private static final class Node<E> {
        @SuppressWarnings("rawtypes")
        private static final Node EMPTY_NODE = new Node<>(Taint.emptyTaint(), null);
        Taint<E> tag;
        Node<E> next;

        Node(Taint<E> tag, Node<E> next) {
            this.tag = tag;
            this.next = next;
        }

        @SuppressWarnings("unchecked")
        static <E> Node<E> emptyNode() {
            return (Node<E>) EMPTY_NODE;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("[");
            for(Node<E> cur = this; cur != null; cur = cur.next) {
                builder.append(cur.tag);
                if(cur.next != null) {
                    builder.append(", ");
                }
            }
            return builder.append("]").toString();
        }
    }

    @SuppressWarnings("unchecked")
    public static <E> ConfluxControlFlowStack<E> factory(boolean disabled) {
        if (disabled) {
            return disabledInstance;
        } else {
            return new ConfluxControlFlowStack<>(false);
        }
    }
}
