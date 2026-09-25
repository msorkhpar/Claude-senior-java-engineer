package practice;

public final class CaesarShift {

    private CaesarShift() {
    }

    /** Returns {@code text} with every ASCII letter moved {@code k} places on (0 <= k <= 25). */
    public static String shift(String text, int k) {
        StringBuilder out = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                out.append((char) ('a' + (c - 'a' + k) % 26));
            } else if (c >= 'A' && c <= 'Z') {
                out.append((char) ('A' + (c - 'A' + k) % 26));
            } else {
                out.append((char) (c + k));
            }
        }
        return out.toString();
    }
}
