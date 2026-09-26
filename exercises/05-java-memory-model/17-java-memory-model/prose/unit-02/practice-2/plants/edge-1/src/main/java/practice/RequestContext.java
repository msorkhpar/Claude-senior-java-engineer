package practice;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;

public final class RequestContext {

    private static volatile String requestId;
    private static final ThreadLocal<SimpleDateFormat> FORMATTER =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    private RequestContext() {
    }

    public static <T> T process(String requestId, Supplier<T> work) {
        RequestContext.requestId = requestId;
        try {
            return work.get();
        } finally {
            RequestContext.requestId = null;
        }
    }

    public static String currentRequestId() {
        return requestId;
    }

    public static SimpleDateFormat formatter() {
        return FORMATTER.get();
    }

    public static String format(Date date) {
        return formatter().format(date);
    }
}
