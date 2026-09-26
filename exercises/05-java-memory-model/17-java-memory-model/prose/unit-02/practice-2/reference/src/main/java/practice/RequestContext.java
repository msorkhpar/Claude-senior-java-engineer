package practice;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;

public final class RequestContext {

    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();
    private static final ThreadLocal<SimpleDateFormat> FORMATTER =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    private RequestContext() {
    }

    public static <T> T process(String requestId, Supplier<T> work) {
        REQUEST_ID.set(requestId);
        try {
            return work.get();
        } finally {
            REQUEST_ID.remove();
        }
    }

    public static String currentRequestId() {
        return REQUEST_ID.get();
    }

    public static SimpleDateFormat formatter() {
        return FORMATTER.get();
    }

    public static String format(Date date) {
        return formatter().format(date);
    }
}
