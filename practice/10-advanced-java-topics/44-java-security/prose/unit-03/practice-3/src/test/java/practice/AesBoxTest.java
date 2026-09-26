package practice;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.AEADBadTagException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AesBoxTest {

    /** A predictable stand-in for SecureRandom, so the test can see which bytes became the IV. */
    static final class Sequence extends SecureRandom {
        private int next;

        Sequence(int start) {
            this.next = start;
        }

        @Override
        public void nextBytes(byte[] bytes) {
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) next++;
            }
        }
    }

    /** A test key made from a counter; it protects nothing. */
    private static SecretKey testKey() {
        byte[] raw = new byte[32];
        for (int i = 0; i < raw.length; i++) {
            raw[i] = (byte) (7 * i + 3);
        }
        return new SecretKeySpec(raw, "AES");
    }

    private static final byte[] MESSAGE = "Contract: Alice pays Bob $100".getBytes(StandardCharsets.UTF_8);

    @Test
    void opensWhatItSealed() throws Exception {
        AesBox box = new AesBox(testKey(), new SecureRandom());

        byte[] sealed = box.seal(MESSAGE);

        assertThat(box.open(sealed)).isEqualTo(MESSAGE);
        assertThat(box.open(box.seal(new byte[0]))).isEmpty();
    }

    @Test
    void eachSealDrawsAFreshIv() throws Exception {
        AesBox box = new AesBox(testKey(), new Sequence(1));

        byte[] first = box.seal(MESSAGE);
        byte[] second = box.seal(MESSAGE);

        assertThat(Arrays.copyOf(second, 12)).isNotEqualTo(Arrays.copyOf(first, 12));
        assertThat(second).isNotEqualTo(first);
    }

    @Test
    void theIvTravelsInFrontOfTheCiphertext() throws Exception {
        byte[] sealed = new AesBox(testKey(), new Sequence(40)).seal(MESSAGE);

        assertThat(sealed).hasSize(12 + MESSAGE.length + 16);
        assertThat(Arrays.copyOf(sealed, 12)).containsExactly(40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51);
        assertThat(new AesBox(testKey(), new Sequence(90)).open(sealed)).isEqualTo(MESSAGE);
    }

    @Test
    void aTamperedBoxIsRefused() throws Exception {
        AesBox box = new AesBox(testKey(), new SecureRandom());
        byte[] sealed = box.seal(MESSAGE);
        sealed[12 + 20] ^= 0x01;

        assertThatThrownBy(() -> box.open(sealed)).isInstanceOf(AEADBadTagException.class);
    }
}
