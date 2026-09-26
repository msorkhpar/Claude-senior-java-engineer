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
            throw new UnsupportedOperationException("write map");
        }

        /** On a success, the result the mapper returns; otherwise the same status and message. */
        public <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
            throw new UnsupportedOperationException("write flatMap");
        }

        /** The value, or empty. */
        public Optional<T> getValue() {
            throw new UnsupportedOperationException("write getValue");
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
