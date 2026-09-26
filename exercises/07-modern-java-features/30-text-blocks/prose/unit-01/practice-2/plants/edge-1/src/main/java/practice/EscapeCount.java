package practice;

public final class EscapeCount {

    private EscapeCount() {
    }

    /** Returns how many escape sequences the literal body {@code body} holds. */
    public static int count(String body) {
        int escapes = 0;
        for (int i = 0; i < body.length(); i++) {
            if (body.charAt(i) == '\\') {
                escapes++;
            }
        }
        return escapes;
    }
}
