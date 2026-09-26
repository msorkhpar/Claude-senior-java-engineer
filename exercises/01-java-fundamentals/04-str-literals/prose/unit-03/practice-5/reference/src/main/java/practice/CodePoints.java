package practice;

public final class CodePoints {

    private CodePoints() {
    }

    /** Returns how many characters s shows, counting an emoji once. */
    public static int characters(String s) {
        return s.codePointCount(0, s.length());
    }

    /** Returns s reversed character by character, keeping each emoji whole. */
    public static String reverse(String s) {
        StringBuilder out = new StringBuilder();
        for (int i = s.length(); i > 0; ) {
            int cp = s.codePointBefore(i);
            out.appendCodePoint(cp);
            i -= Character.charCount(cp);
        }
        return out.toString();
    }
}
