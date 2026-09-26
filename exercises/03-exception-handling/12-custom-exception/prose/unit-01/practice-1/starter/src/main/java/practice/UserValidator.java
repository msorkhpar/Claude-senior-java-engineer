package practice;

/** Unchecked: thrown when a user's data is invalid. Carries the id of that user. */
class InvalidUserException extends RuntimeException {

    InvalidUserException(String message, String userId) {
        throw new UnsupportedOperationException("write InvalidUserException(String, String)");
    }

    String getUserId() {
        throw new UnsupportedOperationException("write getUserId");
    }
}

public class UserValidator {

    /** Returns normally for a usable username, else throws InvalidUserException naming userId. */
    public void validateUsername(String userId, String username) {
        throw new UnsupportedOperationException("write validateUsername");
    }
}
