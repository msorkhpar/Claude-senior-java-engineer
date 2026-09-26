package practice;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class Webhook {

    private Webhook() {
    }

    /** HMAC-SHA256 of body under key, as 64 lower-case hex digits. */
    public static String sign(byte[] key, String body) throws GeneralSecurityException {
        throw new UnsupportedOperationException("TODO");
    }

    /** True only when signature is exactly sign(key, body). */
    public static boolean verify(byte[] key, String body, String signature) throws GeneralSecurityException {
        throw new UnsupportedOperationException("TODO");
    }
}
