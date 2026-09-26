package practice;

public final class Chars {

    private Chars() {
    }

    /** Returns the last character; refuses an empty String. */
    public static char lastChar(String s) {
        if (s.isEmpty()) {
            throw new IllegalArgumentException("an empty String has no last character");
        }
        return s.charAt(s.length() - 1);
    }

    /** Says whether s starts with prefix. */
    public static boolean hasPrefix(String s, String prefix) {
        return s.substring(0, prefix.length()).equals(prefix);
    }
}
