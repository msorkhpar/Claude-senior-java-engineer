package practice;

import java.util.concurrent.Callable;

public final class RequestContext {

    private RequestContext() {
    }

    /** Runs the work with {@code user} as the calling thread's current user, then clears it. */
    public static <T> T runAs(String user, Callable<T> work) throws Exception {
        throw new UnsupportedOperationException("write runAs");
    }

    /** The calling thread's current user, or null. */
    public static String current() {
        throw new UnsupportedOperationException("write current");
    }
}
