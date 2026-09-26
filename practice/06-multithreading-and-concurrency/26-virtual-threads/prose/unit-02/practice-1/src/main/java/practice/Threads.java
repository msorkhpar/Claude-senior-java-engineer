package practice;

public final class Threads {

    private Threads() {
    }

    /** What the caller asks of a thread. */
    public record Spec(String name, boolean daemon, int priority) {
    }

    /** An unstarted thread for the spec: virtual when a virtual thread can honour it, platform otherwise. */
    public static Thread create(Spec spec, Runnable task) {
        throw new UnsupportedOperationException("write create");
    }
}
