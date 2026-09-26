package practice;

import java.util.concurrent.ThreadFactory;

public final class Workers {

    private Workers() {
    }

    /** A factory of unstarted platform threads named prefix0, prefix1, ..., with this daemon status and priority. */
    public static ThreadFactory factory(String prefix, boolean daemon, int priority) {
        return Thread.ofPlatform().name(prefix, 0).daemon(daemon).factory();
    }
}
