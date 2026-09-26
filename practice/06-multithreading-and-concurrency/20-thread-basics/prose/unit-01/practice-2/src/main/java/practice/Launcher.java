package practice;

public final class Launcher {

    /** What a task spends its time on. */
    public enum Work { IO_BOUND, CPU_BOUND }

    private Launcher() {
    }

    /** Starts {@code task} on a thread named {@code name}: virtual for I/O-bound work, platform for CPU-bound work. */
    public static Thread start(Work work, String name, Runnable task) {
        throw new UnsupportedOperationException("write start");
    }
}
