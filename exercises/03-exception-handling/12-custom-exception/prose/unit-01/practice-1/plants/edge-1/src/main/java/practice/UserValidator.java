package practice;

/** Unchecked: thrown when a user's data is invalid. Carries the id of that user. */
class InvalidUserException extends RuntimeException {

    private final String message;
    private final String userId;

    InvalidUserException(String message, String userId) {
        this.message = message;
        this.userId = userId;
    }

    String getUserId() {
        return userId;
    }
}

public class UserValidator {

    /** Returns normally for a usable username, else throws InvalidUserException naming userId. */
    public void validateUsername(String userId, String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidUserException("Username cannot be null or empty", userId);
        }
    }
}
