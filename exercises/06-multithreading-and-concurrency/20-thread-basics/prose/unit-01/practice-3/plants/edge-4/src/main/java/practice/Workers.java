package practice;

import java.util.concurrent.ThreadFactory;

public final class Workers {

    private Workers() {
    }

    /** A factory of unstarted platform threads named prefix0, prefix1, ..., with this daemon status and priority. */
    public static ThreadFactory factory(String prefix, boolean daemon, int priority) {
        java.util.concurrent.atomic.AtomicInteger next = new java.util.concurrent.atomic.AtomicInteger();
        return task -> {
            Thread thread = new Thread(task, prefix + next.getAndIncrement());
            thread.setDaemon(daemon);
            thread.setPriority(priority);
            return thread;
        };
    }
}
