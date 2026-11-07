package practice;

/**
 * The certificate and private key the gateway's TLS listener presents. A reload thread replaces
 * them when the certificate is renewed; request threads read them for every new handshake.
 */
public class TlsCredentials {

    /** Where a reload reads the renewed certificate and key from: a file, a secrets vault. */
    public interface Source {
        String certificate();

        String privateKey();
    }

    /** A certificate and the private key that belongs to it. */
    public record Pair(String certificate, String privateKey) {
    }

    /** The credentials in use; a reload registers a new one and then loads it. */
    private static final class Loaded {
        String certificate;
        String privateKey;
    }

    private volatile Loaded current;

    public TlsCredentials(String certificate, String privateKey) {
        Loaded loaded = new Loaded();
        loaded.certificate = certificate;
        loaded.privateKey = privateKey;
        this.current = loaded;
    }

    /** Reads the renewed certificate and key from the source and starts using them. */
    public void reload(Source source) {
        Loaded next = new Loaded();
        current = next;
        next.certificate = source.certificate();
        next.privateKey = source.privateKey();
    }

    /** The certificate and key a new handshake uses. */
    public Pair current() {
        Loaded loaded = current;
        return new Pair(loaded.certificate, loaded.privateKey);
    }
}
