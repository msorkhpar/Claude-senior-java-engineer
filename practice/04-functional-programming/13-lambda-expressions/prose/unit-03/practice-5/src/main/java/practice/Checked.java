package practice;

import java.util.List;
import java.util.function.Function;

@FunctionalInterface
interface CheckedFunction<T, R> {
    R apply(T t) throws Exception;
}

class ApplyFailedException extends RuntimeException {
    ApplyFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

public final class Checked {

    private Checked() {
    }

    public static <T, R> Function<T, R> unchecked(CheckedFunction<T, R> function) {
        throw new UnsupportedOperationException("write unchecked");
    }

    public static <T, R> List<R> mapAll(List<T> inputs, CheckedFunction<T, R> function) {
        throw new UnsupportedOperationException("write mapAll");
    }
}
