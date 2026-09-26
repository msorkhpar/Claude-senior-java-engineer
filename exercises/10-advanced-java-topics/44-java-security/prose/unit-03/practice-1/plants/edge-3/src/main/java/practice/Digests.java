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
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return hex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
    }

    /** SHA-256 of every byte of in, as 64 lower-case hex digits. */
    public static String sha256Hex(InputStream in) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        int n = in.read(buffer);
        if (n > 0) {
            digest.update(buffer, 0, n);
        }
        return hex(digest.digest());
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
