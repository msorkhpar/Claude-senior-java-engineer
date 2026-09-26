package practice;

public class Account {

    private String owner;

    /** Creates an account for the owner, trimmed. */
    public Account(String owner) {
        throw new UnsupportedOperationException("write Account");
    }

    /** Returns the owner. */
    public String owner() {
        throw new UnsupportedOperationException("write owner");
    }

    /** Replaces the owner and returns the old one. */
    public String rename(String owner) {
        throw new UnsupportedOperationException("write rename");
    }

    /** Returns the owner trimmed, refusing null and blank. */
    private static String checked(String owner) {
        throw new UnsupportedOperationException("write checked");
    }
}
