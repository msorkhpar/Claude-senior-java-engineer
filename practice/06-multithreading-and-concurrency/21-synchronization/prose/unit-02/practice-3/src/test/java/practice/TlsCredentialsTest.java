package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 20, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class TlsCredentialsTest {

    private static final TlsCredentials.Pair MAY = new TlsCredentials.Pair("cert:edge-2027-05", "key:edge-2027-05");
    private static final TlsCredentials.Pair NOVEMBER = new TlsCredentials.Pair("cert:edge-2027-11", "key:edge-2027-11");

    private static TlsCredentials.Source source(TlsCredentials.Pair pair) {
        return new TlsCredentials.Source() {
            public String certificate() { return new String(pair.certificate()); }
            public String privateKey() { return new String(pair.privateKey()); }
        };
    }

    /** Records what the server sent; runs {@code afterCertificate} once the certificate is sent. */
    private static final class Client implements TlsCredentials.Handshake {
        final List<String> received = new ArrayList<>();
        private final Runnable afterCertificate;

        Client(Runnable afterCertificate) {
            this.afterCertificate = afterCertificate;
        }

        @Override
        public void sendCertificate(String certificate) {
            received.add(certificate);
            afterCertificate.run();
        }

        @Override
        public void signWith(String privateKey) {
            received.add(privateKey);
        }
    }

    /** Runs a reload to {@code pair} on the reload thread, to the end. */
    private static void reloadOnAnotherThread(TlsCredentials credentials, TlsCredentials.Pair pair) {
        Thread reload = new Thread(() -> credentials.reload(source(pair)));
        reload.setDaemon(true);
        reload.start();
        try {
            reload.join(5_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertThat(reload.isAlive()).as("the reload finished").isFalse();
    }

    @Test
    void aHandshakeSendsTheCurrentCertificateAndSignsWithItsKey() {
        TlsCredentials credentials = new TlsCredentials(MAY.certificate(), MAY.privateKey());

        Client first = new Client(() -> { });
        credentials.serve(first);
        assertThat(first.received).containsExactly("cert:edge-2027-05", "key:edge-2027-05");

        credentials.reload(source(NOVEMBER));
        Client second = new Client(() -> { });
        credentials.serve(second);
        assertThat(second.received).containsExactly("cert:edge-2027-11", "key:edge-2027-11");
    }

    @Test
    void aReloadBetweenCertificateAndSignatureDoesNotChangeTheKey() {
        TlsCredentials credentials = new TlsCredentials(MAY.certificate(), MAY.privateKey());

        Client client = new Client(() -> reloadOnAnotherThread(credentials, NOVEMBER));
        credentials.serve(client);

        assertThat(client.received).as("the key matches the certificate the client was sent")
                .containsExactly("cert:edge-2027-05", "key:edge-2027-05");
        assertThat(credentials.current()).as("the next handshake gets the renewed pair").isEqualTo(NOVEMBER);
    }
}
