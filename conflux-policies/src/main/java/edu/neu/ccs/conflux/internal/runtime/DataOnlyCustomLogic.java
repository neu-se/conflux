package edu.neu.ccs.conflux.internal.runtime;

import edu.columbia.cs.psl.phosphor.control.ControlFlowStack;
import edu.columbia.cs.psl.phosphor.runtime.Taint;
import edu.columbia.cs.psl.phosphor.struct.LazyArrayObjTags;

public class DataOnlyCustomLogic {

    public static void arraycopy$$PHOSPHORTAGGED(Object src, Taint<?> srcTaint, int srcPos, Taint<?> srcPosTaint,
                                                 Object dest, Taint<?> destTaint, int destPos, Taint<?> destPosTaint,
                                                 int length, Taint<?> lengthTaint, ControlFlowStack ctrl) {
        arraycopy$$PHOSPHORTAGGED(src, srcTaint, srcPos, srcPosTaint, dest, destTaint, destPos, destPosTaint, length,
                lengthTaint);
    }

    public static void arraycopy$$PHOSPHORTAGGED(Object src, Taint<?> srcTaint, int srcPos, Taint<?> srcPosTaint,
                                                 Object dest, Taint<?> destTaint, int destPos, Taint<?> destPosTaint,
                                                 int length, Taint<?> lengthTaint) {
        arraycopy(src, srcPos, dest, destPos, length);
    }

    public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {
        if (src instanceof LazyArrayObjTags && dest instanceof LazyArrayObjTags) {
            System.arraycopy(((LazyArrayObjTags) src).getVal(), srcPos, ((LazyArrayObjTags) dest).getVal(), destPos,
                    length);
            if (((LazyArrayObjTags) src).taints != null) {
                if (((LazyArrayObjTags) dest).taints == null) {
                    ((LazyArrayObjTags) dest).taints = new Taint[((LazyArrayObjTags) dest).getLength()];
                }
                System.arraycopy(((LazyArrayObjTags) src).taints, srcPos, ((LazyArrayObjTags) dest).taints, destPos,
                        length);
            }
        } else if (src instanceof LazyArrayObjTags) {
            System.arraycopy(((LazyArrayObjTags) src).getVal(), srcPos, dest, destPos, length);
        } else if (dest instanceof LazyArrayObjTags) {
            System.arraycopy(src, srcPos, ((LazyArrayObjTags) dest).getVal(), destPos, length);
        } else {
            System.arraycopy(src, srcPos, dest, destPos, length);
        }
    }
}
