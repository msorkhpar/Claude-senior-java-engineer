package com.github.msorkhpar.claudejavatutor.synchronization;

import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Demonstrates deadlock scenarios and prevention strategies:
 * <ul>
 *   <li>Classic deadlock with inconsistent lock ordering</li>
 *   <li>Deadlock prevention via consistent lock ordering</li>
 *   <li>Deadlock prevention via tryLock with timeout</li>
 *   <li>Livelock scenario, and its fix with randomized backoff</li>
 * </ul>
 *
 * @see README_6.2.1.md
 */
public class DeadlockPrevention {

    // -----------------------------------------------------------------------
    // Bank Account — demonstrates lock ordering for deadlock prevention
    // -----------------------------------------------------------------------

    /**
     * A bank account that supports thread-safe transfers using consistent
     * lock ordering to prevent deadlock.
     *
     * <p>Without lock ordering, two threads performing:
     * <pre>
     *   Thread A: transfer(account1, account2, 100)
     *   Thread B: transfer(account2, account1, 50)
     * </pre>
     * could deadlock (A holds lock1, waits for lock2; B holds lock2, waits for lock1).
     *
     * <p>With lock ordering (always acquire the lock with the lower ID first),
     * both threads will attempt to acquire the same lock first, preventing
     * circular wait.
     */
    public static class BankAccount {
        private final long id;
        private int balance;
        private final Object lock = new Object();

        public BankAccount(long id, int initialBalance) {
            this.id = id;
            this.balance = initialBalance;
        }

        public long getId() {
            return id;
        }

        public int getBalance() {
            synchronized (lock) {
                return balance;
            }
        }

