package practice;

public final class Kinds {

    private Kinds() {
    }

    /** Says whether s is null, empty, blank or text. */
    public static String kind(String s) {
        if (s == null) {
            return "null";
        }
        if (s.trim().isEmpty()) {
            return "empty";
        }
        return "text";
    }
}
