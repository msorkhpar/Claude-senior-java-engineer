package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class WorkshopsTest {

    /** A venue with a fixed number of seats per workshop; it can hold its first answer back. */
    static class Venue implements Workshops.Capacity {
        final int seats;
        final boolean holdFirst;
        final AtomicBoolean first = new AtomicBoolean(true);
        final CountDownLatch firstAsked = new CountDownLatch(1);
        final CountDownLatch answer = new CountDownLatch(1);

        Venue(int seats, boolean holdFirst) {
            this.seats = seats;
            this.holdFirst = holdFirst;
        }

        @Override
        public boolean admits(String workshop, int enrolledSoFar) {
            if (holdFirst && first.getAndSet(false)) {
                firstAsked.countDown();
                try {
                    answer.await(20, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return enrolledSoFar < seats;
        }
    }

    private final List<Thread> started = new ArrayList<>();

    Thread daemon(Runnable body) {
        Thread t = new Thread(body);
        t.setDaemon(true);
        t.start();
        started.add(t);
        return t;
    }

    @AfterEach
    void finishThreads() throws InterruptedException {
        for (Thread t : started) t.join(10_000);
    }

    static boolean blockedBy(Thread waiter, Thread holder) {
        ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(waiter.threadId());
        return info != null && info.getLockOwnerId() == holder.threadId();
    }

    static void awaitUntil(BooleanSupplier condition) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) Thread.onSpinWait();
    }

    /**
     * Starts the first enrollment and holds it while the venue's rule answers it; runs the second
     * until it ends or waits for the first; then lets the first finish. Returns both results.
     */
    static List<Boolean> race(Venue venue, Supplier<Boolean> firstCall, Supplier<Boolean> secondCall,
                              WorkshopsTest test) throws InterruptedException {
        AtomicReference<Boolean> firstResult = new AtomicReference<>();
        AtomicReference<Boolean> secondResult = new AtomicReference<>();
        Thread first = test.daemon(() -> firstResult.set(firstCall.get()));
        Thread second = null;
        try {
            assertThat(venue.firstAsked.await(5, TimeUnit.SECONDS))
                    .as("the first enrollment asked the venue's rule").isTrue();
            Thread s = test.daemon(() -> secondResult.set(secondCall.get()));
            second = s;
            awaitUntil(() -> !s.isAlive() || blockedBy(s, first));
        } finally {
            venue.answer.countDown();
        }
        first.join(10_000);
        second.join(10_000);
        List<Boolean> results = new ArrayList<>();
        results.add(firstResult.get());
        results.add(secondResult.get());
        return results;
    }

    @Test
    void signUpsFillAWorkshopAndThenAreRefused() {
        Workshops workshops = new Workshops(new Venue(2, false));
        Workshops.SignupPage page = new Workshops.SignupPage(workshops);

        assertThat(page.signUp("gradle-internals", "ana@example.com")).isTrue();
        assertThat(page.signUp("gradle-internals", "ben@example.com")).isTrue();
        assertThat(page.signUp("gradle-internals", "cy@example.com")).isFalse();
        assertThat(page.signUp("jfr-in-practice", "cy@example.com")).isTrue();

        assertThat(workshops.enrolled("gradle-internals")).isEqualTo(2);
        assertThat(workshops.enrolled("jfr-in-practice")).isEqualTo(1);
    }

    @Test
    void importedBadgesAndSignUpsShareTheSameSeats() {
        Workshops workshops = new Workshops(new Venue(3, false));
        Workshops.SignupPage page = new Workshops.SignupPage(workshops);
        Workshops.BadgeImport badges = new Workshops.BadgeImport(workshops);

        assertThat(badges.importBadge("loom-deep-dive", "B-7731")).isTrue();
        assertThat(page.signUp("loom-deep-dive", "dee@example.com")).isTrue();
        assertThat(badges.importBadge("loom-deep-dive", "B-7732")).isTrue();
        assertThat(badges.importBadge("loom-deep-dive", "B-7733")).isFalse();
        assertThat(page.signUp("loom-deep-dive", "eli@example.com")).isFalse();

        workshops.addWalkIn("loom-deep-dive");
        assertThat(workshops.enrolled("loom-deep-dive")).isEqualTo(4);
    }

    @Test
    void twoSignUpsAtOnceForTheLastSeatGetOneSeat() throws InterruptedException {
        Venue venue = new Venue(1, true);
        Workshops workshops = new Workshops(venue);
        Workshops.SignupPage page = new Workshops.SignupPage(workshops);

        List<Boolean> results = race(venue,
                () -> page.signUp("graalvm-native", "fay@example.com"),
                () -> page.signUp("graalvm-native", "gus@example.com"), this);

        assertThat(results).as("sign-up results").containsExactlyInAnyOrder(true, false);
        assertThat(workshops.enrolled("graalvm-native")).isEqualTo(1);
    }

    @Test
    void twoImportedBadgesAtOnceForTheLastSeatGetOneSeat() throws InterruptedException {
        Venue venue = new Venue(1, true);
        Workshops workshops = new Workshops(venue);
        Workshops.BadgeImport badges = new Workshops.BadgeImport(workshops);

        List<Boolean> results = race(venue,
                () -> badges.importBadge("panama-ffm", "B-1201"),
                () -> badges.importBadge("panama-ffm", "B-1202"), this);

        assertThat(results).as("import results").containsExactlyInAnyOrder(true, false);
        assertThat(workshops.enrolled("panama-ffm")).isEqualTo(1);
    }

    @Test
    void anImportedBadgeAndASignUpAtOnceForTheLastSeatGetOneSeat() throws InterruptedException {
        Venue venue = new Venue(1, true);
        Workshops workshops = new Workshops(venue);
        Workshops.SignupPage page = new Workshops.SignupPage(workshops);
        Workshops.BadgeImport badges = new Workshops.BadgeImport(workshops);

        List<Boolean> results = race(venue,
                () -> badges.importBadge("valhalla-preview", "B-4410"),
                () -> page.signUp("valhalla-preview", "hal@example.com"), this);

        assertThat(results).as("import, then sign-up").containsExactlyInAnyOrder(true, false);
        assertThat(workshops.enrolled("valhalla-preview")).isEqualTo(1);
    }
}
