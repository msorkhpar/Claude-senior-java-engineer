package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class PageFetcherTest {

    private final List<Integer> requested = Collections.synchronizedList(new ArrayList<>());

    private final PageFetcher fetcher = new PageFetcher(page -> {
        requested.add(page);
        return "invoices p" + page;
    });

    private static ExecutorService daemonPool(int threads) {
        return Executors.newFixedThreadPool(threads, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    @Test
    void buildsOneTaskPerPage() {
        assertThat(fetcher.tasks(5)).hasSize(5);
        assertThat(fetcher.tasks(0)).isEmpty();
    }

    @Test
    void eachTaskFetchesItsOwnPageWhenThePoolRunsIt() throws Exception {
        ExecutorService pool = daemonPool(3);
        ExecutorService caller = daemonPool(1);
        try {
            List<String> pages = caller.submit(() -> fetcher.fetchAll(4, pool)).get(10, TimeUnit.SECONDS);

            assertThat(pages).containsExactly("invoices p1", "invoices p2", "invoices p3", "invoices p4");
            assertThat(requested).containsExactlyInAnyOrder(1, 2, 3, 4);
        } finally {
            pool.shutdownNow();
            caller.shutdownNow();
        }
    }
}
