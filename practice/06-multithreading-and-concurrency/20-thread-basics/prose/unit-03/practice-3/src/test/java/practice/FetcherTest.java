package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class FetcherTest {

    private final List<String> started = new ArrayList<>();

    private Fetcher.Fetch returning(String value) {
        return () -> {
            started.add(value);
            return value;
        };
    }

    /** A fetch whose own sleep is interrupted: it throws a real InterruptedException. */
    private Fetcher.Fetch interrupted(String name) {
        return () -> {
            started.add(name);
            Thread.currentThread().interrupt();
            Thread.sleep(60_000);
            return name;
        };
    }

    private static Throwable thrownBy(List<Fetcher.Fetch> fetches) {
        try {
            Fetcher.fetchAll(fetches);
            return null;
        } catch (Throwable t) {
            return t;
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void fetchesEverythingInOrder() throws InterruptedException {
        assertThat(Fetcher.fetchAll(List.of(returning("a"), returning("b"), returning("c")))).containsExactly("a", "b", "c");
        assertThat(started).containsExactly("a", "b", "c");
        assertThat(Fetcher.fetchAll(List.of())).isEmpty();
    }

    @Test
    void propagatesTheInterruptedException() {
        Throwable thrown = thrownBy(List.of(returning("a"), interrupted("b"), returning("c")));
        assertThat(thrown).isInstanceOf(InterruptedException.class);
    }

    @Test
    void stopsAtTheInterruptedFetch() {
        Throwable thrown = thrownBy(List.of(returning("a"), interrupted("b"), returning("c")));
        assertThat(thrown).isInstanceOf(InterruptedException.class);
        assertThat(started).containsExactly("a", "b");
    }

    @Test
    void anInterruptedCallerFetchesNothing() {
        Thread.currentThread().interrupt();
        Throwable thrown = thrownBy(List.of(returning("a"), returning("b")));
        assertThat(thrown).isInstanceOf(InterruptedException.class);
        assertThat(started).isEmpty();
    }
}
