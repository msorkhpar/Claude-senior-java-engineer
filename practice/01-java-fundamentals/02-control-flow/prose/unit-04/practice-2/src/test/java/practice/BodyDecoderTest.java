package practice;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BodyDecoderTest {

    private static final String READING = "{\"sensor\":\"tank-4\",\"level\":0.73}";

    private final BodyDecoder decoder = new BodyDecoder();

    private static byte[] gzipped(String text) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(out)) {
            gz.write(text.getBytes(StandardCharsets.UTF_8));
        }
        return out.toByteArray();
    }

    private static String read(InputStream in) throws IOException {
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    @Test
    void theEncodingsTheApiAcceptsPickTheirCodec() {
        assertThat(decoder.codecFor("gzip")).isEqualTo(BodyDecoder.Codec.GZIP);
        assertThat(decoder.codecFor("x-gzip")).isEqualTo(BodyDecoder.Codec.GZIP);
        assertThat(decoder.codecFor("deflate")).isEqualTo(BodyDecoder.Codec.DEFLATE);
        assertThat(decoder.codecFor("identity")).isEqualTo(BodyDecoder.Codec.IDENTITY);
    }

    @Test
    void aGzippedUploadIsDecoded() throws IOException {
        InputStream body = new ByteArrayInputStream(gzipped(READING));

        assertThat(read(decoder.decode(body, "gzip"))).isEqualTo(READING);
    }

    @Test
    void anEncodingTheApiDoesNotAcceptIsRefused() {
        for (String encoding : new String[]{"br", "zstd", "compress"}) {
            assertThatThrownBy(() -> decoder.codecFor(encoding))
                    .as(encoding)
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void clientsThatCapitaliseTheEncodingAreUnderstood() {
        assertThat(decoder.codecFor("GZIP")).isEqualTo(BodyDecoder.Codec.GZIP);
        assertThat(decoder.codecFor("Deflate")).isEqualTo(BodyDecoder.Codec.DEFLATE);
        assertThat(decoder.codecFor("X-Gzip")).isEqualTo(BodyDecoder.Codec.GZIP);
    }

    @Test
    void anUploadWithoutTheHeaderIsReadAsItIs() throws IOException {
        assertThat(decoder.codecFor(null)).isEqualTo(BodyDecoder.Codec.IDENTITY);

        InputStream body = new ByteArrayInputStream(READING.getBytes(StandardCharsets.UTF_8));
        assertThat(read(decoder.decode(body, null))).isEqualTo(READING);
    }

    @Test
    void aServerWhoseDefaultLocaleIsTurkishStillUnderstandsTheHeader() {
        Locale before = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertThat(decoder.codecFor("IDENTITY")).isEqualTo(BodyDecoder.Codec.IDENTITY);
            assertThat(decoder.codecFor("Identity")).isEqualTo(BodyDecoder.Codec.IDENTITY);
        } finally {
            Locale.setDefault(before);
        }
    }
}
