package practice;

import java.util.concurrent.Callable;

public final class RequestContext {
    private static final ThreadLocal<String> USER = new InheritableThreadLocal<>();
    private RequestContext() {
    }
    public static <T> T runAs(String user, Callable<T> work) throws Exception {
        USER.set(user);
        try {
            return work.call();
        } finally {
            USER.remove();
        }
    }
    public static String current() {
        return USER.get();
    }
}
