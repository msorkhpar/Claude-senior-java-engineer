package practice;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DigestsTest {

    /** Hands out at most one byte per read call, as a slow network stream may. */
    static final class Trickle extends InputStream {
        private final ByteArrayInputStream in;

        Trickle(byte[] data) {
            this.in = new ByteArrayInputStream(data);
        }

        @Override
        public int read() {
            return in.read();
        }

        @Override
        public int read(byte[] b, int off, int len) {
            return in.read(b, off, Math.min(len, 1));
        }
    }

    @Test
    void hashesTextAndStreams() throws Exception {
        String contract = "Contract: Alice pays Bob $100";
        String expected = "3f1474663ea161aa9567e07884bba2edb564c2bd63a44ab38fd0e959bdbcc2f5";

        assertThat(Digests.sha256Hex(contract)).isEqualTo(expected);
        assertThat(Digests.sha256Hex(new ByteArrayInputStream(contract.getBytes(StandardCharsets.UTF_8))))
                .isEqualTo(expected);
    }

    @Test
    void everyByteIsTwoDigits() throws Exception {
        assertThat(Digests.sha256Hex("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void aLeadingZeroByteIsKept() throws Exception {
        assertThat(Digests.sha256Hex("file-417"))
                .isEqualTo("004fe204e259e7d7a7c69613839d6ce32f03b6743bbcf99e939e606472c51411");
    }

    @Test
    void theWholeStreamIsHashed() throws Exception {
        assertThat(Digests.sha256Hex(new Trickle("abc".getBytes(StandardCharsets.UTF_8))))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        assertThat(Digests.sha256Hex(new Trickle(new byte[0])))
                .isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }
}
