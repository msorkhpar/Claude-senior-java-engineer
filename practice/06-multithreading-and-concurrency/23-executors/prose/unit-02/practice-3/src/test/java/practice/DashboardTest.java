package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLTimeoutException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardTest {

    private static final ThreadFactory DAEMONS = r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    };

    private final ExecutorService pool = Executors.newFixedThreadPool(4, DAEMONS);
    private final ExecutorService browser = Executors.newSingleThreadExecutor(DAEMONS);
    private final CountDownLatch slowQueryDone = new CountDownLatch(1);
    private final Map<String, String> onScreen = new ConcurrentHashMap<>();

    @AfterEach
    void cleanUp() {
        slowQueryDone.countDown();
        pool.shutdownNow();
        browser.shutdownNow();
    }

    /** Loads the dashboard on a thread of its own, the way the browser's request does. */
    private Future<?> open(Dashboard.Screen screen, Map<String, Callable<String>> queries) {
        Dashboard dashboard = new Dashboard(pool, screen);
        return browser.submit(() -> {
            dashboard.load(queries);
            return null;
        });
    }

    @Test
    void everyPanelShowsItsQuerysAnswer() throws Exception {
        Map<String, Callable<String>> queries = new LinkedHashMap<>();
        queries.put("error-rate", () -> "0.4%");
        queries.put("p99-latency", () -> "812 ms");
        queries.put("open-incidents", () -> "2");

        open(onScreen::put, queries).get(5, TimeUnit.SECONDS);

        assertThat(onScreen).containsExactlyInAnyOrderEntriesOf(
                Map.of("error-rate", "0.4%", "p99-latency", "812 ms", "open-incidents", "2"));
    }

    @Test
    void aQuickPanelAppearsWhileASlowQueryIsStillRunning() throws Exception {
        CountDownLatch queueDepthShown = new CountDownLatch(1);
        Dashboard.Screen screen = (panel, content) -> {
            onScreen.put(panel, content);
            if (panel.equals("queue-depth")) queueDepthShown.countDown();
        };
        Map<String, Callable<String>> queries = new LinkedHashMap<>();
        queries.put("weekly-revenue", () -> {
            slowQueryDone.await();
            return "1.2M";
        });
        queries.put("queue-depth", () -> "17");

        Future<?> loading = open(screen, queries);

        assertThat(queueDepthShown.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(onScreen).doesNotContainKey("weekly-revenue");
        slowQueryDone.countDown();
        loading.get(5, TimeUnit.SECONDS);
        assertThat(onScreen).containsEntry("weekly-revenue", "1.2M");
    }

    @Test
    void aFailedQueryShowsUnavailableAndTheOtherPanelsStillAppear() throws Exception {
        Map<String, Callable<String>> queries = new LinkedHashMap<>();
        queries.put("disk-usage", () -> "71%");
        queries.put("billing-backlog", () -> {
            throw new SQLTimeoutException("billing replica: query cancelled after 30 s");
        });
        queries.put("deploys-today", () -> "9");

        try {
            open(onScreen::put, queries).get(5, TimeUnit.SECONDS);
        } catch (java.util.concurrent.ExecutionException ignored) {
            // the assertions below say what was missing
        }

        assertThat(onScreen).containsExactlyInAnyOrderEntriesOf(
                Map.of("disk-usage", "71%", "billing-backlog", "unavailable", "deploys-today", "9"));
    }
}
