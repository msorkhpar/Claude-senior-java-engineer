package practice;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;

public final class RequestContext {

    private RequestContext() {
    }

    public static <T> T process(String requestId, Supplier<T> work) {
        throw new UnsupportedOperationException("write process");
    }

    public static String currentRequestId() {
        throw new UnsupportedOperationException("write currentRequestId");
    }

    public static SimpleDateFormat formatter() {
        throw new UnsupportedOperationException("write formatter");
    }

    public static String format(Date date) {
        throw new UnsupportedOperationException("write format");
    }
}
