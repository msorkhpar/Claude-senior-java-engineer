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
        throw new UnsupportedOperationException("write canSwap");
    }
}
