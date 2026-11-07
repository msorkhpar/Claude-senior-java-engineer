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

    private volatile String certificate;
    private volatile String privateKey;

    public TlsCredentials(String certificate, String privateKey) {
        this.certificate = certificate;
        this.privateKey = privateKey;
    }

    /** Reads the renewed certificate and key from the source and starts using them. */
    public void reload(Source source) {
        // TODO: Runs on the reload thread while request threads keep calling current(). Reading
        //       the source can be slow. Start using the renewed certificate and key. You may
        //       change how the credentials are stored.
        throw new IllegalStateException("TODO: reload");
    }

    /** The certificate and key a new handshake uses. */
    public Pair current() {
        // TODO: Runs on request threads, at any moment, including during a reload. Return the
        //       certificate and the key this handshake should use.
        throw new IllegalStateException("TODO: current");
    }
}
