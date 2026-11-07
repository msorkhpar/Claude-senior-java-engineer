package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class HeapDumpTest {

    /** Runs the report on a daemon thread, so a scan that never ends fails the test instead of hanging it. */
    private static Set<String> garbage(List<HeapDump.Obj> heap, List<HeapDump.Obj> roots) throws Exception {
        Callable<Set<String>> report = () -> HeapDump.garbage(heap, roots);
        FutureTask<Set<String>> task = new FutureTask<>(report);
        Thread scanner = new Thread(task, "heap-scan");
        scanner.setDaemon(true);
        scanner.start();
        return task.get(5, TimeUnit.SECONDS);
    }

    private static HeapDump.Obj obj(String id) {
        return new HeapDump.Obj(id);
    }

    @Test
    void anObjectNoRootLeadsToIsGarbage() throws Exception {
        HeapDump.Obj config = obj("AppConfig");
        HeapDump.Obj pool = obj("ConnectionPool");
        HeapDump.Obj metrics = obj("MetricsRegistry");
        HeapDump.Obj request = obj("HttpRequest#4411");
        config.refersTo(pool, metrics);

        Set<String> garbage = garbage(List.of(config, pool, metrics, request), List.of(config));

        assertThat(garbage).containsExactly("HttpRequest#4411");
    }

    @Test
    void anObjectAtTheEndOfAChainFromARootIsKept() throws Exception {
        HeapDump.Obj listeners = obj("Listeners.ALL");
        HeapDump.Obj list = obj("ArrayList#91");
        HeapDump.Obj listener = obj("PriceListener#3");
        HeapDump.Obj screen = obj("QuoteScreen#3");
        HeapDump.Obj rows = obj("byte[]#2048");
        HeapDump.Obj closed = obj("QuoteScreen#2");
        listeners.refersTo(list);
        list.refersTo(listener);
        listener.refersTo(screen);
        screen.refersTo(rows);

        Set<String> garbage = garbage(List.of(listeners, list, listener, screen, rows, closed), List.of(listeners));

        assertThat(garbage).containsExactly("QuoteScreen#2");
    }

    @Test
    void objectsThatOnlyReferToEachOtherAreGarbage() throws Exception {
        HeapDump.Obj app = obj("Application");
        HeapDump.Obj session = obj("Session#77");
        HeapDump.Obj basket = obj("Basket#77");
        session.refersTo(basket);
        basket.refersTo(session);

        Set<String> garbage = garbage(List.of(app, session, basket), List.of(app));

        assertThat(garbage).containsExactly("Basket#77", "Session#77");
    }

    @Test
    void aCycleARootLeadsToIsKeptAndTheReportStillEnds() throws Exception {
        HeapDump.Obj cache = obj("TokenCache.INSTANCE");
        HeapDump.Obj first = obj("Entry#1");
        HeapDump.Obj second = obj("Entry#2");
        HeapDump.Obj expired = obj("Entry#0");
        cache.refersTo(first);
        first.refersTo(second);
        second.refersTo(first);

        Set<String> garbage = garbage(List.of(cache, first, second, expired), List.of(cache));

        assertThat(garbage).containsExactly("Entry#0");
    }
}
