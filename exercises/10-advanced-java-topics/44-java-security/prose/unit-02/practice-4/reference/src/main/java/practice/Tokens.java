package practice;

public final class Tokens {

    private Tokens() {
    }

    /** Compares a secret token with a given one without revealing where they differ. */
    public static boolean matches(CharSequence expected, CharSequence given) {
        if (given == null) {
            return false;
        }
        if (expected.length() != given.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < expected.length(); i++) {
            result |= expected.charAt(i) ^ given.charAt(i);
        }
        return result == 0;
    }
}
