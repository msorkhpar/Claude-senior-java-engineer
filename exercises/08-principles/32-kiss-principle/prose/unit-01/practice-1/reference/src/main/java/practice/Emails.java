package practice;

public final class Emails {

    private Emails() {
    }

    /** Returns whether the address has an @ with text on both sides. */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        int atIndex = email.indexOf('@');
        return atIndex > 0 && atIndex < email.length() - 1;
    }
}
