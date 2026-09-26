package practice;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Digests {

    private Digests() {
    }

    /** SHA-256 of text's UTF-8 bytes, as 64 lower-case hex digits. */
    public static String sha256Hex(String text) throws NoSuchAlgorithmException {
        throw new UnsupportedOperationException("TODO");
    }

    /** SHA-256 of every byte of in, as 64 lower-case hex digits. */
    public static String sha256Hex(InputStream in) throws NoSuchAlgorithmException, IOException {
        throw new UnsupportedOperationException("TODO");
    }
}
