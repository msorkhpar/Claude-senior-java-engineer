package practice;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.LongConsumer;

import static org.assertj.core.api.Assertions.assertThat;

class PriceWatchersTest {

    /** A feed that hands a new subscriber the last price at once, on the subscribing thread. */
    static final class TestFeed implements PriceWatchers.PriceFeed {
        private final Map<String, Long> last = new ConcurrentHashMap<>();
        private final Map<String, List<LongConsumer>> listeners = new ConcurrentHashMap<>();

        @Override
        public void subscribe(String symbol, LongConsumer listener) {
            listeners.computeIfAbsent(symbol, s -> new CopyOnWriteArrayList<>()).add(listener);
            Long price = last.get(symbol);
            if (price != null) listener.accept(price);
        }

        void trade(String symbol, long cents) {
            last.put(symbol, cents);
            for (LongConsumer l : listeners.getOrDefault(symbol, List.of())) l.accept(cents);
        }
    }

    /** A watcher the desk wrote: alerts whenever the price is below a floor. */
    static final class Floor extends PriceWatchers.PriceWatcher {
        private final long floorCents;

        Floor(String symbol, long floorCents, PriceWatchers.Alerts alerts, PriceWatchers.PriceFeed feed) {
            super(symbol, alerts, feed);
            this.floorCents = floorCents;
        }

        @Override
        protected void onPrice(long cents) {
            if (cents < floorCents) alert(symbol() + " at " + cents + " is below " + floorCents);
        }
    }

    private static final ExecutorService DESK = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "desk");
        t.setDaemon(true);
        return t;
    });

    @AfterAll
    static void closeDesk() {
        DESK.shutdownNow();
    }

    /** Runs a step of the desk's on a thread of its own, so a step that never returns fails the test. */
    private static <T> T desk(Callable<T> step) throws Exception {
        return DESK.submit(step).get(10, TimeUnit.SECONDS);
    }

    private final TestFeed feed = new TestFeed();
    private final List<String> alerts = Collections.synchronizedList(new ArrayList<>());

    @Test
    void aStartedWatcherAlertsOnTradesPastItsLimit() throws Exception {
        desk(() -> {
            new PriceWatchers.Ceiling("NRDX", 250_00, alerts::add, feed).start();
            new Floor("KLMT", 40_00, alerts::add, feed).start();
            feed.trade("NRDX", 249_90);
            feed.trade("KLMT", 41_15);
            feed.trade("NRDX", 252_05);
            feed.trade("KLMT", 39_80);
            return null;
        });

        assertThat(alerts).hasSize(2);
        assertThat(alerts.get(0)).contains("NRDX", "25205");
        assertThat(alerts.get(1)).contains("KLMT", "3980");
    }

    @Test
    void aWatcherStartedBelowItsCeilingRaisesNothing() throws Exception {
        feed.trade("NRDX", 231_75);

        desk(() -> new PriceWatchers.Ceiling("NRDX", 250_00, alerts::add, feed).start());

        assertThat(alerts).isEmpty();
    }

    @Test
    void aWatcherStartedAboveItsCeilingAlertsAtOnce() throws Exception {
        feed.trade("NRDX", 262_40);

        desk(() -> new PriceWatchers.Ceiling("NRDX", 250_00, alerts::add, feed).start());

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0)).contains("NRDX", "26240");
    }

    @Test
    void aWatcherHearsNothingBeforeItIsStarted() throws Exception {
        PriceWatchers.PriceWatcher watcher = desk(() -> new Floor("KLMT", 40_00, alerts::add, feed));
        desk(() -> {
            feed.trade("KLMT", 38_60);
            return null;
        });
        assertThat(alerts).isEmpty();

        desk(watcher::start);

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0)).contains("KLMT", "3860");
    }
}
