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
        certificate = source.certificate();
        privateKey = source.privateKey();
    }

    /** The certificate and key a new handshake uses. */
    public Pair current() {
        return new Pair(certificate, privateKey);
    }
}
