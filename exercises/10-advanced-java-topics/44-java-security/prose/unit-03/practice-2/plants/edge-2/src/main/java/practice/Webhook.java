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
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }

    /** True only when signature is exactly sign(key, body). */
    public static boolean verify(byte[] key, String body, String signature) throws GeneralSecurityException {
        byte[] expected = HexFormat.of().parseHex(sign(key, body));
        byte[] given = HexFormat.of().parseHex(signature);
        return MessageDigest.isEqual(expected, given);
    }
}
