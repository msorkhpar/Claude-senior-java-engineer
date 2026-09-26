package practice;

public final class Kinds {

    private Kinds() {
    }

    /** Says whether s is null, empty, blank or text. */
    public static String kind(String s) {
        if (s == null) {
            return "null";
        }
        if (s.isEmpty()) {
            return "empty";
        }
        if (s.equals(" ")) {
            return "blank";
        }
        return "text";
    }
}
