package practice;

import java.util.Optional;
import java.util.function.Function;

public final class Results {

    private Results() {
    }

    public enum ResultStatus { SUCCESS, FAILURE, PENDING }

    public record Result<T>(ResultStatus status, T value, String message) {

        /** On a success, the mapped value; otherwise the same status and message, without calling the mapper. */
        public <R> Result<R> map(Function<T, R> mapper) {
            if (status == ResultStatus.SUCCESS) {
                return success(mapper.apply(value));
            }
            return failure(message);
        }

        /** On a success, the result the mapper returns; otherwise the same status and message. */
        public <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
            if (status == ResultStatus.SUCCESS) {
                return mapper.apply(value);
            }
            return new Result<>(status, null, message);
        }

        /** The value, or empty. */
        public Optional<T> getValue() {
            return Optional.ofNullable(value);
        }
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(ResultStatus.SUCCESS, value, "OK");
    }

    public static <T> Result<T> failure(String message) {
        return new Result<>(ResultStatus.FAILURE, null, message);
    }

    public static <T> Result<T> pending() {
        return new Result<>(ResultStatus.PENDING, null, "Pending");
    }
}
