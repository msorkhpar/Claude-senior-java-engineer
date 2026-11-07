package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 20, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class PointsAccountTest {

    /** Runs {@code body} on a new daemon thread. */
    private static Thread daemon(Runnable body) {
        Thread thread = new Thread(body);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    /**
     * A rule that, the first time it runs, says it is computing and then holds until released
     * (at most 9 s). Every later run computes at once.
     */
    private static LongUnaryOperator heldOnce(LongUnaryOperator rule, CountDownLatch computing, CountDownLatch release) {
        AtomicBoolean first = new AtomicBoolean(true);
        return balance -> {
            if (first.getAndSet(false)) {
                computing.countDown();
                try {
                    release.await(9, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return rule.applyAsLong(balance);
        };
    }

    /** Waits, at most 5 s, until {@code thread} has finished or waits for a lock that {@code holder} owns. */
    private static void awaitDoneOrWaitingFor(Thread thread, Thread holder) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            if (thread.getState() == Thread.State.TERMINATED) {
                return;
            }
            ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(thread.threadId());
            if (info != null && info.getLockOwnerId() == holder.threadId()) {
                return;
            }
            Thread.yield();
        }
    }

    /**
     * Starts {@code slow} on one thread and holds it inside its first computation; runs
     * {@code quick} on another thread meanwhile; then lets the slow rule finish. Returns the
     * balance afterwards.
     */
    private static long whileComputing(PointsAccount account, LongUnaryOperator slow, LongUnaryOperator quick)
            throws InterruptedException {
        CountDownLatch computing = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread slowThread = daemon(() -> account.apply(heldOnce(slow, computing, release)));
        assertThat(computing.await(5, TimeUnit.SECONDS)).as("the slow rule started computing").isTrue();
        Thread quickThread = daemon(() -> account.apply(quick));
        awaitDoneOrWaitingFor(quickThread, slowThread);
        release.countDown();
        slowThread.join(5_000);
        quickThread.join(5_000);
        assertThat(slowThread.isAlive() || quickThread.isAlive()).as("both rules finished").isFalse();
        return account.balance();
    }

    @Test
    void appliesRulesInTurnAndReturnsEachNewBalance() {
        PointsAccount account = new PointsAccount(380);

        assertThat(account.balance()).isEqualTo(380);
        assertThat(account.apply(p -> p + 145)).isEqualTo(525);
        assertThat(account.apply(p -> p * 2)).isEqualTo(1_050);
        assertThat(account.apply(p -> Math.min(p, 900))).isEqualTo(900);
        assertThat(account.apply(p -> p - 75)).isEqualTo(825);
        assertThat(account.balance()).isEqualTo(825);
    }

    @Test
    void anEarnWhileABonusIsComputingIsNotOverwritten() throws InterruptedException {
        long balance = whileComputing(new PointsAccount(400), p -> p + 250, p -> p + 120);

        assertThat(balance).as("400 + 250 bonus + 120 earned").isEqualTo(770);
    }

    @Test
    void aPromotionAppliesToTheBalanceAsItIsWhenItTakesEffect() throws InterruptedException {
        // Doubling and halving give the same result in either order: 1,000.
        long balance = whileComputing(new PointsAccount(1_000), p -> p * 2, p -> p / 2);

        assertThat(balance).as("1,000 doubled and halved, in either order").isEqualTo(1_000);
    }

    @Test
    void aBonusThatLostTheRaceIsStillApplied() throws InterruptedException {
        AtomicLong returnedToTheBonus = new AtomicLong();
        PointsAccount account = new PointsAccount(400);
        CountDownLatch computing = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread bonus = daemon(() -> returnedToTheBonus.set(account.apply(heldOnce(p -> p + 250, computing, release))));
        assertThat(computing.await(5, TimeUnit.SECONDS)).as("the bonus started computing").isTrue();
        Thread refund = daemon(() -> account.apply(p -> p - 100));
        awaitDoneOrWaitingFor(refund, bonus);
        release.countDown();
        bonus.join(5_000);
        refund.join(5_000);

        assertThat(account.balance()).as("400 + 250 bonus - 100 refunded").isEqualTo(550);
        assertThat(returnedToTheBonus.get()).as("the balance the bonus produced includes the bonus")
                .isIn(650L, 550L);
    }
}
