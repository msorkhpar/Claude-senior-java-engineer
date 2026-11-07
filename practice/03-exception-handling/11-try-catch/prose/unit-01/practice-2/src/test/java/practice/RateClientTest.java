package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class RateClientTest {

    /** Every throwable a stack trace would print: the exception, its causes, and what they suppressed. */
    private static List<Throwable> printed(Throwable top) {
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<Throwable> out = new ArrayList<>();
        List<Throwable> todo = new ArrayList<>(List.of(top));
        while (!todo.isEmpty()) {
            Throwable t = todo.remove(todo.size() - 1);
            if (t == null || !seen.add(t)) continue;
            out.add(t);
            todo.add(t.getCause());
            todo.addAll(List.of(t.getSuppressed()));
        }
        return out;
    }

    private static RateClient.Source failing(IOException e, AtomicInteger calls) {
        return pair -> {
            calls.incrementAndGet();
            throw e;
        };
    }

    @Test
    void usesThePrimaryAndFallsBackToTheMirror() throws Exception {
        AtomicInteger mirrorCalls = new AtomicInteger();
        RateClient.Source mirror = pair -> {
            mirrorCalls.incrementAndGet();
            return new BigDecimal("0.0061");
        };

        RateClient healthy = new RateClient(pair -> new BigDecimal("0.0062"), mirror);
        assertThat(healthy.rate("JPY-CHF")).isEqualByComparingTo("0.0062");
        assertThat(mirrorCalls.get()).isZero();

        RateClient degraded = new RateClient(failing(new IOException("503 Service Unavailable"), new AtomicInteger()), mirror);
        assertThat(degraded.rate("JPY-CHF")).isEqualByComparingTo("0.0061");
        assertThat(mirrorCalls.get()).isEqualTo(1);

        RateClient down = new RateClient(failing(new IOException("503 Service Unavailable"), new AtomicInteger()),
                failing(new IOException("read timed out"), new AtomicInteger()));
        Throwable thrown = catchThrowable(() -> down.rate("SEK-NOK"));
        assertThat(thrown).isInstanceOf(RateClient.RatesUnavailableException.class).hasMessageContaining("SEK-NOK");
    }

    @Test
    void thePrimarysFailureCanBeFoundInWhatTheCheckoutGets() {
        IOException fromPrimary = new IOException("403 Forbidden: key revoked");
        RateClient client = new RateClient(failing(fromPrimary, new AtomicInteger()),
                failing(new IOException("connection reset"), new AtomicInteger()));

        Throwable thrown = catchThrowable(() -> client.rate("AUD-NZD"));

        assertThat(thrown).isInstanceOf(RateClient.RatesUnavailableException.class);
        assertThat(printed(thrown)).anySatisfy(t -> assertThat(t).isSameAs(fromPrimary));
    }

    @Test
    void theMirrorsFailureCanBeFoundInWhatTheCheckoutGets() {
        IOException fromMirror = new IOException("certificate expired");
        RateClient client = new RateClient(failing(new IOException("connect timed out"), new AtomicInteger()),
                failing(fromMirror, new AtomicInteger()));

        Throwable thrown = catchThrowable(() -> client.rate("CAD-MXN"));

        assertThat(thrown).isInstanceOf(RateClient.RatesUnavailableException.class);
        assertThat(printed(thrown)).anySatisfy(t -> assertThat(t).isSameAs(fromMirror));
    }
}
