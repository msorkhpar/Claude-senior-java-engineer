package practice;

public final class Emails {

    private Emails() {
    }

    /** Returns the part before the @. */
    public static String user(String email) {
        int at = email.indexOf('@');
        return at < 0 ? email : email.substring(0, at);
    }

    /** Returns the part after the @, or "" when there is no @. */
    public static String domain(String email) {
        return email.substring(email.indexOf('@') + 1);
    }
}
