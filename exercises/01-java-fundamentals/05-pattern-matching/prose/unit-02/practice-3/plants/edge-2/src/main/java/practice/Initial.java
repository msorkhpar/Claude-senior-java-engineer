package practice;

public final class Initial {

    private Initial() {
    }

    /** Returns the upper-case first character of a String, or '?'. */
    public static char of(Object obj) {
        String s = obj instanceof String ? (String) obj : obj.toString();
        if (!(obj instanceof String) || s.isEmpty()) {
            return '?';
        }
        return Character.toUpperCase(s.charAt(0));
    }
}
