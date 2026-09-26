package practice;

import java.util.concurrent.Callable;

public final class RequestContext {

    private static final ThreadLocal<String> USER = new ThreadLocal<>();

    private RequestContext() {
    }

    /** Runs the work with {@code user} as the calling thread's current user, then clears it. */
    public static <T> T runAs(String user, Callable<T> work) throws Exception {
        USER.set(user);
        T result = work.call();
        USER.remove();
        return result;
    }

    /** The calling thread's current user, or null. */
    public static String current() {
        return USER.get();
    }
}
