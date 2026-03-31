package com.github.msorkhpar.claudejavatutor.synchronization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DisplayName("6.2.1 - Deadlock Prevention Tests")
class DeadlockPreventionTest {

    @Nested
    @DisplayName("BankAccount — Lock Ordering")
    class BankAccountTests {

        @Test
        @DisplayName("Should transfer funds correctly between two accounts")
        void testBasicTransfer() {
            var account1 = new DeadlockPrevention.BankAccount(1, 1000);
            var account2 = new DeadlockPrevention.BankAccount(2, 500);

            boolean result = DeadlockPrevention.BankAccount.transfer(account1, account2, 200);

            assertThat(result).isTrue();
            assertThat(account1.getBalance()).isEqualTo(800);
            assertThat(account2.getBalance()).isEqualTo(700);
        }

        @Test
        @DisplayName("Should reject transfer when insufficient funds")
        void testTransferInsufficientFunds() {
            var account1 = new DeadlockPrevention.BankAccount(1, 100);
            var account2 = new DeadlockPrevention.BankAccount(2, 500);

            boolean result = DeadlockPrevention.BankAccount.transfer(account1, account2, 200);

            assertThat(result).isFalse();
            assertThat(account1.getBalance()).isEqualTo(100);
            assertThat(account2.getBalance()).isEqualTo(500);
        }

        @Test
        @DisplayName("Should reject transfer to self")
        void testTransferToSelf() {
            var account = new DeadlockPrevention.BankAccount(1, 1000);

            boolean result = DeadlockPrevention.BankAccount.transfer(account, account, 100);

            assertThat(result).isFalse();
            assertThat(account.getBalance()).isEqualTo(1000);
        }

        @Test
        @DisplayName("Should not deadlock with concurrent bidirectional transfers (lock ordering)")
        @Timeout(10)
        void testConcurrentBidirectionalTransfers() throws InterruptedException {
            var account1 = new DeadlockPrevention.BankAccount(1, 100_000);
            var account2 = new DeadlockPrevention.BankAccount(2, 100_000);
            int transfersPerThread = 5_000;
            CountDownLatch latch = new CountDownLatch(2);

            // Thread A: transfer from account1 to account2
            Thread.ofPlatform().start(() -> {
                for (int i = 0; i < transfersPerThread; i++) {
                    DeadlockPrevention.BankAccount.transfer(account1, account2, 1);
                }
                latch.countDown();
            });

            // Thread B: transfer from account2 to account1 (opposite direction)
            Thread.ofPlatform().start(() -> {
                for (int i = 0; i < transfersPerThread; i++) {
                    DeadlockPrevention.BankAccount.transfer(account2, account1, 1);
                }
                latch.countDown();
            });

            boolean completed = latch.await(10, TimeUnit.SECONDS);
            assertThat(completed).isTrue()
                .as("Both threads should complete without deadlock due to lock ordering");

            // Total money in the system should be conserved
            assertThat(account1.getBalance() + account2.getBalance()).isEqualTo(200_000);
        }

        @Test
        @DisplayName("Should preserve total money across many concurrent transfers")
        @Timeout(10)
        void testMoneyConservation() throws InterruptedException {
            var accounts = new DeadlockPrevention.BankAccount[5];
            int initialBalance = 10_000;
            for (int i = 0; i < accounts.length; i++) {
                accounts[i] = new DeadlockPrevention.BankAccount(i, initialBalance);
            }

            int numThreads = 10;
            int transfersPerThread = 1_000;
            CountDownLatch latch = new CountDownLatch(numThreads);

            for (int t = 0; t < numThreads; t++) {
                final int seed = t;
                Thread.ofPlatform().start(() -> {
                    for (int i = 0; i < transfersPerThread; i++) {
                        int from = (seed + i) % accounts.length;
                        int to = (seed + i + 1) % accounts.length;
                        DeadlockPrevention.BankAccount.transfer(accounts[from], accounts[to], 1);
                    }
                    latch.countDown();
                });
            }

            latch.await(10, TimeUnit.SECONDS);

            int totalBalance = 0;
            for (var account : accounts) {
                totalBalance += account.getBalance();
            }
            assertThat(totalBalance).isEqualTo(accounts.length * initialBalance)
                .as("Total money must be conserved across all transfers");
        }
    }

    @Nested
    @DisplayName("TryLock-based Transfer")
    class TryLockAccountTests {

        @Test
        @DisplayName("Should transfer funds correctly with tryLock")
        void testBasicTryLockTransfer() throws InterruptedException {
            var account1 = new DeadlockPrevention.TryLockAccount(1, 1000);
            var account2 = new DeadlockPrevention.TryLockAccount(2, 500);

            boolean result = DeadlockPrevention.TryLockAccount.transferWithTimeout(
                account1, account2, 200, 1, TimeUnit.SECONDS
            );

            assertThat(result).isTrue();
            assertThat(account1.getBalance()).isEqualTo(800);
            assertThat(account2.getBalance()).isEqualTo(700);
        }

