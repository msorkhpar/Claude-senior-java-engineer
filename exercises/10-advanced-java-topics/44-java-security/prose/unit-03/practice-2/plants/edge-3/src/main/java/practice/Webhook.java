package practice;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class Webhook {

    private static Mac cached;

    private Webhook() {
    }

    /** HMAC-SHA256 of body under key, as 64 lower-case hex digits. */
    public static String sign(byte[] key, String body) throws GeneralSecurityException {
        if (cached == null) {
            cached = Mac.getInstance("HmacSHA256");
            cached.init(new SecretKeySpec(key, "HmacSHA256"));
        }
        return HexFormat.of().formatHex(cached.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }

    /** True only when signature is exactly sign(key, body). */
    public static boolean verify(byte[] key, String body, String signature) throws GeneralSecurityException {
        byte[] expected = sign(key, body).getBytes(StandardCharsets.UTF_8);
        byte[] given = signature.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, given);
    }
}
