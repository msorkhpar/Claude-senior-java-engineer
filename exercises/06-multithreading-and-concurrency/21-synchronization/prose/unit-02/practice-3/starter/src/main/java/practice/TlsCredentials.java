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

    /** One TLS handshake with a client, as far as the credentials are concerned. */
    public interface Handshake {
        /** Sends the server's certificate to the client. */
        void sendCertificate(String certificate);

        /** Proves the server owns the certificate it sent, by signing with its private key. */
        void signWith(String privateKey);
    }

    private volatile Pair current;

    public TlsCredentials(String certificate, String privateKey) {
        this.current = new Pair(certificate, privateKey);
    }

    /** Reads the renewed certificate and key from the source and starts using them. */
    public void reload(Source source) {
        current = new Pair(source.certificate(), source.privateKey());
    }

    /** The certificate and key a new handshake uses. */
    public Pair current() {
        return current;
    }

    /** Runs the server's side of one handshake: send the certificate, then sign. */
    public void serve(Handshake handshake) {
        // TODO: A reload can happen at any moment, including between the two steps of a
        //       handshake. The client checks the signature against the certificate it was sent.
        throw new IllegalStateException("TODO: serve");
    }
}
