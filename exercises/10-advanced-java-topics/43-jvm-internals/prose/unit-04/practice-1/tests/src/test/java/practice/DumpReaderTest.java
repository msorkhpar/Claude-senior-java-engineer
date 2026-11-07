package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** The thread blocks below come from a real jcmd Thread.print on Java 21; each test takes the ones it needs. */
class DumpReaderTest {

    private static String dump(String... threads) {
        return "2026-11-07 01:20:13\n"
                + "Full thread dump OpenJDK 64-Bit Server VM (21.0.12.1+1-LTS mixed mode, sharing):\n\n"
                + String.join("\n", threads)
                + "\nJNI global refs: 14, weak refs: 0\n";
    }

    @Test
    void aBlockedThreadIsChargedToTheThreadHoldingItsLock() {
        assertThat(DumpReader.whoBlocksWhom(dump(NIGHTLY_CLOSE, TELLER)))
                .isEqualTo(Map.of("teller", List.of("nightly-close")));
    }

    @Test
    void threadsThatOnlyParkBlockNobody() {
        assertThat(DumpReader.whoBlocksWhom(dump(NIGHTLY_CLOSE, SETTLE_1))).isEmpty();
    }

    @Test
    void aThreadInWaitDoesNotHoldTheMonitorItWaitsOn() {
        Map<String, List<String>> report = DumpReader.whoBlocksWhom(dump(FEE_CALC, STATEMENT, REFUND_2));

        assertThat(report.get("refund-2")).containsExactly("fee-calc");
    }

    @Test
    void aThreadWaitingForANotifyIsNotBlocked() {
        Map<String, List<String>> report = DumpReader.whoBlocksWhom(dump(FEE_CALC, STATEMENT, REFUND_2));

        assertThat(report).containsOnlyKeys("refund-2");
    }

    @Test
    void twoLocksOfTheSameClassAreTwoLocks() {
        Map<String, List<String>> report =
                DumpReader.whoBlocksWhom(dump(SETTLE_1, SETTLE_2, FEE_CALC, REFUND_2));

        assertThat(report).isEqualTo(Map.of(
                "settle-2", List.of("settle-1"),
                "refund-2", List.of("fee-calc")));
    }

    @Test
    void aThreadCanHoldSeveralMonitorsAtOnce() {
        Map<String, List<String>> report = DumpReader.whoBlocksWhom(dump(SETTLE_1, SETTLE_2, REFUND_1));

        assertThat(report).isEqualTo(Map.of(
                "settle-2", List.of("settle-1"),
                "refund-1", List.of("settle-1")));
    }

