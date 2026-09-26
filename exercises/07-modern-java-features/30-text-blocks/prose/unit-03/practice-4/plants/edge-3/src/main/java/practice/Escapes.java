package practice;

public final class Escapes {

    private Escapes() {
    }

    /** Returns {@code raw} with its written escape sequences turned into characters. */
    public static String decode(String raw) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c != '\\') {
                out.append(c);
                continue;
            }
            char next = raw.charAt(++i);
            switch (next) {
                case 'n' -> out.append('\n');
                case 't' -> out.append('\t');
                case 'r' -> out.append('\r');
                case 'b' -> out.append('\b');
                case 'f' -> out.append('\f');
                case 's' -> out.append(' ');
                case '"', '\'', '\\' -> out.append(next);
                default -> {
                    if (next >= '0' && next <= '7') {
                        int code = next - '0';
                        int limit = next <= '3' ? 2 : 1;
                        while (limit-- > 0 && i + 1 < raw.length() && raw.charAt(i + 1) >= '0' && raw.charAt(i + 1) <= '7') {
                            code = code * 8 + (raw.charAt(++i) - '0');
                        }
                        out.append((char) code);
                    } else {
                        throw new IllegalArgumentException("Invalid escape sequence: \\" + next);
                    }
                }
            }
        }
        return out.toString();
    }
}
