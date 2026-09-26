package practice;

public final class Shift {

    private Shift() {
    }

    /** Returns {@code text} with up to {@code n} leading whitespace characters removed from each line. */
    public static String left(String text, int n) {
        StringBuilder out = new StringBuilder();
        for (String line : text.split("\n")) {
            int k = 0;
            while (k < n && k < line.length() && Character.isWhitespace(line.charAt(k))) {
                k++;
            }
            out.append(line.substring(k)).append('\n');
        }
        return out.toString();
    }
}
