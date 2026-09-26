package practice;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HybridTest {

    private static KeyPair recipient;

    @BeforeAll
    static void keys() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        recipient = generator.generateKeyPair();
    }

    private static final byte[] MESSAGE = "Contract: Alice pays Bob $100".getBytes(StandardCharsets.UTF_8);

    /** Unwraps the session key the way any OAEP recipient would. */
    private static byte[] sessionKeyOf(Hybrid.Sealed sealed) throws Exception {
        Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        rsa.init(Cipher.DECRYPT_MODE, recipient.getPrivate());
        return rsa.doFinal(sealed.encryptedKey());
    }

    @Test
    void opensWhatWasSealedForIt() throws Exception {
        Hybrid.Sealed sealed = Hybrid.seal(MESSAGE, recipient.getPublic(), new SecureRandom());

        assertThat(Hybrid.open(sealed, recipient.getPrivate())).isEqualTo(MESSAGE);
    }

    @Test
    void aLongMessageIsCarriedByAes() throws Exception {
        byte[] large = new byte[10_000];
        for (int i = 0; i < large.length; i++) {
            large[i] = (byte) (i * 31);
        }

        Hybrid.Sealed sealed = Hybrid.seal(large, recipient.getPublic(), new SecureRandom());

        assertThat(Hybrid.open(sealed, recipient.getPrivate())).isEqualTo(large);
    }

    @Test
    void theSessionKeyIsWrappedWithOaep() throws Exception {
        Hybrid.Sealed sealed = Hybrid.seal(MESSAGE, recipient.getPublic(), new SecureRandom());

        byte[] key = sessionKeyOf(sealed);
        Cipher aes = Cipher.getInstance("AES/GCM/NoPadding");
        aes.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, sealed.data(), 0, 12));

        assertThat(key).hasSize(32);
        assertThat(aes.doFinal(sealed.data(), 12, sealed.data().length - 12)).isEqualTo(MESSAGE);
    }

    @Test
    void everyMessageGetsItsOwnSessionKey() throws Exception {
        SecureRandom random = new SecureRandom();
        Hybrid.Sealed first = Hybrid.seal(MESSAGE, recipient.getPublic(), random);
        Hybrid.Sealed second = Hybrid.seal(MESSAGE, recipient.getPublic(), random);

        assertThat(sessionKeyOf(second)).isNotEqualTo(sessionKeyOf(first));
    }
}
