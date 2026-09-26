package practice;

public final class Threads {

    private Threads() {
    }

    /** What the caller asks of a thread. */
    public record Spec(String name, boolean daemon, int priority) {
    }

    /** An unstarted thread for the spec: virtual when a virtual thread can honour it, platform otherwise. */
    public static Thread create(Spec spec, Runnable task) {
        if (spec.priority() == Thread.NORM_PRIORITY) {
            return Thread.ofVirtual().name(spec.name()).unstarted(task);
        }
        return Thread.ofPlatform()
                .name(spec.name())
                .daemon(spec.daemon())
                .priority(spec.priority())
                .unstarted(task);
    }
}