        @Test
        @DisplayName("Should return false when insufficient funds with tryLock")
        void testTryLockInsufficientFunds() throws InterruptedException {
            var account1 = new DeadlockPrevention.TryLockAccount(1, 50);
            var account2 = new DeadlockPrevention.TryLockAccount(2, 500);

            boolean result = DeadlockPrevention.TryLockAccount.transferWithTimeout(
                account1, account2, 100, 1, TimeUnit.SECONDS
            );

            assertThat(result).isFalse();
            assertThat(account1.getBalance()).isEqualTo(50);
            assertThat(account2.getBalance()).isEqualTo(500);
        }

        @Test
        @DisplayName("Should not deadlock with concurrent bidirectional tryLock transfers")
        @Timeout(15)
        void testConcurrentTryLockTransfers() throws InterruptedException {
            var account1 = new DeadlockPrevention.TryLockAccount(1, 100_000);
            var account2 = new DeadlockPrevention.TryLockAccount(2, 100_000);
            int transfersPerThread = 1_000;
            CountDownLatch latch = new CountDownLatch(2);
            AtomicInteger successCount = new AtomicInteger(0);

            Thread.ofPlatform().start(() -> {
                for (int i = 0; i < transfersPerThread; i++) {
                    try {
                        if (DeadlockPrevention.TryLockAccount.transferWithTimeout(
                                account1, account2, 1, 1, TimeUnit.SECONDS)) {
                            successCount.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                latch.countDown();
            });

            Thread.ofPlatform().start(() -> {
                for (int i = 0; i < transfersPerThread; i++) {
                    try {
                        if (DeadlockPrevention.TryLockAccount.transferWithTimeout(
                                account2, account1, 1, 1, TimeUnit.SECONDS)) {
                            successCount.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                latch.countDown();
            });

            boolean completed = latch.await(15, TimeUnit.SECONDS);
            assertThat(completed).isTrue()
                .as("Both threads should complete without deadlock");

            // Money is conserved
            assertThat(account1.getBalance() + account2.getBalance()).isEqualTo(200_000);
        }
    }

    @Nested
    @DisplayName("Deadlock Detection")
    class DeadlockDetectionTests {

        @Test
        @DisplayName("Should detect deadlock using ThreadMXBean")
        @Timeout(5)
        void testDeadlockDetection() throws InterruptedException {
            var demo = new DeadlockPrevention.DeadlockDemo();

            Thread t1 = Thread.ofPlatform().name("deadlock-thread-1").start(demo::methodA);
            Thread t2 = Thread.ofPlatform().name("deadlock-thread-2").start(demo::methodB);

            // Wait for both threads to start and acquire their first locks
            await().atMost(2, TimeUnit.SECONDS)
                .until(() -> demo.isThread1Started() && demo.isThread2Started());

            // Give threads time to reach deadlock state
            Thread.sleep(200);

            // Use ThreadMXBean to detect deadlock
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();

            assertThat(deadlockedThreads).isNotNull()
                .as("ThreadMXBean should detect the deadlocked threads");
            assertThat(deadlockedThreads.length).isGreaterThanOrEqualTo(2);

            // Clean up: interrupt the deadlocked threads
            t1.interrupt();
            t2.interrupt();
            // The threads won't respond to interrupt while in synchronized blocks,
            // but we try to be good citizens. They'll be cleaned up by the JVM.
        }
    }

    @Nested
    @DisplayName("Livelock Demonstration")
    class LivelockTests {

        @Test
        @DisplayName("Should demonstrate livelock where workers keep yielding without progress")
        @Timeout(5)
        void testLivelockBehavior() throws InterruptedException {
            int maxRetries = 100;
            var demo = new DeadlockPrevention.LivelockDemo(maxRetries);

            AtomicBoolean worker1Result = new AtomicBoolean(true);
            AtomicBoolean worker2Result = new AtomicBoolean(true);
            CountDownLatch latch = new CountDownLatch(2);

            Thread.ofPlatform().start(() -> {
                worker1Result.set(demo.worker1Work());
                latch.countDown();
            });

            Thread.ofPlatform().start(() -> {
                worker2Result.set(demo.worker2Work());
                latch.countDown();
            });

            latch.await(5, TimeUnit.SECONDS);

            // Both workers should fail to complete (livelock)
            assertThat(worker1Result.get()).isFalse()
                .as("Worker1 should fail to complete due to livelock");
            assertThat(worker2Result.get()).isFalse()
                .as("Worker2 should fail to complete due to livelock");

            // Both workers should have attempted retries
            assertThat(demo.getRetryCountWorker1()).isGreaterThan(0);
            assertThat(demo.getRetryCountWorker2()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should exhaust max retries in livelock scenario")
        void testLivelockExhaustsRetries() {
            int maxRetries = 10;
            var demo = new DeadlockPrevention.LivelockDemo(maxRetries);

            // Single-threaded livelock: worker1 keeps giving up
            boolean result = demo.worker1Work();

            assertThat(result).isFalse();
            assertThat(demo.getRetryCountWorker1()).isEqualTo(maxRetries);
        }
    }
}
