package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DecoderTest {

    @Test
    void decodesAsciiBytes() {
        assertThat(Decoder.decode(new byte[]{72, 101, 108, 108, 111})).isEqualTo("Hello");
        assertThat(Decoder.decode(new byte[]{})).isEmpty();
    }

    @Test
    void aCharacterMayTakeSeveralBytes() {
        assertThat(Decoder.decode(new byte[]{104, (byte) 0xC3, (byte) 0xA9})).isEqualTo("h\u00e9");
        assertThat(Decoder.decode(new byte[]{(byte) 0xE2, (byte) 0x82, (byte) 0xAC})).isEqualTo("\u20ac");
    }
}
