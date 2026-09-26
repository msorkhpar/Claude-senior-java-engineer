package practice;

public final class Reordering {

    private Reordering() {
    }

    public enum Kind { READ, WRITE, VOLATILE_READ, VOLATILE_WRITE, LOCK, UNLOCK }

    /** One operation of a thread: a read or write of a variable, or a lock or unlock of a monitor. */
    public record Op(Kind kind, String target) {
    }

    /** Whether {@code first; second}, adjacent in program order, may run as {@code second; first}. */
    public static boolean canSwap(Op first, Op second) {
        if (isMonitor(first) && isMonitor(second)) {
            return false;
        }
        if (first.kind() == Kind.LOCK || second.kind() == Kind.UNLOCK) {
            return false; // it would move an operation out of the block
        }
        if (first.kind() == Kind.UNLOCK || second.kind() == Kind.LOCK) {
            Op other = isMonitor(first) ? second : first;
            return !isVolatile(other); // a plain operation may move into the block
        }
        Kind a = first.kind();
        Kind b = second.kind();
        if (isVolatile(a) && isVolatile(b)) {
            return false;
        }
        if (b == Kind.VOLATILE_WRITE) {
            return false; // nothing before a volatile write moves after it
        }
        if (a == Kind.VOLATILE_READ) {
            return false; // nothing after a volatile read moves before it
        }
        if (isVolatile(a) || isVolatile(b)) {
            return true;
        }
        return true;
    }

    private static boolean isVolatile(Kind kind) {
        return kind == Kind.VOLATILE_READ || kind == Kind.VOLATILE_WRITE;
    }

    private static boolean isVolatile(Op op) {
        return isVolatile(op.kind());
    }

    private static boolean isMonitor(Op op) {
        return op.kind() == Kind.LOCK || op.kind() == Kind.UNLOCK;
    }
}
