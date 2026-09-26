package practice;

public final class Edits {

    private Edits() {
    }

    /** Inserts toInsert at position; position may be the length (the end). */
    public static String insertAt(String original, String toInsert, int position) {
        return new StringBuilder(original).insert(position, toInsert).toString();
    }

    /** Deletes the characters from start up to, not including, end. */
    public static String deleteRange(String original, int start, int end) {
        StringBuilder sb = new StringBuilder(original);
        return sb.delete(start, Math.min(end + 1, sb.length())).toString();
    }

    /** Returns the text reversed. */
    public static String reversed(String text) {
        return new StringBuilder(text).reverse().toString();
    }
}
