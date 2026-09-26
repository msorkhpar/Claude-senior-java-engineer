package practice;

import java.util.concurrent.Callable;

public final class RequestContext {

    private static volatile String user;

    private RequestContext() {
    }

    /** Runs the work with {@code user} as the calling thread's current user, then clears it. */
    public static <T> T runAs(String name, Callable<T> work) throws Exception {
        user = name;
        try {
            return work.call();
        } finally {
            user = null;
        }
    }

    /** The calling thread's current user, or null. */
    public static String current() {
        return user;
    }
}
