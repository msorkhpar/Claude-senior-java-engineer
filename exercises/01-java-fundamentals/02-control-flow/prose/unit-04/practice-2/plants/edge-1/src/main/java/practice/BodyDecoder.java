package practice;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
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
        if (contentEncoding == null) {
            return Codec.IDENTITY;
        }
        switch (contentEncoding) {
            case "identity":
                return Codec.IDENTITY;
            case "gzip", "x-gzip":
                return Codec.GZIP;
            case "deflate":
                return Codec.DEFLATE;
            default:
                throw new IllegalArgumentException("unsupported Content-Encoding: " + contentEncoding);
        }
    }
}
