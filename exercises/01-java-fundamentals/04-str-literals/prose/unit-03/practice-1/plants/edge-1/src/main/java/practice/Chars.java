package practice;

public final class Chars {

    private Chars() {
    }

    /** Returns the last character; refuses an empty String. */
    public static char lastChar(String s) {
        return s.charAt(s.length() - 1);
    }

    /** Says whether s starts with prefix. */
    public static boolean hasPrefix(String s, String prefix) {
        return s.startsWith(prefix);
    }
}
