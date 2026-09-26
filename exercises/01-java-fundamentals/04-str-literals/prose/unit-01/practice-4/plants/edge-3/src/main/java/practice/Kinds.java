package practice;

public final class Kinds {

    private Kinds() {
    }

    /** Says whether s is null, empty, blank or text. */
    public static String kind(String s) {
        if (s.isEmpty()) {
            return "empty";
        }
        if (s.isBlank()) {
            return "blank";
        }
        return "text";
    }
}
