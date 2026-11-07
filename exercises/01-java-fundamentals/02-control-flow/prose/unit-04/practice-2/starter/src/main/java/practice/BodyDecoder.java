package practice;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Decodes request bodies that clients upload to the ingest API, according to the request's
 * Content-Encoding header.
 */
public class BodyDecoder {

    /** The encodings the ingest API accepts. */
    public enum Codec { IDENTITY, GZIP, DEFLATE }

    /** Wraps the raw body in the decoder its Content-Encoding header asks for. */
    public InputStream decode(InputStream body, String contentEncoding) throws IOException {
        switch (codecFor(contentEncoding)) {
            case GZIP:
                return new GZIPInputStream(body);
            case DEFLATE:
                return new InflaterInputStream(body);
            default:
                return body;
        }
    }

    /**
     * The codec for a Content-Encoding header value. The value is null when the request had
     * no such header. An encoding the API doesn't accept throws IllegalArgumentException.
     */
    Codec codecFor(String contentEncoding) {
        // TODO: Map the header's value to a codec: "identity", "gzip" (or its old alias
        //       "x-gzip") and "deflate". Clients differ in how they write the value, and a
        //       request may come without the header at all, which means its body is not
        //       encoded. Anything else is refused with IllegalArgumentException.
        throw new IllegalStateException("TODO: codecFor");
    }
}
