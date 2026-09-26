package practice;

public final class Escapes {

    private Escapes() {
    }

    /** Returns {@code raw} with its written escape sequences turned into characters. */
    public static String decode(String raw) {
        // one escape kind at a time, over the whole string
        String s = raw.replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\r", "\r")
                .replace("\\b", "\b")
                .replace("\\f", "\f")
                .replace("\\s", " ")
                .replace("\\\"", "\"")
                .replace("\\'", "'");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length() && s.charAt(i + 1) >= '0' && s.charAt(i + 1) <= '7') {
                int j = i + 1;
                int code = 0;
                while (j < s.length() && j < i + 4 && s.charAt(j) >= '0' && s.charAt(j) <= '7') {
                    code = code * 8 + (s.charAt(j) - '0');
                    j++;
                }
                out.append((char) code);
                i = j - 1;
            } else if (c == '\\' && i + 1 < s.length() && Character.isLetter(s.charAt(i + 1))) {
                throw new IllegalArgumentException("Invalid escape sequence: \\" + s.charAt(i + 1));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
