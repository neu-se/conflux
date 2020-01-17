package edu.gmu.swe.phosphor;

/**
 * Specifies which portion of flow benchmark's input data should be tainted.
 */
public enum TaintedPortionPolicy {

    FIRST_HALF("1stHalfTainted"),
    SECOND_HALF("2ndHalfTainted"),
    ALL("FullyTainted");

    private final String desc;

    TaintedPortionPolicy(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * Returns the inclusive start index of the portion of an input of the specified length that should be tainted for
     * this policy.
     */
    public int getTaintedRangeStart(int length) {
        return (this == TaintedPortionPolicy.SECOND_HALF) ? length/2 : 0;
    }

    /**
     * Returns the exclusive end index of the portion of an input of the specified length that should be tainted for
     * this policy.
     */
    public int getTaintedRangeEnd(int length) {
        return (this == TaintedPortionPolicy.FIRST_HALF) ? length/2 : length;
    }

    public boolean inTaintedRange(int i, int length) {
        return i >= getTaintedRangeStart(length) && i < getTaintedRangeEnd(length);
    }
}
