public class ExpressionSwitchBugs {
    private void testNested() {
        int i = 0;
        check(42, id(switch (42) {
            case 42: if (i == 0) {
                yield 41 + switch (0) {
                    case 0 -> 1;
                    default -> -1;
                };
            }
            default: i++; yield 43;
        }));
    }

    private void testAnonymousClasses() {
        for (int i : new int[] {1, 2}) {
            check(3, id((switch (i) {
                case 1 -> new I() {
                    public int g() { return 3; }
                };
                default -> (I) () -> { return 3; };
            }).g()));
        }
    }

    private final int value = 2;
    private final int field = id(switch(value) {
        case 0 -> -1;
        case 2 -> {
            int temp = 0;
            temp += 3;
            yield temp;
        }
        default -> throw new IllegalStateException();
    });

    private int id(int i) {
        return i;
    }

    private int id(Object o) {
        return -1;
    }

    private static void check(int a, int e) {
        if (a != e) {
            throw new AssertionError();
        }
    }

    public interface I {
        public int g();
    }
}