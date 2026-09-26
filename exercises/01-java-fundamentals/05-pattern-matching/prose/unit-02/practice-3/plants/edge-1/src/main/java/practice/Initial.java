package practice;

public final class Initial {

    private Initial() {
    }

    /** Returns the upper-case first character of a String, or '?'. */
    public static char of(Object obj) {
        if (!(obj instanceof String s)) {
            return '?';
        }
        return Character.toUpperCase(s.charAt(0));
    }
}
