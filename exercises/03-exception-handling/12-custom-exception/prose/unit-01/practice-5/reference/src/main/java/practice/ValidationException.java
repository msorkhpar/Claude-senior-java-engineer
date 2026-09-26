package practice;

public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message, null, false, false);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause, false, false);
    }
}
