package practice;

public final class Threads {
    private Threads() {
    }
    public record Spec(String name, boolean daemon, int priority) {
    }
    public static Thread create(Spec spec, Runnable task) {
        if (!spec.daemon()) {
            return Thread.ofPlatform().name(spec.name()).daemon(false).unstarted(task);
        }
        if (spec.priority() != Thread.NORM_PRIORITY) {
            return Thread.ofPlatform().name(spec.name()).daemon(true).priority(spec.priority()).unstarted(task);
        }
        return Thread.ofVirtual().name(spec.name()).unstarted(task);
    }
}
