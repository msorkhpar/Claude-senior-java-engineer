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
        return input -> {
            try {
                return function.apply(input);
            } catch (Exception e) {
                throw new ApplyFailedException("failed on " + input, e);
            }
        };
    }

    public static <T, R> List<R> mapAll(List<T> inputs, CheckedFunction<T, R> function) {
        return inputs.stream().map(unchecked(function)).toList();
    }
}
