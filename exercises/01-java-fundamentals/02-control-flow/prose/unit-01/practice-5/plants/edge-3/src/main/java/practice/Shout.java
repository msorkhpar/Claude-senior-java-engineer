package practice;

public final class Shout {

    private Shout() {
    }

    /** Upper-cases a String longer than 5 characters, keeps a shorter one, and returns "" otherwise. */
    public static String shout(Object obj) {
        if (obj instanceof String s && s.length() > 5) {
            return s.toUpperCase();
        } else if (obj != null) {
            return obj.toString();
        }
        return "";
    }
}
