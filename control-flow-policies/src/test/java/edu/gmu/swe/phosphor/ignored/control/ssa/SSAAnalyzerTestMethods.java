package edu.gmu.swe.phosphor.ignored.control.ssa;

@SuppressWarnings("unused")
public class SSAAnalyzerTestMethods {
    public static void indexOfBreak() {
        int z = 0; // constant
        int[] a = new int[5]; // variant +0
        for(/*constant */ int i = 0; i < a.length; i++) {
            if(a[i] == 0) {
                z = i; // variant +1
                break;
            }
        }
    }
}
