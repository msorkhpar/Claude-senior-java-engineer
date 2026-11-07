package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class RetryingTest {

    /** Fails its first calls as scripted, then answers. */
    static final class ScriptedRates implements Retrying.Rates {
        final List<Exception> failures;
        int calls;

        ScriptedRates(Exception... failures) {
            this.failures = new ArrayList<>(List.of(failures));
        }

        @Override
        public long rate(String pair) throws IOException {
            calls++;
            if (!failures.isEmpty()) {
                Exception next = failures.remove(0);
                if (next instanceof IOException io) throw io;
                throw (RuntimeException) next;
            }
            return pair.equals("EUR/CHF") ? 937_412 : 1_084_600;
        }
    }

    @Test
    void aRateThatAnswersAtOnceIsReturned() throws IOException {
        ScriptedRates remote = new ScriptedRates();
        Retrying.Rates rates = Retrying.wrap(remote, Retrying.Rates.class, 4);

        assertThat(rates.rate("EUR/CHF")).isEqualTo(937_412);
        assertThat(remote.calls).isEqualTo(1);
    }

    @Test
    void aDroppedConnectionIsTriedAgain() throws IOException {
        ScriptedRates remote = new ScriptedRates(
                new SocketTimeoutException("read timed out"), new IOException("broken pipe"));
        Retrying.Rates rates = Retrying.wrap(remote, Retrying.Rates.class, 4);

        assertThat(rates.rate("GBP/USD")).isEqualTo(1_084_600);
        assertThat(remote.calls).isEqualTo(3);
    }

    @Test
    void whenEveryAttemptFailsTheCallerGetsTheLastNetworkError() {
        ScriptedRates remote = new ScriptedRates(
                new IOException("broken pipe"), new IOException("broken pipe"),
                new SocketTimeoutException("read timed out"), new IOException("never reached"));
        Retrying.Rates rates = Retrying.wrap(remote, Retrying.Rates.class, 3);

        Throwable thrown = catchThrowable(() -> rates.rate("EUR/CHF"));

        assertThat(thrown).isExactlyInstanceOf(SocketTimeoutException.class).hasMessage("read timed out");
        assertThat(remote.calls).isEqualTo(3);
    }

    @Test
    void aRejectedPairIsNotTriedAgain() {
        ScriptedRates remote = new ScriptedRates(
                new IllegalArgumentException("unknown pair XAU/EUR"), new IllegalArgumentException("again"));
        Retrying.Rates rates = Retrying.wrap(remote, Retrying.Rates.class, 3);

        Throwable thrown = catchThrowable(() -> rates.rate("XAU/EUR"));

        assertThat(thrown).isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("unknown pair XAU/EUR");
        assertThat(remote.calls).isEqualTo(1);
    }
}