    private static final String NIGHTLY_CLOSE = """
            "nightly-close" #28 [52113] prio=5 os_prio=0 cpu=0.19ms elapsed=1.98s tid=0x0000745dac1412f0 nid=52113 waiting on condition  [0x0000745d7a9fe000]
               java.lang.Thread.State: WAITING (parking)
            \tat jdk.internal.misc.Unsafe.park(java.base@21.0.12.1/Native Method)
            \t- parking to wait for  <0x00000005ab818178> (a java.util.concurrent.CountDownLatch$Sync)
            \tat java.util.concurrent.locks.LockSupport.park(java.base@21.0.12.1/LockSupport.java:221)
            \tat java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(java.base@21.0.12.1/AbstractQueuedSynchronizer.java:788)
            \tat java.util.concurrent.locks.AbstractQueuedSynchronizer.acquireSharedInterruptibly(java.base@21.0.12.1/AbstractQueuedSynchronizer.java:1133)
            \tat java.util.concurrent.CountDownLatch.await(java.base@21.0.12.1/CountDownLatch.java:230)
            \tat Payments.park(Payments.java:18)
            \tat Payments.nightlyClose(Payments.java:41)
            \t- locked <0x00000005ab817f68> (a Payments$Vault)
            \tat Payments$$Lambda/0x0000745d2c001000.run(Unknown Source)
            \tat java.lang.Thread.runWith(java.base@21.0.12.1/Thread.java:1596)
            \tat java.lang.Thread.run(java.base@21.0.12.1/Thread.java:1583)
            """;
    private static final String TELLER = """
            "teller" #29 [52114] prio=5 os_prio=0 cpu=0.19ms elapsed=1.78s tid=0x0000745dac142de0 nid=52114 waiting for monitor entry  [0x0000745d7a8fe000]
               java.lang.Thread.State: BLOCKED (on object monitor)
            \tat Payments.touchVault(Payments.java:32)
            \t- waiting to lock <0x00000005ab817f68> (a Payments$Vault)
            \tat Payments$$Lambda/0x0000745d2c001220.run(Unknown Source)
            \tat java.lang.Thread.runWith(java.base@21.0.12.1/Thread.java:1596)
            \tat java.lang.Thread.run(java.base@21.0.12.1/Thread.java:1583)
            """;
    private static final String SETTLE_1 = """
            "settle-1" #30 [52115] prio=5 os_prio=0 cpu=0.23ms elapsed=1.57s tid=0x0000745dac143f80 nid=52115 waiting on condition  [0x0000745d7a7fe000]
               java.lang.Thread.State: WAITING (parking)
            \tat jdk.internal.misc.Unsafe.park(java.base@21.0.12.1/Native Method)
            \t- parking to wait for  <0x00000005ab818178> (a java.util.concurrent.CountDownLatch$Sync)
            \tat java.util.concurrent.locks.LockSupport.park(java.base@21.0.12.1/LockSupport.java:221)
            \tat java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(java.base@21.0.12.1/AbstractQueuedSynchronizer.java:788)
            \tat java.util.concurrent.locks.AbstractQueuedSynchronizer.acquireSharedInterruptibly(java.base@21.0.12.1/AbstractQueuedSynchronizer.java:1133)
            \tat java.util.concurrent.CountDownLatch.await(java.base@21.0.12.1/CountDownLatch.java:230)
            \tat Payments.park(Payments.java:18)
            \tat Payments.settle(Payments.java:24)
            \t- locked <0x00000005ab8175c8> (a Payments$AuditLog)
            \t- locked <0x00000005ab816bf0> (a Payments$Account)
            \tat Payments$$Lambda/0x0000745d2c001440.run(Unknown Source)
            \tat java.lang.Thread.runWith(java.base@21.0.12.1/Thread.java:1596)
            \tat java.lang.Thread.run(java.base@21.0.12.1/Thread.java:1583)
            """;
    private static final String SETTLE_2 = """
            "settle-2" #31 [52116] prio=5 os_prio=0 cpu=0.37ms elapsed=1.37s tid=0x0000745dac145140 nid=52116 waiting for monitor entry  [0x0000745d7a6fe000]
               java.lang.Thread.State: BLOCKED (on object monitor)
            \tat Payments.touchChecking(Payments.java:29)
            \t- waiting to lock <0x00000005ab816bf0> (a Payments$Account)
            \tat Payments$$Lambda/0x0000745d2c001660.run(Unknown Source)
            \tat java.lang.Thread.runWith(java.base@21.0.12.1/Thread.java:1596)
            \tat java.lang.Thread.run(java.base@21.0.12.1/Thread.java:1583)
            """;
    private static final String REFUND_1 = """
            "refund-1" #32 [52117] prio=5 os_prio=0 cpu=0.29ms elapsed=1.17s tid=0x0000745dac146450 nid=52117 waiting for monitor entry  [0x0000745d7a5fe000]
               java.lang.Thread.State: BLOCKED (on object monitor)
            \tat Payments.touchAudit(Payments.java:30)
            \t- waiting to lock <0x00000005ab8175c8> (a Payments$AuditLog)
            \tat Payments$$Lambda/0x0000745d2c001880.run(Unknown Source)
            \tat java.lang.Thread.runWith(java.base@21.0.12.1/Thread.java:1596)
            \tat java.lang.Thread.run(java.base@21.0.12.1/Thread.java:1583)
            """;
    private static final String STATEMENT = """
            "statement" #33 [52118] prio=5 os_prio=0 cpu=0.15ms elapsed=0.97s tid=0x0000745dac1478c0 nid=52118 in Object.wait()  [0x0000745d7a4fe000]
               java.lang.Thread.State: WAITING (on object monitor)
            \tat java.lang.Object.wait0(java.base@21.0.12.1/Native Method)
            \t- waiting on <0x00000005ab816c08> (a Payments$Account)
            \tat java.lang.Object.wait(java.base@21.0.12.1/Object.java:366)
            \tat java.lang.Object.wait(java.base@21.0.12.1/Object.java:339)
            \tat Payments.statement(Payments.java:36)
            \t- locked <0x00000005ab816c08> (a Payments$Account)
            \tat Payments$$Lambda/0x0000745d2c001aa0.run(Unknown Source)
            \tat java.lang.Thread.runWith(java.base@21.0.12.1/Thread.java:1596)
            \tat java.lang.Thread.run(java.base@21.0.12.1/Thread.java:1583)
            """;
    private static final String FEE_CALC = """
            "fee-calc" #34 [52119] prio=5 os_prio=0 cpu=0.19ms elapsed=0.77s tid=0x0000745dac148c50 nid=52119 waiting on condition  [0x0000745d7a3fe000]
               java.lang.Thread.State: WAITING (parking)
            \tat jdk.internal.misc.Unsafe.park(java.base@21.0.12.1/Native Method)
            \t- parking to wait for  <0x00000005ab818178> (a java.util.concurrent.CountDownLatch$Sync)
            \tat java.util.concurrent.locks.LockSupport.park(java.base@21.0.12.1/LockSupport.java:221)
            \tat java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(java.base@21.0.12.1/AbstractQueuedSynchronizer.java:788)
            \tat java.util.concurrent.locks.AbstractQueuedSynchronizer.acquireSharedInterruptibly(java.base@21.0.12.1/AbstractQueuedSynchronizer.java:1133)
            \tat java.util.concurrent.CountDownLatch.await(java.base@21.0.12.1/CountDownLatch.java:230)
            \tat Payments.park(Payments.java:18)
            \tat Payments.feeCalc(Payments.java:40)
            \t- locked <0x00000005ab816c08> (a Payments$Account)
            \tat Payments$$Lambda/0x0000745d2c001cc0.run(Unknown Source)
            \tat java.lang.Thread.runWith(java.base@21.0.12.1/Thread.java:1596)
            \tat java.lang.Thread.run(java.base@21.0.12.1/Thread.java:1583)
            """;
    private static final String REFUND_2 = """
            "refund-2" #35 [52120] prio=5 os_prio=0 cpu=0.13ms elapsed=0.57s tid=0x0000745dac14a5d0 nid=52120 waiting for monitor entry  [0x0000745d7a2fe000]
               java.lang.Thread.State: BLOCKED (on object monitor)
            \tat Payments.touchSavings(Payments.java:31)
            \t- waiting to lock <0x00000005ab816c08> (a Payments$Account)
            \tat Payments$$Lambda/0x0000745d2c003000.run(Unknown Source)
            \tat java.lang.Thread.runWith(java.base@21.0.12.1/Thread.java:1596)
            \tat java.lang.Thread.run(java.base@21.0.12.1/Thread.java:1583)
            """;
}