        /**
         * Transfers amount from {@code from} to {@code to} using consistent
         * lock ordering based on account ID. This prevents deadlock even when
         * two threads transfer in opposite directions simultaneously.
         *
         * @return true if the transfer succeeded, false if insufficient funds
         */
        public static boolean transfer(BankAccount from, BankAccount to, int amount) {
            // Determine lock order by account ID to prevent deadlock
            BankAccount first = from.id < to.id ? from : to;
            BankAccount second = from.id < to.id ? to : from;

            // Handle equal IDs (same account — should not transfer to self)
            if (from.id == to.id) {
                return false;
            }

            synchronized (first.lock) {
                synchronized (second.lock) {
                    if (from.balance >= amount) {
                        from.balance -= amount;
                        to.balance += amount;
                        return true;
                    }
                    return false;
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // TryLock-based Transfer — deadlock avoidance via timeout
    // -----------------------------------------------------------------------

    /**
     * A bank account using {@link ReentrantLock#tryLock(long, TimeUnit)} to avoid
     * deadlock. Instead of blocking indefinitely, the thread backs off if it
     * cannot acquire both locks within a timeout.
     */
    public static class TryLockAccount {
        private final long id;
        private int balance;
        private final ReentrantLock lock = new ReentrantLock();

        public TryLockAccount(long id, int initialBalance) {
            this.id = id;
            this.balance = initialBalance;
        }

        public long getId() {
            return id;
        }

        public int getBalance() {
            lock.lock();
            try {
                return balance;
            } finally {
                lock.unlock();
            }
        }

        /**
         * Attempts to transfer using tryLock with a timeout.
         * If both locks cannot be acquired together, the operation releases
         * any held lock and retries — avoiding deadlock through back-off.
         *
         * @param timeout  the maximum total time to keep retrying
         * @param unit     the time unit of the timeout
         * @return true if transfer succeeded, false if it could not acquire locks or insufficient funds
         * @throws InterruptedException if the current thread is interrupted
         */
        public static boolean transferWithTimeout(
                TryLockAccount from, TryLockAccount to, int amount,
                long timeout, TimeUnit unit) throws InterruptedException {

            long deadline = System.nanoTime() + unit.toNanos(timeout);

            while (System.nanoTime() < deadline) {
                if (from.lock.tryLock()) {
                    try {
                        if (to.lock.tryLock()) {
                            try {
                                if (from.balance >= amount) {
                                    from.balance -= amount;
                                    to.balance += amount;
                                    return true;
                                }
                                return false; // insufficient funds
                            } finally {
                                to.lock.unlock();
                            }
                        }
                    } finally {
                        from.lock.unlock();
                    }
                }
                // Back off briefly to let the other thread proceed
                Thread.onSpinWait();
            }
            return false; // timed out — could not acquire both locks
        }
    }

    // -----------------------------------------------------------------------
    // Deadlock Detector — demonstrates deadlock detection concept
    // -----------------------------------------------------------------------

    /**
     * A resource holder that intentionally creates a deadlock scenario for
     * demonstration and testing purposes.
     *
     * <p>Two threads each hold one lock and try to acquire the other's lock,
     * creating a classic circular-wait deadlock.
     */
    public static class DeadlockDemo {
        private final Object lock1 = new Object();
        private final Object lock2 = new Object();
        private volatile boolean thread1Started = false;
        private volatile boolean thread2Started = false;

        /**
         * Acquires lock1 then attempts to acquire lock2.
         * If another thread holds lock2 and is waiting for lock1, deadlock occurs.
         */
        public void methodA() {
            synchronized (lock1) {
                thread1Started = true;
                // Spin-wait for the other thread to start (to ensure deadlock)
                while (!thread2Started) {
                    Thread.onSpinWait();
                }
                synchronized (lock2) {
                    // This point is never reached in a deadlock scenario
                }
            }
        }

        /**
         * Acquires lock2 then attempts to acquire lock1 — opposite order from methodA.
         */
        public void methodB() {
            synchronized (lock2) {
                thread2Started = true;
                while (!thread1Started) {
                    Thread.onSpinWait();
                }
                synchronized (lock1) {
                    // This point is never reached in a deadlock scenario
                }
            }
        }

        public boolean isThread1Started() {
            return thread1Started;
        }

        public boolean isThread2Started() {
            return thread2Started;
        }
    }

    // -----------------------------------------------------------------------
    // Livelock Demonstration
    // -----------------------------------------------------------------------

    /**
     * Two "polite" walkers meet in a narrow hallway. Each round, a walker steps forward; if it sees
     * the other one also stepping forward, it politely steps back and tries again next round.
     *
     * <p>{@link #runPolite(int)} is a real livelock: both threads keep running and keep reacting to
     * each other, and neither ever gets through. A {@link Phaser} makes the two walkers move in lock
     * step. That is the timing under which livelock happens; with free-running threads it happens
     * only when their timing lines up, so a demonstration without lock step would be a flaky one.
     * The round limit is what makes it terminate: the result reports that the limit was hit with no
     * work done.
     *
     * <p>{@link #runWithRandomBackoff(int)} is the fix: after a conflict, each walker waits a random
     * number of rounds before stepping forward again. As soon as only one of them steps forward, it
     * gets through, and then the other does. Each round after a conflict breaks the symmetry with
     * probability 1/2, so both finish within a few rounds.
     */
    public static final class LivelockDemo {

        /**
         * What happened: whether each walker got through, and how many rounds each one used.
         */
        public record Outcome(boolean walker1Done, boolean walker2Done, int walker1Rounds, int walker2Rounds) {
            /** True if at least one walker got through. */
            public boolean anyProgress() {
                return walker1Done || walker2Done;
            }
        }

        private final AtomicBoolean[] stepsForward = {new AtomicBoolean(), new AtomicBoolean()};
        private final Phaser lockStep = new Phaser(2);
        private final boolean randomBackoff;
        private final int maxRounds;

        private LivelockDemo(boolean randomBackoff, int maxRounds) {
            this.randomBackoff = randomBackoff;
            this.maxRounds = maxRounds;
        }

        /** Runs the livelock: two polite walkers, at most {@code maxRounds} rounds each. */
        public static Outcome runPolite(int maxRounds) throws InterruptedException {
            return new LivelockDemo(false, maxRounds).run();
        }

        /** Runs the fixed version: the same walkers, with randomized backoff after a conflict. */
        public static Outcome runWithRandomBackoff(int maxRounds) throws InterruptedException {
            return new LivelockDemo(true, maxRounds).run();
        }

        private Outcome run() throws InterruptedException {
            boolean[] done = new boolean[2];
            int[] rounds = new int[2];
            Thread w1 = Thread.ofPlatform().name("walker-1").start(() -> walk(0, done, rounds));
            Thread w2 = Thread.ofPlatform().name("walker-2").start(() -> walk(1, done, rounds));
            w1.join(); // join makes each walker's writes to done/rounds visible here
            w2.join();
            return new Outcome(done[0], done[1], rounds[0], rounds[1]);
        }

        private void walk(int me, boolean[] done, int[] rounds) {
            int other = 1 - me;
            boolean afterConflict = false;
            for (int round = 1; round <= maxRounds; round++) {
                rounds[me] = round;
                // Phase 1: decide whether to step forward this round.
                boolean stepForward = !afterConflict || !randomBackoff
                        || ThreadLocalRandom.current().nextBoolean();
                stepsForward[me].set(stepForward);
                lockStep.arriveAndAwaitAdvance();

                // Phase 2: look at the other walker (both decisions are now visible).
                boolean goThrough = stepForward && !stepsForward[other].get();
                if (stepForward && !goThrough) {
                    afterConflict = true; // both stepped forward: politely step back
                }
                lockStep.arriveAndAwaitAdvance(); // the other walker has looked, too

                if (goThrough) {
                    done[me] = true; // the "work": getting through the hallway
                    stepsForward[me].set(false); // out of the way
                    lockStep.arriveAndDeregister(); // stop taking part in the lock step
                    return;
                }
            }
            // Round limit hit without getting through. Leave the lock step, so a walker that is
            // still registered can never wait for this one.
            lockStep.arriveAndDeregister();
        }
    }
}
