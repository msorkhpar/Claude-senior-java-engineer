package practice;

import org.junit.jupiter.api.Test;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeedDecoderTest {

    private final FeedDecoder decoder = new FeedDecoder();

    private static byte[] encode(String text, String charset) {
        return text.getBytes(Charset.forName(charset));
    }

    @Test
    void aUtf8UploadIsReadAsItsSupplierWroteIt() throws Exception {
        byte[] body = encode("Zoë's oat bars, 6 pack\n\nSmoked paprika 50 g\n", "UTF-8");

        assertThat(decoder.names(new FeedDecoder.Upload("Northfield Foods", "UTF-8", body)))
                .containsExactly("Zoë's oat bars, 6 pack", "Smoked paprika 50 g");
    }

    @Test
    void aLatin1UploadKeepsItsAccents() throws Exception {
        byte[] body = encode("Smørrebrød kit\nÆblekage 400 g\n", "ISO-8859-1");

        assertThat(decoder.names(new FeedDecoder.Upload("Fyn Bageri", "ISO-8859-1", body)))
                .containsExactly("Smørrebrød kit", "Æblekage 400 g");
    }

    @Test
    void aBodyThatIsNotValidInItsCharsetIsRefused() {
        byte[] whole = encode("Tarte flambée\nCrème brûlée 2 x 100 g\n", "UTF-8");
        byte[] cut = Arrays.copyOf(whole, whole.length - 16);   // the upload broke off inside "û"
        FeedDecoder.Upload upload = new FeedDecoder.Upload("Alsace Fine", "UTF-8", cut);

        assertThatThrownBy(() -> decoder.names(upload)).isInstanceOf(CharacterCodingException.class);
    }

    @Test
    void aWindows1252UploadKeepsItsEuroSigns() throws Exception {
        byte[] body = encode("Gift card € 25\nGift card € 50\n", "windows-1252");

        assertThat(decoder.names(new FeedDecoder.Upload("Kado Centrum", "windows-1252", body)))
                .containsExactly("Gift card € 25", "Gift card € 50");
    }
}
