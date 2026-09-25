package practice;

public final class CaesarShift {

    private CaesarShift() {
    }

    /** Returns {@code text} with every ASCII letter moved {@code k} places on (0 <= k <= 25). */
    public static String shift(String text, int k) {
        StringBuilder out = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                out.append((char) (c + k));
            } else if (c >= 'A' && c <= 'Z') {
                out.append((char) (c + k));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
