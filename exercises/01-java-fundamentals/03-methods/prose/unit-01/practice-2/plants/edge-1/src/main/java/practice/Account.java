package practice;

public class Account {

    private String owner;

    /** Creates an account for the owner, trimmed. */
    public Account(String owner) {
        this.owner = checked(owner);
    }

    /** Returns the owner. */
    public String owner() {
        return owner;
    }

    /** Replaces the owner and returns the old one. */
    public String rename(String owner) {
        String old = this.owner;
        owner = checked(owner);
        return old;
    }

    /** Returns the owner trimmed, refusing null and blank. */
    private static String checked(String owner) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("an owner is required");
        }
        return owner.trim();
    }
}
