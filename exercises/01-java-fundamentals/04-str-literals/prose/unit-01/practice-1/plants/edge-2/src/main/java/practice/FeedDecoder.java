package practice;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.util.List;

/**
 * Reads the product names out of a supplier's upload. Each supplier declares the charset its
 * upload is written in; the body holds one product name per line.
 */
public class FeedDecoder {

    /** One upload: who sent it, the charset they declared, and the body's bytes. */
    public record Upload(String supplier, String charset, byte[] body) { }

    /**
     * The product names of an upload, one per non-empty line.
     *
     * @throws CharacterCodingException when the body is not valid text in the declared charset
     */
    public List<String> names(Upload upload) throws CharacterCodingException {
        String text = decode(upload.body(), upload.charset());
        return text.lines().map(String::strip).filter(line -> !line.isEmpty()).toList();
    }

    /** The text of {@code body}, which its supplier says is written in {@code charsetName}. */
    String decode(byte[] body, String charsetName) throws CharacterCodingException {
        return new String(body, Charset.forName(charsetName));
    }
}
