package practice;

import java.nio.charset.StandardCharsets;

public final class Decoder {

    private Decoder() {
    }

    /** Returns the text the UTF-8 bytes encode. */
    public static String decode(byte[] utf8) {
        char[] chars = new char[utf8.length];
        for (int i = 0; i < utf8.length; i++) {
            chars[i] = (char) (utf8[i] & 0xFF);
        }
        return new String(chars);
    }
}
