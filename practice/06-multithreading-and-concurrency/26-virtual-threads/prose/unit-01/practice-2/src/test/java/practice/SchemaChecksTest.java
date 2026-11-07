package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class SchemaChecksTest {

    /** A document is valid when it carries a signature; "%PDF" can't be read at all. */
    private static boolean verdict(String document) {
        if (document.startsWith("%PDF")) throw new IllegalArgumentException("not XML: " + document);
        return document.contains("<signature>");
    }

    /** Builds checkers, counting them. Each checker notes when two threads use it at once. */
    private static final class Checkers {
        final AtomicInteger built = new AtomicInteger();
        final CountDownLatch sharedUse = new CountDownLatch(1);
        volatile CountDownLatch allInside = new CountDownLatch(0);
        volatile CountDownLatch release = new CountDownLatch(0);

        SchemaChecks.Checker newChecker() {
            built.incrementAndGet();
            AtomicInteger users = new AtomicInteger();
            return document -> {
                if (users.incrementAndGet() > 1) sharedUse.countDown();
                allInside.countDown();
                try {
                    if (!release.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("never released");
                    return verdict(document);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                } finally {
                    users.decrementAndGet();
                }
            };
        }
    }

    /** Each request on a virtual thread of its own, as the upload service runs them. */
    private static List<Future<Boolean>> upload(ExecutorService requests, SchemaChecks checks, List<String> documents) {
        List<Future<Boolean>> verdicts = new ArrayList<>();
        for (String document : documents) verdicts.add(requests.submit(() -> checks.check(document)));
        return verdicts;
    }

    private static List<String> documents(int count) {
        List<String> documents = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            documents.add(i % 3 == 0 ? "<order id=\"" + i + "\"/>" : "<order id=\"" + i + "\"><signature>k" + i + "</signature></order>");
        }
        return documents;
    }

    @Test
    void manyUploadsAtOnceEachGetTheirVerdict() throws Exception {
        ExecutorService requests = Executors.newVirtualThreadPerTaskExecutor();
        Checkers checkers = new Checkers();
        try {
            SchemaChecks checks = new SchemaChecks(checkers::newChecker, 4);
            List<String> documents = documents(25);
            List<Future<Boolean>> verdicts = upload(requests, checks, documents);
            for (int i = 0; i < documents.size(); i++) {
                assertThat(verdicts.get(i).get(10, TimeUnit.SECONDS)).as(documents.get(i)).isEqualTo(verdict(documents.get(i)));
            }
        } finally {
            requests.shutdownNow();
        }
    }

    @Test
    void neverMoreCheckersThanTheLimit() throws Exception {
        ExecutorService requests = Executors.newVirtualThreadPerTaskExecutor();
        Checkers checkers = new Checkers();
        try {
            SchemaChecks checks = new SchemaChecks(checkers::newChecker, 3);
            for (Future<Boolean> verdict : upload(requests, checks, documents(12))) {
                verdict.get(10, TimeUnit.SECONDS);
            }
            assertThat(checkers.built.get()).as("checkers built for 12 uploads, limit 3").isBetween(1, 3);
        } finally {
            requests.shutdownNow();
        }
    }

    @Test
    void anUnreadableDocumentDoesNotCostTheServiceItsChecker() throws Exception {
        ExecutorService requests = Executors.newVirtualThreadPerTaskExecutor();
        Checkers checkers = new Checkers();
        try {
            SchemaChecks checks = new SchemaChecks(checkers::newChecker, 1);
            Future<Boolean> unreadable = requests.submit(() -> checks.check("%PDF-1.7 scanned-invoice"));
            Throwable failure = catchThrowable(() -> unreadable.get(10, TimeUnit.SECONDS));
            assertThat(failure).isInstanceOf(ExecutionException.class).hasCauseInstanceOf(IllegalArgumentException.class);

            Future<Boolean> next = requests.submit(() -> checks.check("<order id=\"77\"><signature>z</signature></order>"));
            assertThat(next.get(5, TimeUnit.SECONDS)).isTrue();
        } finally {
            requests.shutdownNow();
        }
    }

    @Test
    void noCheckerServesTwoUploadsAtOnce() throws Exception {
        ExecutorService requests = Executors.newVirtualThreadPerTaskExecutor();
        Checkers checkers = new Checkers();
        checkers.allInside = new CountDownLatch(3);
        checkers.release = new CountDownLatch(1);
        try {
            SchemaChecks checks = new SchemaChecks(checkers::newChecker, 3);
            List<Future<Boolean>> verdicts = upload(requests, checks, documents(3));
            assertThat(checkers.allInside.await(10, TimeUnit.SECONDS)).as("three uploads being checked at once").isTrue();
            assertThat(checkers.sharedUse.getCount()).as("a checker in use by two uploads at once").isEqualTo(1);
            checkers.release.countDown();
            for (Future<Boolean> verdict : verdicts) verdict.get(10, TimeUnit.SECONDS);
        } finally {
            checkers.release.countDown();
            requests.shutdownNow();
        }
    }
}
