package practice;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeedLinesTest {

    private final List<String> lines = new ArrayList<>();

    /** A stream that hands out at most two bytes per read, as a slow network connection may. */
    private static InputStream twoBytesAtATime(byte[] bytes) {
        return new FilterInputStream(new ByteArrayInputStream(bytes)) {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return super.read(b, off, Math.min(len, 2));
            }
        };
    }

    @Test
    void handsOnEachLineInOrderAndCountsThem() throws IOException {
        byte[] feed = "SKU-881;12\nSKU-904;0\nSKU-117;305\n".getBytes(StandardCharsets.US_ASCII);

        int count = FeedLines.forEachLine(new ByteArrayInputStream(feed), StandardCharsets.UTF_8, lines::add);

        assertThat(count).isEqualTo(3);
        assertThat(lines).containsExactly("SKU-881;12", "SKU-904;0", "SKU-117;305");
    }

    @Test
    void readsAUtf8FeedThatArrivesInOnePiece() throws IOException {
        byte[] feed = "Ærøskøbing;4\nŁódź;9".getBytes(StandardCharsets.UTF_8);

        FeedLines.forEachLine(new ByteArrayInputStream(feed), StandardCharsets.UTF_8, lines::add);

        assertThat(lines).containsExactly("Ærøskøbing;4", "Łódź;9");
    }

    @Test
    void decodesWithTheCharsetThePartnerDeclared() throws IOException {
        byte[] feed = "Mölndal;7\nCórdoba;31\n".getBytes(StandardCharsets.ISO_8859_1);

        FeedLines.forEachLine(new ByteArrayInputStream(feed), StandardCharsets.ISO_8859_1, lines::add);

        assertThat(lines).containsExactly("Mölndal;7", "Córdoba;31");
    }

    @Test
    void aCharacterSplitAcrossReadsArrivesWhole() throws IOException {
        byte[] feed = "Ærø–Fyn;2\nŁódź;9\n".getBytes(StandardCharsets.UTF_8);

        FeedLines.forEachLine(twoBytesAtATime(feed), StandardCharsets.UTF_8, lines::add);

        assertThat(lines).containsExactly("Ærø–Fyn;2", "Łódź;9");
    }

    @Test
    void bytesNotValidInTheDeclaredCharsetEndTheRead() {
        byte[] feed = "Mölndal;7\n".getBytes(StandardCharsets.ISO_8859_1);    // declared UTF-8, sent Latin-1

        assertThatThrownBy(() -> FeedLines.forEachLine(new ByteArrayInputStream(feed), StandardCharsets.UTF_8, lines::add))
                .isInstanceOf(CharacterCodingException.class);
        assertThat(lines).isEmpty();
    }
}
