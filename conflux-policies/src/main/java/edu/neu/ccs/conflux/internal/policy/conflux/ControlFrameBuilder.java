package edu.neu.ccs.conflux.internal.policy.conflux;

final class ControlFrameBuilder<E> {

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
