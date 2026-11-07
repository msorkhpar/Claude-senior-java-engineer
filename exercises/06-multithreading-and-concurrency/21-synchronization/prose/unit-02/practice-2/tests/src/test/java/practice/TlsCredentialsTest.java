package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 20, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class TlsCredentialsTest {

    private static final TlsCredentials.Pair SPRING = new TlsCredentials.Pair("cert:gw-2026-03", "key:gw-2026-03");
    private static final TlsCredentials.Pair AUTUMN = new TlsCredentials.Pair("cert:gw-2026-09", "key:gw-2026-09");

    private static TlsCredentials.Source source(TlsCredentials.Pair pair) {
        return new TlsCredentials.Source() {
            public String certificate() { return new String(pair.certificate()); }
            public String privateKey() { return new String(pair.privateKey()); }
        };
    }

    /**
     * A source that, while the reload reads {@code slowPart} ("certificate" or "privateKey"),
     * says so and holds until released (at most 9 s).
     */
    private static TlsCredentials.Source slowAt(String slowPart, TlsCredentials.Pair pair,
                                                CountDownLatch reading, CountDownLatch release) {
        return new TlsCredentials.Source() {
            public String certificate() {
                if (slowPart.equals("certificate")) hold();
                return new String(pair.certificate());
            }

            public String privateKey() {
                if (slowPart.equals("privateKey")) hold();
                return new String(pair.privateKey());
            }

            private void hold() {
                reading.countDown();
                try {
                    release.await(9, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
    }

    /** What a handshake got during a held reload, and whether it was still waiting 5 s later. */
    private record Outcome(TlsCredentials.Pair pair, boolean waited) {
    }

    /** Starts a reload to AUTUMN that holds while it reads {@code slowPart}, and runs a handshake meanwhile. */
    private static Outcome handshakeDuringReload(String slowPart) throws InterruptedException {
        TlsCredentials credentials = new TlsCredentials(SPRING.certificate(), SPRING.privateKey());
        CountDownLatch reading = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread reload = new Thread(() -> credentials.reload(slowAt(slowPart, AUTUMN, reading, release)));
        reload.setDaemon(true);
        reload.start();
        assertThat(reading.await(5, TimeUnit.SECONDS)).as("the reload is reading the source").isTrue();

        AtomicReference<TlsCredentials.Pair> during = new AtomicReference<>();
        Thread handshake = new Thread(() -> during.set(credentials.current()));
        handshake.setDaemon(true);
        handshake.start();
        handshake.join(5_000);
        boolean waited = handshake.isAlive();
        TlsCredentials.Pair pair = during.get();

        release.countDown();
        reload.join(5_000);
        handshake.join(5_000);
        assertThat(credentials.current()).as("after the reload").isEqualTo(AUTUMN);
        return new Outcome(pair, waited);
    }

    @Test
    void handshakesUseTheRenewedPairAfterAReload() {
        TlsCredentials credentials = new TlsCredentials(SPRING.certificate(), SPRING.privateKey());
        assertThat(credentials.current()).isEqualTo(SPRING);

        credentials.reload(source(AUTUMN));
        assertThat(credentials.current()).isEqualTo(AUTUMN);

        credentials.reload(source(SPRING));
        assertThat(credentials.current()).isEqualTo(SPRING);
    }

    @Test
    void aHandshakeWhileTheKeyIsBeingReadGetsTheOldPair() throws InterruptedException {
        assertThat(handshakeDuringReload("privateKey").pair()).isEqualTo(SPRING);
    }

    @Test
    void aHandshakeWhileTheCertificateIsBeingReadGetsTheOldPair() throws InterruptedException {
        assertThat(handshakeDuringReload("certificate").pair()).isEqualTo(SPRING);
    }

    @Test
    void aHandshakeDoesNotWaitForASlowReload() throws InterruptedException {
        assertThat(handshakeDuringReload("privateKey").waited()).as("the handshake waited for the reload").isFalse();
    }
}
